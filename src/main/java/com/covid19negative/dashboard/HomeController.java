package com.covid19negative.dashboard;

import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.NewsAPIResponse;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

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
        return "Let's come together to fight against Novel Corona Virus ";
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

    @PostMapping("/news")
    public News fetchNews(@RequestBody NewsCriteria newsForm) {
        try {
            return dashboardService.getNews(newsForm);
        } catch (Exception e) {
            throw new InternalServerError(String.format("Error occurred while fetching %s news", newsForm.getType()), e);
        }
    }
}
