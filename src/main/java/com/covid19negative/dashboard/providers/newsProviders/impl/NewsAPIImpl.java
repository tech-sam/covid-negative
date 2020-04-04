package com.covid19negative.dashboard.providers.newsProviders.impl;

import com.covid19negative.common.configuration.CovidDataProperties;
import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.Statistics;
import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.NewsAPIResponse;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import com.covid19negative.dashboard.service.processor.QueryProcessor;
import com.rometools.rome.io.FeedException;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;

@Component
@Slf4j
public class NewsAPIImpl extends AbstractNewsProvider {

    private final String NEWS_API_CACHE_KEY = "covid-bulletin";

    private final String NEWS_API_SOURCE = "news-api-source";

    @Autowired
    private RedisTemplate redisDefaultTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CovidDataProperties covidDataProperties;

    @Override
    public boolean supports(NewsCriteria newsForm) {
        return newsForm.getType().equalsIgnoreCase(NewsCriteria.newsTypes.ANY.toString());
    }

    public List<NewsItem> fetchBulletins(NewsCriteria newsForm) {
        List<NewsItem> newsItems = new ArrayList<>();
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        Object itemVal = valueOperations.get(NEWS_API_CACHE_KEY);
        if (!CollectionUtils.isEmpty((List<NewsItem>) itemVal)) {
            return (List<NewsItem>) itemVal;
        }
        try {
            String newsApiUrl = covidDataProperties.getNewsApiUrl();
            log.debug("Fetched news api url {}", newsApiUrl);
            QueryProcessor processor = getQueryProcessor(NEWS_API_SOURCE);
            String processedQuery = processor != null ? (String) processor.getsProcessedQuery(newsForm.getQuery()) : "";
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(newsApiUrl)
                    .queryParam("q", processedQuery)
                    .queryParam("pageSize", newsForm.getPageSize())
                    .queryParam("page", newsForm.getPage())
                    .queryParam("apiKey", "ade457f15bd0400fb28161d5b92c9101");
            ResponseEntity<NewsAPIResponse> response = restTemplate.getForEntity(builder.toUriString(), NewsAPIResponse.class);
            HttpStatus statusCode = response.getStatusCode();
            if (!statusCode.is2xxSuccessful()) {
                throw new InternalServerError(String.format("Error while fetching real time news with error code %s", statusCode.value()));
            }
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            NewsAPIResponse newsRepo = response.getBody();
            newsRepo.getArticles().stream().forEach(article -> {
                NewsItem item = new NewsItem();
                item.setSource(article.getArticleSource() != null ? article.getArticleSource().getName() : "");
                item.setAuthor(article.getAuthor());
                item.setTitle(article.getTitle());
                item.setDescription(article.getDescription());
                item.setUrl(article.getUrl());
                item.setUrlToImage(article.getUrlToImage());
                try {
                    Date date = input.parse(article.getPublishedAt());
                    item.setPublishedAt(output.format(date));
                } catch (ParseException e) {
                    throw new InternalServerError("Error occurred while parsing published date to fetch real time news", e);
                }
                newsItems.add(item);

            });
        } catch (Exception e) {
            log.error("Error occurred while fetching news from newsAPI", e);
            throw new InternalServerError("Error occurred while fetching news from newsAPI", e);
        }
        valueOperations.setIfAbsent(NEWS_API_CACHE_KEY, newsItems,
                30, TimeUnit.MINUTES);
        return newsItems;
    }
}
