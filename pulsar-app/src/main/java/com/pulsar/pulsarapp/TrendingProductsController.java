package com.pulsar.pulsarapp;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trending")
@RequiredArgsConstructor
public class TrendingProductsController {

    private final Firestore db;
    private static final String COLLECTION_NAME = "trendingProducts";

    @GetMapping
    public List<ProductViewCount> getTopTrendingProducts() throws ExecutionException, InterruptedException {
        // Create a query to get the top 10 products by 'viewCount' in descending order.
        Query query = db.collection(COLLECTION_NAME)
                        .orderBy("viewCount", Query.Direction.DESCENDING)
                        .limit(10);
        
        // Asynchronously retrieve the documents that match the query.
        // The .get().get() part waits for the asynchronous operation to complete.
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        // Map the retrieved documents to our ProductViewCount DTO
        return documents.stream()
                .map(doc -> doc.toObject(ProductViewCount.class))
                .collect(Collectors.toList());
    }
}