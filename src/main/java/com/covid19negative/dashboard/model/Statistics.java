package com.covid19negative.dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Statistics implements Serializable {
    private List<DetailedReport> statewise;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetailedReport implements Serializable {
        private String state;
        private String active;
        private String confirmed;
        private String deaths;
        private String lastupdatedtime;
        private String recovered;
    }
}
