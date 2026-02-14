package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.client.AiServiceClient;
import org.example.backend.dto.request.*;
import org.example.backend.service.AiIntegrationService;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationServiceImpl implements AiIntegrationService {

    private final AiServiceClient aiServiceClient;

    @Override
    public Map<String, Object> getAiHealth() {
        try {
            return aiServiceClient.getHealth();
        } catch (WebClientResponseException e) {
            log.error("AI health check failed: {}", e.getMessage());
            return Map.of("status", "unreachable", "error", e.getMessage());
        } catch (Exception e) {
            log.error("AI health check error: {}", e.getMessage());
            return Map.of("status", "error", "error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getModelInfo() {
        try {
            return aiServiceClient.getModelInfo();
        } catch (Exception e) {
            log.error("AI model-info failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> predictDemand(AiForecastRequest request) {
        try {
            return aiServiceClient.predict(request);
        } catch (WebClientResponseException e) {
            log.error("AI predict failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI prediction failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI predict error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> generateForecast(AiForecastGenerateRequest request) {
        try {
            return aiServiceClient.generateForecast(request);
        } catch (WebClientResponseException e) {
            log.error("AI generate-forecast failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI forecast generation failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI generate-forecast error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> assignStorage(AiStorageRequest request) {
        try {
            return aiServiceClient.assignStorage(request);
        } catch (WebClientResponseException e) {
            log.error("AI assign-storage failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI storage assignment failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI assign-storage error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> optimizePicking(AiPickingRequest request) {
        try {
            return aiServiceClient.optimizePicking(request);
        } catch (WebClientResponseException e) {
            log.error("AI optimize-picking failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI picking optimization failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI optimize-picking error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> simulate(AiSimulationRequest request) {
        try {
            return aiServiceClient.simulate(request);
        } catch (WebClientResponseException e) {
            log.error("AI simulate failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI simulation failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI simulate error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> explain(AiExplainRequest request) {
        try {
            return aiServiceClient.explain(request);
        } catch (WebClientResponseException e) {
            log.error("AI explain failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI explainability failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI explain error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> preparationOrder(AiPreparationOrderRequest request) {
        try {
            return aiServiceClient.preparationOrder(request);
        } catch (WebClientResponseException e) {
            log.error("AI preparation-order failed [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI preparation order failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI preparation-order error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getWarehouseState() {
        try {
            return aiServiceClient.getWarehouseState();
        } catch (Exception e) {
            log.error("AI warehouse-state error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> resetWarehouse() {
        try {
            return aiServiceClient.resetWarehouse();
        } catch (Exception e) {
            log.error("AI reset-warehouse error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable: " + e.getMessage(), e);
        }
    }
}
