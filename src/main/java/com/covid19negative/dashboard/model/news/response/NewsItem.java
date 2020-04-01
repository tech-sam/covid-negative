package com.covid19negative.dashboard.model.news.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class NewsItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String source;

    private String author;

    private String title;

    private String description;

    private String url;

    private String urlToImage;

    private String publishedAt;

}
