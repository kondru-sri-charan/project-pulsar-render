package main.java.com.pulsar.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class StreamProcessorHandler implements BackgroundFunction<StreamProcessorHandler.PubSubMessage> {

    private static final Logger logger = Logger.getLogger(StreamProcessorHandler.class.getName());
    private static final String COLLECTION_NAME = "trendingProducts";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Firestore db = FirestoreOptions.getDefaultInstance().getService();

    public static class PubSubMessage {
        public Message message;
        public static class Message {
            public String data;
        }
    }

    @Override
    public void accept(PubSubMessage payload, Context context) {
        try {
            String encodedData = payload.message.data;
            if (encodedData == null) {
                logger.warning("Received Pub/Sub message with no data field.");
                return;
            }

            String jsonPayload = new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
            
            ProductViewEvent event = objectMapper.readValue(jsonPayload, ProductViewEvent.class);

            // --- THIS IS THE CORRECTED SECTION ---
            logger.info("Processing event for productId: " + event.productId); // Use direct field access
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(event.productId); // Use direct field access
            docRef.update("viewCount", FieldValue.increment(1), "productId", event.productId); // Use direct field access
            // --- END OF CORRECTION ---
            
        } catch (Exception e) {
            logger.severe("Error processing Pub/Sub message: " + e.getMessage());
        }
    }

    private static class ProductViewEvent {
        public String productId;
        public String userId;
        public long timestamp;
    }
}