package com.covid19negative.dashboard.service;

import com.covid19negative.common.configuration.CovidDataProperties;
import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.GlobalSummary;
import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import io.github.nandandesai.twitterscraper4j.TwitterScraper;
import io.github.nandandesai.twitterscraper4j.models.Tweet;
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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private Map<String, List<INewsProvider>> newsProvidersMap = new HashMap<>();

    List<INewsProvider> newsProviders;

    public Statistics getCovidStatistics() {
        try {
            ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
            String key = "covid19-stats-india";
            Object data = valueOperations.get(key);
            if (data != null) {
                return (Statistics) data;
            }
            valueOperations.setIfAbsent(key, getCovidStatisticsResponse().getBody(),
                    2, TimeUnit.HOURS);
            return (Statistics) valueOperations.get(key);
        } catch (Exception e) {
            log.error("error getting statistics", e);
            throw e;
        }
    }

    private ResponseEntity<Statistics> getCovidStatisticsResponse() {
        String statisticsUrl = covidDataProperties.getStatisticsUrl();
        log.debug("Covid data url {}", statisticsUrl);
        ResponseEntity<Statistics> response = restTemplate.getForEntity(statisticsUrl, Statistics
                .class);
        HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new InternalServerError(String.format("Error fetching real time data with error code %s", statusCode.value()));
        }
        return response;
    }

    public News getNews(NewsCriteria newsForm) {
        try {
            List<INewsProvider> newsProviders = getNewsProviders(newsForm);
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

    private List<INewsProvider> getNewsProviders(NewsCriteria newsForm) {
        if (!newsProvidersMap.containsKey(newsForm.getType())) {
            if (CollectionUtils.isEmpty(newsProviders)) {
                newsProviders = new ArrayList<>(appContext
                        .getBeansOfType(INewsProvider.class).values());
            }
            List<INewsProvider> filteredNewsProviders = newsProviders.stream().filter(newsProvider -> newsProvider.supports(newsForm)).collect(Collectors.toList());
            newsProvidersMap.put(newsForm.getType(), filteredNewsProviders);
        }
        return newsProvidersMap.get(newsForm.getType());
    }

    public Statistics.Global getGlobalCovidStatistics() {
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        String key = "covid19-global-stats";
        Object data = valueOperations.get(key);
        if (data != null) {
            return (Statistics.Global) data;
        }
        valueOperations.setIfAbsent(key, getCovidGlobalResponse().getBody(),
                2, TimeUnit.HOURS);
        return (Statistics.Global) valueOperations.get(key);
    }

    private ResponseEntity<Statistics.Global> getCovidGlobalResponse() {
        String globalStatsUrl = covidDataProperties.getGlobalStatsUrl();
        log.debug("Covid global data url {} ", globalStatsUrl);
        ResponseEntity<Statistics.Global> response = restTemplate.getForEntity(globalStatsUrl, Statistics.Global.class);
        HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new InternalServerError(String.format("Error fetching real time data with error code %s", statusCode.value()));
        }
        return response;
    }

    public GlobalSummary getGlobalCovidSummary() {
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        String key = "covid19-global-summary";
        if(valueOperations.get(key) != null) {
            return (GlobalSummary) valueOperations.get(key);
        }
        valueOperations.setIfAbsent(key, getGlobalSummaryResponse().getBody(),
                2, TimeUnit.HOURS);
        return (GlobalSummary) valueOperations.get(key);
    }

    private ResponseEntity<GlobalSummary> getGlobalSummaryResponse() {
        String globalSummaryUrl = covidDataProperties.getGlobalSummaryUrl();
        log.debug("Covid global summary url {} ", globalSummaryUrl);
        ResponseEntity<GlobalSummary> response = restTemplate.getForEntity(globalSummaryUrl, GlobalSummary.class);
        HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new InternalServerError(String.format("Error fetching real time data with error code %s", statusCode.value()));
        }
        return response;
    }

    public List<Tweet>  getCovidTweets() {
        try {
            TwitterScraper scraper = TwitterScraper.builder().build();
            List<Tweet> covid = scraper.searchTweetsWithHashtag("IndiaFightsCorona");
            List<Tweet> userTimeline = scraper.getUserTimeline("@WHO");
            List<Tweet> ministryOfHealth = scraper.getUserTimeline("@MoHFW_INDIA");
            covid.addAll(userTimeline);
            covid.addAll(ministryOfHealth);
            return covid;
        } catch (Exception e) {
            throw new InternalServerError("",e);
        }
    }
}
