package main.java.com.pulsar.eventgenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewEvent {
    private String productId;
    private String userId;
    private long timestamp;
}