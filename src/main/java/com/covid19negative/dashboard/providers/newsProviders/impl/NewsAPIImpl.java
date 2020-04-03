package com.covid19negative.dashboard.providers.newsProviders.impl;

import com.covid19negative.common.configuration.CovidDataProperties;
import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.NewsAPIResponse;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NewsAPIImpl implements INewsProvider {

    private final String REDIS_KEY = "covid-bulletin";

    @Autowired
    private RedisTemplate redisDefaultTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CovidDataProperties covidDataProperties;

    public List<NewsItem> fetchBulletins(NewsCriteria newsForm) {
        List<NewsItem> newsItems = new ArrayList<>();
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        Object itemVal = valueOperations.get(REDIS_KEY);
        if (!CollectionUtils.isEmpty((List<NewsItem>) itemVal)) {
            return (List<NewsItem>) itemVal;
        }
        String newsApiUrl = covidDataProperties.getNewsApiUrl();
        log.debug("Fetched news api url {}", newsApiUrl);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(newsApiUrl)
                .queryParam("q", newsForm.getQuery())
                .queryParam("pageSize", newsForm.getPageSize())
                .queryParam("page", newsForm.getPage())
                .queryParam("apiKey", "ade457f15bd0400fb28161d5b92c9101");
        ResponseEntity<NewsAPIResponse> response = restTemplate.getForEntity(builder.toUriString(), NewsAPIResponse.class);
        HttpStatus statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new InternalServerError(String.format("Error while fetching real time news with error code %s", statusCode.value()));
        }
        NewsAPIResponse newsRespo = response.getBody();
        newsRespo.getArticles().stream().forEach(article -> {
            NewsItem item = new NewsItem();
            item.setSource(article.getArticleSource() != null ? article.getArticleSource().getName() : "");
            item.setAuthor(article.getAuthor());
            item.setTitle(article.getTitle());
            item.setDescription(article.getDescription());
            item.setUrl(article.getUrl());
            item.setUrlToImage(article.getUrlToImage());
            item.setPublishedAt(article.getPublishedAt());
            newsItems.add(item);
        });
        valueOperations.setIfAbsent(REDIS_KEY, newsItems,
                30, TimeUnit.MINUTES);
        return newsItems;
    }
}
