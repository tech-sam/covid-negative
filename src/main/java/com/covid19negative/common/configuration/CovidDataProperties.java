package com.covid19negative.common.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "covid.data")
@Getter
@Setter
public class CovidDataProperties {
    private String statisticsUrl;
    private String newsApiUrl;
    private String globalStatsUrl;
    private String globalSummaryUrl;
}
