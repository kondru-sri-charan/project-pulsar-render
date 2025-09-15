package com.pulsar.pulsarapi;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GcpConfig {

    // This environment variable will be set by Docker Compose for local testing
    private final String firestoreEmulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST");

    @Bean
    public Firestore firestore() throws IOException {
        FirestoreOptions.Builder optionsBuilder = FirestoreOptions.newBuilder();

        // If the emulator host is set, connect to it
        if (firestoreEmulatorHost != null) {
            optionsBuilder.setEmulatorHost(firestoreEmulatorHost)
                          .setCredentialsProvider(NoCredentialsProvider.create())
                          .setProjectId("local-project"); // Dummy project ID for emulator
        }
        
        // Otherwise, the builder will use default credentials to connect to the real GCP Firestore
        
        return optionsBuilder.build().getService();
    }
}