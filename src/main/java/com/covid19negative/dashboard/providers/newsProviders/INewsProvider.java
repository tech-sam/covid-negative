package com.covid19negative.dashboard.providers.newsProviders;

import com.covid19negative.dashboard.model.news.providerEntities.newsAPI.NewsAPIResponse;
import com.covid19negative.dashboard.model.news.request.NewsCriteria;
import com.covid19negative.dashboard.model.news.response.News;
import com.covid19negative.dashboard.model.news.response.NewsItem;

import java.util.List;

public interface INewsProvider {

    List<NewsItem> fetchBulletins(NewsCriteria newsForm);

    boolean supports(NewsCriteria newsForm);
}
