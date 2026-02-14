package org.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.request.*;
import org.example.backend.dto.response.ApiResponse;
import org.example.backend.service.AiIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller that proxies all AI/ML service endpoints through the Spring Boot
 * backend.
 * The frontend only needs to talk to this backend — all AI calls are handled
 * server-to-server.
 *
 * AI Service: http://4.251.194.25:8000
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Service", description = "AI/ML forecasting, storage optimization, picking & simulation endpoints")
public class AiController {

    private final AiIntegrationService aiIntegrationService;

    // ======================== HEALTH & STATUS ========================

    @GetMapping("/health")
    @Operation(summary = "AI service health check", description = "Check if the AI service is running and get model status")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> aiHealth() {
        Map<String, Object> health = aiIntegrationService.getAiHealth();
        return ResponseEntity.ok(ApiResponse.success(health, "AI service health status"));
    }

    @GetMapping("/model-info")
    @Operation(summary = "AI model information", description = "Get full model documentation, assumptions, and performance metrics")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> modelInfo() {
        Map<String, Object> info = aiIntegrationService.getModelInfo();
        return ResponseEntity.ok(ApiResponse.success(info, "AI model information retrieved"));
    }

    // ======================== FORECASTING ========================

    @PostMapping("/predict")
    @Operation(summary = "Predict demand", description = "Predict demand for given products on a specific date using XGBoost + Prophet ensemble")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> predict(@RequestBody AiForecastRequest request) {
        List<Map<String, Object>> predictions = aiIntegrationService.predictDemand(request);
        return ResponseEntity.ok(ApiResponse.success(predictions,
                "Demand predictions for " + request.getProductIds().size() + " products"));
    }

    @PostMapping("/generate-forecast")
    @Operation(summary = "Generate forecast CSV", description = "Generate bulk demand forecast for all products over a date range")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateForecast(
            @RequestBody AiForecastGenerateRequest request) {
        Map<String, Object> result = aiIntegrationService.generateForecast(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Forecast generated successfully"));
    }

    // ======================== STORAGE OPTIMIZATION ========================

    @PostMapping("/assign-storage")
    @Operation(summary = "AI storage assignment", description = "Get AI-optimized storage location for a product based on demand frequency and warehouse layout")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignStorage(@RequestBody AiStorageRequest request) {
        Map<String, Object> result = aiIntegrationService.assignStorage(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Storage location assigned by AI"));
    }

    // ======================== PICKING OPTIMIZATION ========================

    @PostMapping("/optimize-picking")
    @Operation(summary = "Optimize picking route", description = "Multi-chariot picking optimization with nearest-neighbor routing and congestion detection")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> optimizePicking(@RequestBody AiPickingRequest request) {
        Map<String, Object> result = aiIntegrationService.optimizePicking(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Picking route optimized by AI"));
    }

    // ======================== SIMULATION ========================

    @PostMapping("/simulate")
    @Operation(summary = "Simulate warehouse operations", description = "Process chronological ingoing/outgoing events with AI-driven storage and routing")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> simulate(@RequestBody AiSimulationRequest request) {
        Map<String, Object> result = aiIntegrationService.simulate(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Simulation completed"));
    }

    // ======================== EXPLAINABILITY ========================

    @PostMapping("/explain")
    @Operation(summary = "Explain AI prediction", description = "Get detailed explanation of a forecast prediction with feature importance and reasoning")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> explain(@RequestBody AiExplainRequest request) {
        Map<String, Object> result = aiIntegrationService.explain(request);
        return ResponseEntity.ok(ApiResponse.success(result, "AI prediction explained"));
    }

    // ======================== PREPARATION ORDER ========================

    @PostMapping("/preparation-order")
    @Operation(summary = "Generate preparation order", description = "Generate a preparation order based on forecasted demand with optional manual overrides")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> preparationOrder(
            @RequestBody AiPreparationOrderRequest request) {
        Map<String, Object> result = aiIntegrationService.preparationOrder(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Preparation order generated"));
    }

    // ======================== WAREHOUSE STATE (AI-side) ========================

    @GetMapping("/warehouse-state")
    @Operation(summary = "AI warehouse state", description = "Get current warehouse occupancy and stored products from the AI engine")
    @PreAuthorize("hasAuthority('operation:read')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> warehouseState() {
        Map<String, Object> state = aiIntegrationService.getWarehouseState();
        return ResponseEntity.ok(ApiResponse.success(state, "AI warehouse state retrieved"));
    }

    @PostMapping("/reset-warehouse")
    @Operation(summary = "Reset AI warehouse state", description = "Reset the AI engine's in-memory warehouse state to empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetWarehouse() {
        Map<String, Object> result = aiIntegrationService.resetWarehouse();
        return ResponseEntity.ok(ApiResponse.success(result, "AI warehouse state reset"));
    }
}
