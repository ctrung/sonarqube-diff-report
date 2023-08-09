package sonarqube.diff.report.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Measure(String metric, String value) {
}
