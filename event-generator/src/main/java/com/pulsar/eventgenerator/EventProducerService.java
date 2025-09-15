package com.pulsar.eventgenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
//import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EventProducerService {

    private final Publisher publisher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    public EventProducerService(Publisher publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 100) // Generate an event every 100ms
    public void generateEvent() {
        try {
            String productId = "product-" + random.nextInt(100);
            String userId = "user-" + random.nextInt(1000);

            ProductViewEvent event = new ProductViewEvent(productId, userId, System.currentTimeMillis());
            String eventJson = objectMapper.writeValueAsString(event);

            ByteString data = ByteString.copyFromUtf8(eventJson);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Publishing is asynchronous. The publish method returns a future.
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);

            // We can add a callback to log the result of the publish operation.
            ApiFutures.addCallback(messageIdFuture, new ApiFutureCallback<>() {
                public void onSuccess(String messageId) {
                    log.info("Published event for productId: {}, messageId: {}", productId, messageId);
                }
                public void onFailure(Throwable t) {
                    log.error("Failed to publish event to Pub/Sub for productId: {}", productId, t);
                }
            }, MoreExecutors.directExecutor());

        } catch (Exception e) {
            log.error("Error creating or publishing event", e);
        }
    }
}