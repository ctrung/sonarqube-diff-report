package sonarqube.diff.report.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "report")
public class ReportProps {

    /**
     * Report filename.
     */
    private String fileName;

    /**
     * Trend column label in the report.
     */
    private String labelTrend;

    /**
     * Project column label in the report.
     */
    private String labelProject;
}
