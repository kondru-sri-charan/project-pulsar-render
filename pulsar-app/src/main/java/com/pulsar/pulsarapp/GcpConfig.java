package main.java.com.pulsar.pulsarapp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcpConfig {

    // IMPORTANT: Change this to your actual GCP Project ID
    private final String projectId = "project-pulsar"; 
    private final String topicId = "product-view-topic";
    
    // This environment variable will be set by Docker Compose for local testing
    private final String pubsubEmulatorHost = System.getenv("PUBSUB_EMULATOR_HOST");

    @Bean
    public Publisher publisher() throws IOException {
        TopicName topicName = TopicName.of(projectId, topicId);
        Publisher.Builder publisherBuilder = Publisher.newBuilder(topicName);

        // If the emulator host is set, configure the client to connect to it instead of the real GCP service.
        if (pubsubEmulatorHost != null) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(pubsubEmulatorHost).usePlaintext().build();
            TransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
            CredentialsProvider credentialsProvider = NoCredentialsProvider.create(); // No auth needed for emulator
            
            publisherBuilder
                .setChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider);
        }
        
        // If pubsubEmulatorHost is null, the builder will use the default credentials
        // (from your GOOGLE_APPLICATION_CREDENTIALS) to connect to the real GCP Pub/Sub.
        
        return publisherBuilder.build();
    }


    @Bean
    public Firestore firestore() {
        String firestoreEmulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");
        FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder();

        if (firestoreEmulatorHost != null) {
            optionsBuilder.setEmulatorHost(firestoreEmulatorHost)
                          .setCredentialsProvider(NoCredentialsProvider.create())
                          .setProjectId("local-project");
        }
        return optionsBuilder.build().getService();
    }
}