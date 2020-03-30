package com.covid19negative.dashboard.service;

import com.covid19negative.common.configuration.CovidDataProperties;
import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.Statistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DashboardService {

    @Autowired
    private RedisTemplate redisDefaultTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CovidDataProperties covidDataProperties;

    public Statistics getCovidStatistics() {
        try {
            log.debug("Covid data url {}", covidDataProperties.getStatisticsUrl());
            ResponseEntity<Statistics> response = restTemplate.getForEntity(covidDataProperties.getStatisticsUrl(), Statistics
                    .class);
            HttpStatus statusCode = response.getStatusCode();
            if (!statusCode.is2xxSuccessful()) {
                throw new InternalServerError(String.format("Error fetching real time data with error code %s", statusCode.value()));
            }
            ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
            String key = "covid19-stats-india";
            valueOperations.setIfAbsent(key, response.getBody(),
                    2, TimeUnit.HOURS);
            return (Statistics) valueOperations.get(key);
        } catch (Exception e) {
            log.error("error getting statistics", e);
            throw e;
        }
    }
}
