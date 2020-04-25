package com.covid19negative.dashboard.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
// keys are same as response keys
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalSummary implements Serializable {

    @JsonProperty("Countries")
    private List<Country> countries;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country implements Serializable {
        @JsonProperty("Country")
        private String country;
        @JsonProperty("Slug")
        private String slug;
        @JsonProperty("NewConfirmed")
        private String newConfirmed;
        @JsonProperty("TotalConfirmed")
        private String totalConfirmed;
        @JsonProperty("NewDeaths")
        private String newDeaths;
        @JsonProperty("NewRecovered")
        private String newRecovered;
        @JsonProperty("TotalRecovered")
        private String totalRecovered;
    }
}
