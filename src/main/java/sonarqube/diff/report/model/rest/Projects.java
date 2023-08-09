package sonarqube.diff.report.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Projects(Paging paging, List<Component> components) {
}
