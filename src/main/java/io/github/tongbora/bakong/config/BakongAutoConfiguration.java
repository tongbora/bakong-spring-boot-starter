package io.github.tongbora.bakong.config;

import org.springframework.beans.factory.annotation.Qualifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tongbora.bakong.service.BakongService;
import io.github.tongbora.bakong.service.BakongTokenService;
import io.github.tongbora.bakong.service.impl.BakongServiceImpl;
import io.github.tongbora.bakong.service.impl.BakongTokenServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(BakongProperties.class)
public class BakongAutoConfiguration {

    @Bean("bakongRestTemplate")
    @ConditionalOnMissingBean(name = "bakongRestTemplate")
    public RestTemplate bakongRestTemplate() {
        return new RestTemplate();
    }

    @Bean("bakongObjectMapper")
    @ConditionalOnMissingBean(name = "bakongObjectMapper")
    public ObjectMapper bakongObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public BakongTokenService bakongTokenService(
            @Qualifier("bakongRestTemplate") RestTemplate bakongRestTemplate,
            @Qualifier("bakongObjectMapper") ObjectMapper bakongObjectMapper,
            BakongProperties properties
    ) {
        return new BakongTokenServiceImpl(
                bakongRestTemplate,
                bakongObjectMapper,
                properties
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public BakongService bakongService(
            BakongTokenService bakongTokenService,
            @Qualifier("bakongRestTemplate") RestTemplate bakongRestTemplate,
            @Qualifier("bakongObjectMapper") ObjectMapper bakongObjectMapper,
            BakongProperties properties
    ) {
        return new BakongServiceImpl(
                bakongTokenService,
                bakongRestTemplate,
                bakongObjectMapper,
                properties
        );
    }
}