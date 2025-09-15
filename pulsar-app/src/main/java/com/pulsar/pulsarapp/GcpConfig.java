package com.pulsar.pulsarapp;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcpConfig {

    private final String projectId = "local-project";
    private final String topicId = "product-view-topic";
    private final String pubsubEmulatorHost = System.getenv("PUBSUB_EMULATOR_HOST");

    @Bean
    public Publisher publisher() throws IOException {
        // This method is correct and unchanged
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher.Builder publisherBuilder = Publisher.newBuilder(topicName);
        if (pubsubEmulatorHost != null) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pubsubEmulatorHost).usePlaintext().build();
            TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
            publisherBuilder.setChannelProvider(channelProvider).setCredentialsProvider(credentialsProvider);
        }
        return publisherBuilder.build();
    }

    @Bean
    public Firestore firestore() {
        // This method is correct and unchanged
        String firestoreEmulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");
        FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder();
        if (firestoreEmulatorHost != null) {
            optionsBuilder.setEmulatorHost(firestoreEmulatorHost);
            optionsBuilder.setProjectId("local-project");
        }
        return optionsBuilder.build().getService();
    }

    // THIS IS THE NEW, CORRECT SUBSCRIBER BEAN
    @Bean(destroyMethod = "stopAsync")
    public Subscriber subscriber(LocalEventProcessor messageReceiver) throws IOException {
        String subscriptionId = "product-view-subscription";
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
        Subscriber.Builder subscriberBuilder = Subscriber.newBuilder(subscriptionName, messageReceiver);
        if (pubsubEmulatorHost != null) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pubsubEmulatorHost).usePlaintext().build();
            TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
            subscriberBuilder.setChannelProvider(channelProvider).setCredentialsProvider(credentialsProvider);
        }
        Subscriber subscriber = subscriberBuilder.build();
        subscriber.startAsync().awaitRunning();
        return subscriber;
    }
}