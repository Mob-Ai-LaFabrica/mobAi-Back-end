package org.example.backend.service;

import org.example.backend.dto.request.*;

import java.util.List;
import java.util.Map;

/**
 * Service layer for AI/ML integration.
 * Wraps calls to the external FastAPI AI service with error handling and
 * logging.
 */
public interface AiIntegrationService {

    // Health & Info
    Map<String, Object> getAiHealth();

    Map<String, Object> getModelInfo();

    // Forecasting
    List<Map<String, Object>> predictDemand(AiForecastRequest request);

    Map<String, Object> generateForecast(AiForecastGenerateRequest request);

    // Storage
    Map<String, Object> assignStorage(AiStorageRequest request);

    // Picking
    Map<String, Object> optimizePicking(AiPickingRequest request);

    // Simulation
    Map<String, Object> simulate(AiSimulationRequest request);

    // Explainability
    Map<String, Object> explain(AiExplainRequest request);

    // Preparation Order
    Map<String, Object> preparationOrder(AiPreparationOrderRequest request);

    // Warehouse State
    Map<String, Object> getWarehouseState();

    Map<String, Object> resetWarehouse();
}
