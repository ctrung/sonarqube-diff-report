package sonarqube.diff.report.util;

import sonarqube.diff.report.properties.SqProps;

public class SonarqubeUtil {

    private static final String SONARQUBE_API_PROJECTS_EP    = "/api/projects/search?ps=";
    private static final String SONARQUBE_API_METRICS_EP     = "/api/measures/component?component=__COMPONENT__&metricKeys=__METRIC_KEYS__";

    private SonarqubeUtil() {
        // util
    }

    public static String getProjectsEpUrl(SqProps.Instance inst) {
        return inst.getHostUrl() + SONARQUBE_API_PROJECTS_EP + inst.getProjectsApiPageSize();
    }

    public static String getMetricsEpUrl(SqProps.Instance inst, String comp, String metricKeys) {
        return inst.getHostUrl() + SONARQUBE_API_METRICS_EP
                .replace("__COMPONENT__", comp)
                .replace("__METRIC_KEYS__", metricKeys);
    }
}
