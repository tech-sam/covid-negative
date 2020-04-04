package com.covid19negative.dashboard.service.processorImpls;

import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.service.processor.QueryProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class RSSFeedsQueryProcessor implements QueryProcessor {

    private final static String RSS_FEED_SOURCE = "rss-feed-source";

    private final static String ANY_CHARACTER = ".*";

    @Override
    public boolean isSupported(String processSource) {
        return processSource.equalsIgnoreCase(RSS_FEED_SOURCE);
    }

    @Override
    public <T> T getsProcessedQuery(List<String> rawQuery) {
        final List<Pattern> patterns = new ArrayList<>();
        rawQuery.forEach(q -> {
            patterns.add(Pattern.compile(ANY_CHARACTER.concat(q.toLowerCase()).concat(ANY_CHARACTER)));
        });
        return (T) patterns;
    }

}
