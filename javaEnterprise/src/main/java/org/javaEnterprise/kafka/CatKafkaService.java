package org.javaEnterprise.kafka;

import org.javaEnterprise.kafka.dto.CatRequestMessage;
import org.javaEnterprise.kafka.dto.CatResponseMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CatKafkaService {
    private final KafkaTemplate<String, CatRequestMessage> kafkaTemplate;
    private final ConcurrentMap<Long, CompletableFuture<CatResponseMessage>> responseFutures = new ConcurrentHashMap<>();

    public CatKafkaService(KafkaTemplate<String, CatRequestMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<CatResponseMessage> sendRequest(CatRequestMessage request) {
        CompletableFuture<CatResponseMessage> future = new CompletableFuture<>();
        responseFutures.put(request.getRequestId(), future);
        kafkaTemplate.send("catService.requests", request);
        return future;
    }

    @KafkaListener(topics = "catService.responses", groupId = "java-enterprise-group", containerFactory = "catResponseKafkaListenerContainerFactory")
    public void handleResponse(CatResponseMessage response) {
        CompletableFuture<CatResponseMessage> future = responseFutures.remove(response.getRequestId());
        if (future != null) {
            future.complete(response);
        }
    }
} 