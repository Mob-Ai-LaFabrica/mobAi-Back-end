package org.example.backend.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.request.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Client for the MobAI AI/ML FastAPI service.
 * Endpoints: /predict, /assign-storage, /optimize-picking, /simulate,
 * /explain, /preparation-order, /generate-forecast,
 * /warehouse-state, /reset-warehouse, /health, /model-info
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiServiceClient {

    private final WebClient aiWebClient;

    // ======================== HEALTH & INFO ========================

    public Map<String, Object> getHealth() {
        return aiWebClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> getModelInfo() {
        return aiWebClient.get()
                .uri("/model-info")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> getApiInfo() {
        return aiWebClient.get()
                .uri("/api")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== FORECASTING ========================

    public List<Map<String, Object>> predict(AiForecastRequest request) {
        log.info("AI predict request: {} products for date {}", request.getProductIds().size(), request.getDate());
        return aiWebClient.post()
                .uri("/predict")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .map(list -> (List<Map<String, Object>>) (List<?>) list)
                .block();
    }

    public Map<String, Object> generateForecast(AiForecastGenerateRequest request) {
        log.info("AI generate-forecast: {} to {}", request.getStartDate(), request.getEndDate());
        return aiWebClient.post()
                .uri("/generate-forecast")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== STORAGE ========================

    public Map<String, Object> assignStorage(AiStorageRequest request) {
        log.info("AI assign-storage: product {} qty {}", request.getProductId(), request.getQuantity());
        return aiWebClient.post()
                .uri("/assign-storage")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== PICKING ========================

    public Map<String, Object> optimizePicking(AiPickingRequest request) {
        log.info("AI optimize-picking: {} items", request.getItems().size());
        return aiWebClient.post()
                .uri("/optimize-picking")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== SIMULATION ========================

    public Map<String, Object> simulate(AiSimulationRequest request) {
        log.info("AI simulate: {} events", request.getEvents().size());
        return aiWebClient.post()
                .uri("/simulate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== EXPLAINABILITY ========================

    public Map<String, Object> explain(AiExplainRequest request) {
        log.info("AI explain: product {} date {}", request.getProductId(), request.getDate());
        return aiWebClient.post()
                .uri("/explain")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== PREPARATION ORDER ========================

    public Map<String, Object> preparationOrder(AiPreparationOrderRequest request) {
        log.info("AI preparation-order: date {}", request.getDate());
        return aiWebClient.post()
                .uri("/preparation-order")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // ======================== WAREHOUSE STATE ========================

    public Map<String, Object> getWarehouseState() {
        return aiWebClient.get()
                .uri("/warehouse-state")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public Map<String, Object> resetWarehouse() {
        return aiWebClient.post()
                .uri("/reset-warehouse")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
