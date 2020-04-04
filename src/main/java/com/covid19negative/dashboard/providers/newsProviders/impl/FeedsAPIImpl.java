package com.covid19negative.dashboard.providers.newsProviders.impl;

import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import com.covid19negative.dashboard.service.processor.QueryProcessor;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.DateUtils;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
@Slf4j
public class FeedsAPIImpl extends AbstractNewsProvider {

    private final static String FEEDS_CACHE_KEY = "verified-covid19-bulletin";

    private final String RSS_FEED_SOURCE = "rss-feed-source";

    private static final Map<String, String> rssFeedsMap;

    static {
        Map<String, String> feedMap = new HashMap<>();
        feedMap.put("WHO", "https://www.who.int/feeds/entity/csr/don/en/rss.xml");
        feedMap.put("Government Of India", "https://services.india.gov.in/feed/rss?cat_id=5&ln=en");
        feedMap.put("CDC", "https://tools.cdc.gov/api/v2/resources/media/403372.rss");
        feedMap.put("United Nations", "https://news.un.org/feed/subscribe/en/news/topic/health/feed/rss.xml");
        feedMap.put("CIDRAP", "https://www.cidrap.umn.edu/news/178636/rss");
        feedMap.put("FDA", "https://www.fda.gov/about-fda/contact-fda/stay-informed/rss-feeds/press-releases/rss.xml");
        rssFeedsMap = Collections.unmodifiableMap(feedMap);
    }

    @Autowired
    private RedisTemplate redisDefaultTemplate;

    @Override
    public boolean supports(NewsCriteria newsForm) {
        return newsForm.getType().equalsIgnoreCase(NewsCriteria.newsTypes.VERIFIED.toString());
    }

    @Override
    public List<NewsItem> fetchBulletins(NewsCriteria newsForm) {
        List<NewsItem> newsItems = new ArrayList<>();
        ValueOperations valueOperations = redisDefaultTemplate.opsForValue();
        Object itemVal = valueOperations.get(FEEDS_CACHE_KEY);
        if (!CollectionUtils.isEmpty((List<NewsItem>) itemVal)) {
            return (List<NewsItem>) itemVal;
        }
        QueryProcessor processor = getQueryProcessor(RSS_FEED_SOURCE);
        List<Pattern> patterns = processor != null ? processor.getsProcessedQuery(newsForm.getQuery()) : null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        rssFeedsMap.forEach((feedSource, feedLink) -> {
            log.debug("Fetched feeds api url {} for verified news from {}", feedLink, feedSource);
            try {
                URL feedUrl = new URL(feedLink);
                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));
                for (SyndEntry entry : (List<SyndEntry>) feed.getEntries()) {
                    if (isDesiredNewsPresent(date, entry, patterns)) {
                        NewsItem item = new NewsItem();
                        item.setSource(feedSource);
                        item.setTitle(entry.getTitle());
                        item.setDescription(entry.getDescription().getValue());
                        item.setUrl(entry.getLink());
                        item.setPublishedAt(formatter.format(entry.getPublishedDate()));
                        newsItems.add(item);
                        if (newsItems.size() >= newsForm.getPageSize())
                            break;
                    }
                }
            } catch (IOException | FeedException e) {
                log.error(String.format("Error while fetching news from feed source %s", feedSource), e);
                throw new InternalServerError(String.format("Error while fetching news from feed source %s", feedSource), e);
            }
        });
        Collections.shuffle(newsItems);
        valueOperations.setIfAbsent(FEEDS_CACHE_KEY, newsItems,
                30, TimeUnit.MINUTES);
        return newsItems;
    }

    private boolean isDesiredNewsPresent(Date date, SyndEntry entry, List<Pattern> patterns) {
        SimpleDateFormat input = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
        try {
            String d1 = output.format(input.parse(date.toString()));
            String d2 = output.format(input.parse(entry.getPublishedDate().toString()));
            return patterns.stream().anyMatch(p -> d1.compareTo(d2) == 0 && ((!StringUtils.isEmpty(entry.getTitle()) && p.matcher(entry.getTitle().toLowerCase()).matches()) || (!StringUtils.isEmpty(entry.getDescription().getValue()) && p.matcher(entry.getDescription().getValue().toLowerCase()).matches())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

}