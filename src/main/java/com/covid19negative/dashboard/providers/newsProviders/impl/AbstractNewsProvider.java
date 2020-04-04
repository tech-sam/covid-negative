package com.covid19negative.dashboard.providers.newsProviders.impl;

import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.NewsItem;
import com.covid19negative.dashboard.providers.newsProviders.INewsProvider;
import com.covid19negative.dashboard.service.processor.QueryProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.management.Query;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public abstract class AbstractNewsProvider implements INewsProvider {

    @Autowired
    private ApplicationContext appContext;

    private List<QueryProcessor> queryProcessors;

    private Map<String, QueryProcessor> queryProcessorsMap = new HashMap<>();

    public QueryProcessor getQueryProcessor(String processSource) {
        if (!queryProcessorsMap.containsKey(processSource)) {
            if (CollectionUtils.isEmpty(queryProcessors)) {
                queryProcessors = new ArrayList<>(appContext
                        .getBeansOfType(QueryProcessor.class).values());
            }
            Optional<QueryProcessor> mayBeProcessor = queryProcessors.stream().filter(processor -> processor.isSupported(processSource)).findFirst();
            if (mayBeProcessor.isPresent())
                queryProcessorsMap.put(processSource, mayBeProcessor.get());
        }
        return queryProcessorsMap.get(processSource);
    }

}
