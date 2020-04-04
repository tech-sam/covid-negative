package com.covid19negative.dashboard.model.news.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.tomcat.util.buf.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class NewsCriteria implements Serializable {

    private static final String ANY = "any";

    private static final String VERIFIED = "verified";

    public enum newsTypes {
        ANY, VERIFIED;
    }

    private static final long serialVersionUID = 1L;

    private List<String> query;
    private int pageSize;
    private int page;
    private String type;
}
