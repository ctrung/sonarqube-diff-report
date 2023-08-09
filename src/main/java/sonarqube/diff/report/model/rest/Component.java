package sonarqube.diff.report.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Component(String key, String name, ZonedDateTime lastAnalysisDate, List<Measure> measures) {
}
