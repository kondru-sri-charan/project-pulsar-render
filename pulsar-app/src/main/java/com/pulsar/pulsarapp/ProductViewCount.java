package com.pulsar.pulsarapp;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // A no-argument constructor is required for Firestore's automatic data mapping
public class ProductViewCount {
    private String productId;
    // Firestore stores numbers as Long, but we can safely map to int for this use case
    private long viewCount;
}