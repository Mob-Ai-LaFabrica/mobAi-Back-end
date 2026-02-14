package org.example.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiForecastGenerateRequest {

    @JsonProperty("start_date")
    @Builder.Default
    private String startDate = "2026-01-09";

    @JsonProperty("end_date")
    @Builder.Default
    private String endDate = "2026-02-08";

    @JsonProperty("output_file")
    @Builder.Default
    private String outputFile = "forecast_submission.csv";
}
