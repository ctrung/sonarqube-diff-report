package sonarqube.diff.report.model;

import lombok.Data;

@Data
public class Diff {

    public Diff(String projectKey) {
        this.projectKey = projectKey;
    }

    private String projectKey;
    private String projectNameOld;
    private String projectNameNew;

    private Integer blockingViolationsOld;
    private Integer blockingViolationsNew;

    private Integer criticalViolationsOld;
    private Integer criticalViolationsNew;

    private Integer majorViolationsOld;
    private Integer majorViolationsNew;

    private Integer infoViolationsOld;
    private Integer infoViolationsNew;

    private Integer minorViolationsOld;
    private Integer minorViolationsNew;

    private Integer violationsOld;
    private Integer violationsNew;
}
