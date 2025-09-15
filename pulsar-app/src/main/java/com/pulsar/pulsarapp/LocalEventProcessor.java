package com.pulsar.pulsarapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class LocalEventProcessor implements MessageReceiver {

    private final Firestore db;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LocalEventProcessor(Firestore db) {
        this.db = db;
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            ProductViewEvent event = objectMapper.readValue(message.getData().toStringUtf8(), ProductViewEvent.class);
            log.info("<<< CONSUMER: Received event for productId: {}", event.getProductId());

            DocumentReference docRef = db.collection("trendingProducts").document(event.getProductId());

            // --- THIS IS THE CRITICAL FIX ---
            // We create a map of the data we want to write.
            Map<String, Object> data = new HashMap<>();
            data.put("productId", event.getProductId());
            data.put("viewCount", FieldValue.increment(1)); // This will atomically increment the count

            // Use .set() with SetOptions.merge() to create the document if it doesn't exist,
            // or merge the data (including the increment) if it does.
            docRef.set(data, com.google.cloud.firestore.SetOptions.merge());
            // --- END OF FIX ---

            consumer.ack();
        } catch (Exception e) {
            log.error("Error processing message and writing to Firestore", e);
            consumer.nack();
        }
    }
}