package main.java.com.pulsar.pulsarapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class LocalEventProcessor {

    private final Firestore db;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocalEventProcessor(Firestore db) {
        this.db = db;
    }

    // This method runs automatically after the application starts
    @PostConstruct
    public void startSubscriber() {
        String projectId = "local-project";
        String subscriptionId = "product-view-subscription";
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            try {
                ProductViewEvent event = objectMapper.readValue(message.getData().toStringUtf8(), ProductViewEvent.class);
                log.info("LOCAL PROCESSOR: Received event for productId: {}", event.productId);

                DocumentReference docRef = db.collection("trendingProducts").document(event.productId);
                docRef.update("viewCount", FieldValue.increment(1), "productId", event.productId);

                consumer.ack(); // Acknowledge the message
            } catch (Exception e) {
                log.error("Error processing message locally", e);
                consumer.nack(); // Tell Pub/Sub the message failed processing
            }
        };

        // If running with an emulator, create a subscriber that connects to it
        String pubsubEmulatorHost = System.getenv("PUBSUB_EMULATOR_HOST");
        if (pubsubEmulatorHost != null) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pubsubEmulatorHost).usePlaintext().build();
            TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

            Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver)
                    .setChannelProvider(channelProvider)
                    .setCredentialsProvider(credentialsProvider)
                    .build();
            
            subscriber.startAsync().awaitRunning();
            log.info("Local Pub/Sub subscriber started and listening on emulator.");
        } else {
            log.warn("PUBSUB_EMULATOR_HOST not set. Local subscriber is not running.");
        }
    }
}