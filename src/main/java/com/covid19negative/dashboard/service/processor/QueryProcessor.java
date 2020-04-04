package com.covid19negative.dashboard.service.processor;

import com.covid19negative.dashboard.model.news.request.NewsCriteria;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface QueryProcessor {

    public <T extends Object> T getsProcessedQuery(List<String> rawQuery) ;

    public boolean isSupported(String processSource);
}
