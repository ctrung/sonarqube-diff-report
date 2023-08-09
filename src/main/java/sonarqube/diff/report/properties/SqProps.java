package sonarqube.diff.report.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sonarqube")
public class SqProps {

    /**
     * Common token for SonarQube instances.
     */
    private String token;

    /**
     * First SonarQube instance.
     */
    private Instance firstInstance;

    /**
     * Second SonarQube instance.
     */
    private Instance secondInstance;

    @Data
    public static class Instance {

        /**
         * SonarQube label (short). Appears in logs and report column.
         */
        private String label;

        /**
         * SonarQube host URL for REST API calls.
         */
        private String hostUrl;

        /**
         * SonarQube REST API /api/projects/search pageSize param.
         */
        private int projectsApiPageSize;
    }
}
