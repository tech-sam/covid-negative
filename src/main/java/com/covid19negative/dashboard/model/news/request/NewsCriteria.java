package com.covid19negative.dashboard.model.news.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class NewsCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private String query;
    private int pageSize;
    private int page;
}
