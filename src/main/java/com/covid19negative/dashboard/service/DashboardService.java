package com.covid19negative.dashboard.service;

import com.covid19negative.common.configuration.CovidDataProperties;
import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.Article;
import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.NewsAPIResponse;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    private ApplicationContext appContext;

    List<INewsProvider> newsProviders;

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

    public News getNews(NewsCriteria newsForm) {
        try {
            List<INewsProvider> newsProviders = getNewsProviders();
            List<NewsItem> newsItems = new ArrayList<>();
            newsProviders.forEach(n -> newsItems.addAll(n.fetchBulletins(newsForm)));
            News news = new News();
            news.setNewsItems(newsItems);
            return news;
        } catch (Exception e) {
            String errorMessage = String.format("Error while fetching real time news for query %s", newsForm.getQuery());
            log.error(errorMessage, e);
            throw new InternalServerError(errorMessage);
        }
    }

    private List<INewsProvider> getNewsProviders() {
        if (CollectionUtils.isEmpty(newsProviders)) {
            newsProviders = new ArrayList<>(appContext
                    .getBeansOfType(INewsProvider.class).values());
        }
        return newsProviders;
    }
}
