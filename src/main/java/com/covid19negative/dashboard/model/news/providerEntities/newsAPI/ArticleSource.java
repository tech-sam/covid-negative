package com.covid19negative.dashboard.model.news.providerEntities.newsAPI;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ArticleSource implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;
}
