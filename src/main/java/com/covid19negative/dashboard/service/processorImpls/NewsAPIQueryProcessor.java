package com.covid19negative.dashboard.service.processorImpls;

import com.covid19negative.common.exception.InternalServerError;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.service.processor.QueryProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static java.net.URLEncoder.encode;

@Component
@Slf4j
public class NewsAPIQueryProcessor implements QueryProcessor {

    private String NEWS_API_SOURCE = "news-api-source";

    @Override
    public boolean isSupported(String processSource) {
        return processSource.equalsIgnoreCase(NEWS_API_SOURCE);
    }

    @Override
    public <T> T getsProcessedQuery(List<String> rawQuery) {
        StringBuilder processedQuery = new StringBuilder();
        int lastIndex = rawQuery.size() - 1;
        try {
            rawQuery.forEach(q -> {
                processedQuery.append(q.toLowerCase());
                if (rawQuery.indexOf(q) != lastIndex) {
                    processedQuery.append(" OR ");
                }
            });
            return (T) encode(processedQuery.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while fetching processed query for newsAPI.org", e);
            throw new InternalServerError("Error occurred while fetching processed query for newsAPI.org", e);
        }
    }
}
