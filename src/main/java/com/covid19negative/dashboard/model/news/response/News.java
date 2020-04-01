package com.covid19negative.dashboard.model.news.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class News implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<NewsItem> newsItems;
}
