package io.github.tongbora.bakong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bakong")
public class BakongProperties {

    private String accountId;
    private String baseUrl;
    private String email;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getEmail() {
        return email;
    }
}