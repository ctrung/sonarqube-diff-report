package sonarqube.diff.report;

import sonarqube.diff.report.properties.SqProps;
import sonarqube.diff.report.service.AppService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SonarqubeDiffReport {

    public static void main(String[] args) {
        SpringApplication.run(SonarqubeDiffReport.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, SqProps sonarqubeProps) {
        return builder.basicAuthentication(sonarqubeProps.getToken(), "").build();
    }

    @Bean
    public CommandLineRunner run(AppService appService) {
        return args -> appService.generateReport();
    }

}
