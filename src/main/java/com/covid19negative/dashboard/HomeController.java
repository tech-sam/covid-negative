package com.covid19negative.dashboard;

import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/v1")
public class HomeController {


    @Autowired
    private RedisTemplate redisDefaultTemplate;

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/")
    public String welcomeMsg() {
        return "Lets come together and fight against Novel Corona Virus ";
    }

    @PostMapping("/cache")
    public String putCacheSample() {
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        valueOperations.set("redis-test-sample-string", "Hi you have successfully added redis sample",
                2, TimeUnit.MINUTES);
        return "{}";
    }

    @GetMapping("/cache")
    public String getCacheSample() {
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        Object o = valueOperations.get("redis-test-sample-string");
        return o.toString();
    }

    @GetMapping("/statistics")
    public Statistics getCovidStatistics() {
       return dashboardService.getCovidStatistics();
    }
}
