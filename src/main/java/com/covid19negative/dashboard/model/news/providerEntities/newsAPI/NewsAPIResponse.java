package com.covid19negative.dashboard.model.news.providerEntities.newsAPI;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NewsAPIResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String status;

    private String code;

    private String message;

    private int totalResults;

    private List<Article> articles;
}
