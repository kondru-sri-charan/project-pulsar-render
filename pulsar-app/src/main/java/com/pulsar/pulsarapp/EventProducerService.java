package com.pulsar.pulsarapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class EventProducerService {

    private final Publisher publisher; // Uses the manual Publisher
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public EventProducerService(Publisher publisher) { // Injects the Publisher bean from GcpConfig
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 100)
    public void generateEvent() {
        try {
            String productId = "product-" + random.nextInt(100);
            ProductViewEvent event = new ProductViewEvent(productId, "user-" + random.nextInt(1000), System.currentTimeMillis());
            String eventJson = objectMapper.writeValueAsString(event);

            ByteString data = ByteString.copyFromUtf8(eventJson);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            
            // Use the manual publisher to send the message
            publisher.publish(pubsubMessage);
            log.info(">>> PRODUCER: Published event for productId: {}", productId);

        } catch (Exception e) {
            log.error("Failed to publish event to Pub/Sub", e);
        }
    }
}