package io.github.tongbora.bakong.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bakong")
public class BakongProperties {

    private String accountId;
    private String acquiringBank;
    private String merchantName;
    private String mobileNumber;
    private String storeLabel;
    private String baseUrl;
    private String email;

    public String getAccountId() {
        return accountId;
    }

    public String getAcquiringBank() {
        return acquiringBank;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setAcquiringBank(String acquiringBank) {
        this.acquiringBank = acquiringBank;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setStoreLabel(String storeLabel) {
        this.storeLabel = storeLabel;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getStoreLabel() {
        return storeLabel;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getEmail() {
        return email;
    }
}