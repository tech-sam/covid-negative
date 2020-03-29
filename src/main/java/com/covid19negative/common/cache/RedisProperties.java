package com.covid19negative.common.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(value = "redis.default")
@Getter
@Setter
public class RedisProperties {

    private String host;
    private int port;
    private String password;

}
