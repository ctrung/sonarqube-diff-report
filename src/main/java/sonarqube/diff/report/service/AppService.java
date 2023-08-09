package sonarqube.diff.report.service;

import sonarqube.diff.report.model.Diff;
import sonarqube.diff.report.model.rest.Component;
import sonarqube.diff.report.model.rest.Measure;
import sonarqube.diff.report.model.rest.Metrics;
import sonarqube.diff.report.model.rest.Projects;
import sonarqube.diff.report.properties.ReportProps;
import sonarqube.diff.report.properties.SqProps;
import sonarqube.diff.report.util.PredefinedColor;
import sonarqube.diff.report.util.SonarqubeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static sonarqube.diff.report.util.PoiUtil.createCell;
import static sonarqube.diff.report.util.PredefinedColor.*;

@Slf4j
@Service
public class AppService {

    private static final String BLOCKING_VIOLATIONS_KEY  = "blocker_violations";
    private static final String CRITICAL_VIOLATIONS_KEY  = "critical_violations";
    private static final String MAJOR_VIOLATIONS_KEY     = "major_violations";
    private static final String INFO_VIOLATIONS_KEY      = "info_violations";
    private static final String MINOR_VIOLATIONS_KEY     = "minor_violations";
    private static final String VIOLATIONS_KEY           = "violations";
    private static final String LAST_COMMIT_DATE_KEY     = "last_commit_date";
    private static final String METRIC_KEYS = BLOCKING_VIOLATIONS_KEY + "," + CRITICAL_VIOLATIONS_KEY + "," + MAJOR_VIOLATIONS_KEY + "," + INFO_VIOLATIONS_KEY + "," + MINOR_VIOLATIONS_KEY + "," + VIOLATIONS_KEY + "," + LAST_COMMIT_DATE_KEY;

    private static final String DOWNWARDS_ARROW     = "_x2193_";
    private static final String RIGHTWARDS_ARROW    = "_x2192_";
    private static final String UPWARDS_ARROW       = "_x2191_";

    private final RestTemplate restTemplate;
    private final SqProps sqProps;
    private final ReportProps reportProps;

    public AppService(RestTemplate restTemplate, SqProps sqProps, ReportProps reportProps) {
        this.restTemplate = restTemplate;
        this.sqProps = sqProps;
        this.reportProps = reportProps;
    }

    public void generateReport() throws IOException {
        
        // Retrieve projects
        Projects oldProjects = restTemplate.getForObject(SonarqubeUtil.getProjectsEpUrl(sqProps.getFirstInstance()), Projects.class);
        Projects newProjects = restTemplate.getForObject(SonarqubeUtil.getProjectsEpUrl(sqProps.getSecondInstance()), Projects.class);

        // Crunch data
        var data = new LinkedHashMap<String, Diff>();

        retrieveAndMergeData(sqProps.getFirstInstance().getLabel(), oldProjects, data,
                (diff, comp) -> {
            diff.setProjectNameOld(comp.name());
            populateOldMetrics(restTemplate, diff);
        });

        retrieveAndMergeData(sqProps.getSecondInstance().getLabel(), newProjects, data,
                (diff, comp) -> {
            diff.setProjectNameNew(comp.name());
            populateNewMetrics(restTemplate, diff);
        });

        // Generate report
        generateReport(data);
    }

    private void retrieveAndMergeData(String labelSonarqube1, Projects projects, Map<String, Diff> data, BiConsumer<Diff, Component> biConsumer) {

        if(projects == null) {
            log.warn("No projects available for SonarQube " + labelSonarqube1 + ", can't retrieve metrics.");
            return;
        }
        log.info("Retrieving SonarQube " + labelSonarqube1 + " metrics");

        int i = 1;
        for (Component comp : projects.components()) {
            if (i%5 == 0) {
                log.info(i + "/" + projects.components().size() + "...");
            }
            i++;
            Diff diff = data.computeIfAbsent(comp.key(), Diff::new);
            biConsumer.accept(diff, comp);
        }
    }

    private void generateReport(LinkedHashMap<String, Diff> data) throws IOException {

        if(data.isEmpty()) {
            log.error("No data to crunch for SonarQube " + sqProps.getFirstInstance().getLabel() + " and " + sqProps.getSecondInstance().getLabel() + ", no report file will be generated.");
            return;
        }
        log.info("Generating report file...");

        List<Diff> values = List.copyOf(data.values());


        // Write to file
        try (OutputStream fileOut = new FileOutputStream(reportProps.getFileName());
             Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("diff");

            // headers

            Row row = sheet.createRow(0);
            row.createCell(0).setCellValue(reportProps.getLabelProject());
            createHeaderLine1Cells(wb, sheet, row, 1, BLOCKING_VIOLATIONS_KEY, LIGHT_YELLOW);
            createHeaderLine1Cells(wb, sheet, row, 4, CRITICAL_VIOLATIONS_KEY, LIGHT_GREY);
            createHeaderLine1Cells(wb, sheet, row, 7, MAJOR_VIOLATIONS_KEY, LIGHT_YELLOW);
            createHeaderLine1Cells(wb, sheet, row, 10, MINOR_VIOLATIONS_KEY, LIGHT_GREY);
            createHeaderLine1Cells(wb, sheet, row, 13, INFO_VIOLATIONS_KEY, LIGHT_YELLOW);
            createHeaderLine1Cells(wb, sheet, row, 16, VIOLATIONS_KEY, LEATHER_ORANGE);

            row = sheet.createRow(1);
            createHeaderLine2Cells(wb, row, 1, LIGHT_YELLOW);
            createHeaderLine2Cells(wb, row, 4, LIGHT_GREY);
            createHeaderLine2Cells(wb, row, 7, LIGHT_YELLOW);
            createHeaderLine2Cells(wb, row, 10, LIGHT_GREY);
            createHeaderLine2Cells(wb, row, 13, LIGHT_YELLOW);
            createHeaderLine2Cells(wb, row, 16, LEATHER_ORANGE);


            // lines

            for (int i = 0; i <values.size(); i++) {
                Diff diff = values.get(i);

                row = sheet.createRow(i+2);

                row.createCell(0).setCellValue(diff.getProjectNameOld() + " (" + diff.getProjectKey() + ")");

                createCells(wb, row, 1, diff.getBlockingViolationsOld(), diff.getBlockingViolationsNew(), LIGHT_YELLOW);
                createCells(wb, row, 4, diff.getCriticalViolationsOld(), diff.getCriticalViolationsNew(), LIGHT_GREY);
                createCells(wb, row, 7, diff.getMajorViolationsOld(), diff.getMajorViolationsNew(), LIGHT_YELLOW);
                createCells(wb, row, 10, diff.getMinorViolationsOld(), diff.getMinorViolationsNew(), LIGHT_GREY);
                createCells(wb, row, 13, diff.getInfoViolationsOld(), diff.getInfoViolationsNew(), LIGHT_YELLOW);
                createCells(wb, row, 16, diff.getViolationsOld(), diff.getViolationsNew(), LEATHER_ORANGE);
            }

            // resize all columns
            for (int i = 0; i < 19; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(fileOut);
        }

    }

    private void createHeaderLine1Cells(Workbook wb, Sheet sheet, Row row, int startIdx, String metricKey, PredefinedColor bgColor) {
        sheet.addMergedRegion(new CellRangeAddress(0, 0, startIdx, startIdx + 2));
        createCell(wb, row, startIdx, bgColor, metricKey);
    }

    private void createHeaderLine2Cells(Workbook wb, Row row, int startIdx, PredefinedColor bgColor) {
        createCell(wb, row, startIdx, bgColor, sqProps.getFirstInstance().getLabel());
        createCell(wb, row, startIdx + 1, bgColor, sqProps.getSecondInstance().getLabel());
        createCell(wb, row, startIdx + 2, bgColor, reportProps.getLabelTrend());
    }

    private void createCells(Workbook wb, Row row, int startIdx, Integer metricOld, Integer metricNew, PredefinedColor bgColor) {

        createCell(wb, row, startIdx, bgColor, metricOld);

        createCell(wb, row, startIdx + 1, bgColor, metricNew);

        String value            = null;
        PredefinedColor fgColor = null;
        boolean bold            = false;
        if (metricOld != null && metricNew != null) {
            if (metricOld < metricNew) {
                value   = UPWARDS_ARROW;
                fgColor = RED;
                bold    = true;
            } else if (metricOld.intValue() == metricNew.intValue()) {
                value = RIGHTWARDS_ARROW;
            } else {
                value = DOWNWARDS_ARROW;
                fgColor = GREEN;
                bold = true;
            }
        }
        createCell(wb, row, startIdx + 2, bgColor, fgColor, bold, value);
    }

    private void populateOldMetrics(RestTemplate restTemplate, Diff diff) {
        Metrics metrics = restTemplate.getForObject(SonarqubeUtil.getMetricsEpUrl(sqProps.getFirstInstance(), diff.getProjectKey(), METRIC_KEYS), Metrics.class);
        if(metrics == null || metrics.component() == null) return;
        Component comp = metrics.component();
        diff.setBlockingViolationsOld(getIntMetric(comp, BLOCKING_VIOLATIONS_KEY));
        diff.setCriticalViolationsOld(getIntMetric(comp, CRITICAL_VIOLATIONS_KEY));
        diff.setMajorViolationsOld(getIntMetric(comp, MAJOR_VIOLATIONS_KEY));
        diff.setInfoViolationsOld(getIntMetric(comp, INFO_VIOLATIONS_KEY));
        diff.setMinorViolationsOld(getIntMetric(comp, MINOR_VIOLATIONS_KEY));
        diff.setViolationsOld(getIntMetric(comp, VIOLATIONS_KEY));
    }

    private void populateNewMetrics(RestTemplate restTemplate, Diff diff) {
        Metrics metrics = restTemplate.getForObject(SonarqubeUtil.getMetricsEpUrl(sqProps.getSecondInstance(), diff.getProjectKey(), METRIC_KEYS), Metrics.class);
        if(metrics == null || metrics.component() == null) return;
        Component comp = metrics.component();
        diff.setBlockingViolationsNew(getIntMetric(comp, BLOCKING_VIOLATIONS_KEY));
        diff.setCriticalViolationsNew(getIntMetric(comp, CRITICAL_VIOLATIONS_KEY));
        diff.setMajorViolationsNew(getIntMetric(comp, MAJOR_VIOLATIONS_KEY));
        diff.setInfoViolationsNew(getIntMetric(comp, INFO_VIOLATIONS_KEY));
        diff.setMinorViolationsNew(getIntMetric(comp, MINOR_VIOLATIONS_KEY));
        diff.setViolationsNew(getIntMetric(comp, VIOLATIONS_KEY));
    }

    private Integer getIntMetric(Component comp, String key) {
        Optional<Measure> measure = comp.measures().stream().filter(m -> m.metric().equals(key)).findFirst();
        if(measure.isEmpty()) return null;
        return Integer.valueOf(measure.get().value());
    }
}
