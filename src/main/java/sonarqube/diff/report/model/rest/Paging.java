package sonarqube.diff.report.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Paging(int pageIndex, int pageSize, int total) {
}
