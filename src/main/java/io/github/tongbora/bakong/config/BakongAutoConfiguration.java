package io.github.tongbora.bakong.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tongbora.bakong.service.BakongService;
import io.github.tongbora.bakong.service.BakongTokenService;
import io.github.tongbora.bakong.service.impl.BakongServiceImpl;
import io.github.tongbora.bakong.service.impl.BakongTokenServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(BakongProperties.class)
public class BakongAutoConfiguration {

    @Bean("bakongRestClient")
    @ConditionalOnMissingBean(name = "bakongRestClient")
    public RestClient bakongRestClient() {
        return RestClient.create();
    }

    @Bean("bakongObjectMapper")
    @ConditionalOnMissingBean(name = "bakongObjectMapper")
    public ObjectMapper bakongObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public BakongTokenService bakongTokenService(
            @Qualifier("bakongRestClient") RestClient bakongRestClient,
            @Qualifier("bakongObjectMapper") ObjectMapper bakongObjectMapper,
            BakongProperties properties
    ) {
        return new BakongTokenServiceImpl(bakongRestClient, bakongObjectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public BakongService bakongService(
            BakongTokenService bakongTokenService,
            @Qualifier("bakongRestClient") RestClient bakongRestClient,
            @Qualifier("bakongObjectMapper") ObjectMapper bakongObjectMapper,
            BakongProperties properties
    ) {
        return new BakongServiceImpl(bakongTokenService, bakongRestClient, bakongObjectMapper, properties);
    }
}