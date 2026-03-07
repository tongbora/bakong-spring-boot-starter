package io.github.tongbora.bakong.dto;
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;

public record BakongRequest(
        KHQRCurrency currency,
        Double amount,
        String merchantName,
        String merchantCity,
        String merchantId,
        String acquiringBank,
        String upiAccountInformation,
        Integer expirationTimestamp,
        String billNumber,
        String storeLabel,
        String terminalLabel,
        String mobileNumber,
        String purposeOfTransaction,
        String merchantAlternateLanguagePreference,
        String merchantNameAlternateLanguage,
        String merchantCityAlternateLanguage
) {

    public BakongRequest {
        currency = currency == null ? KHQRCurrency.KHR : currency;
        merchantName = merchantName == null ? "DEFAULT MERCHANT" : merchantName;
        merchantCity = merchantCity == null ? "PHNOM PENH" : merchantCity;
        merchantId = merchantId == null ? "DEFAULT MERCHANT ID" : merchantId;
        acquiringBank = acquiringBank == null ? "DEFAULT BANK" : acquiringBank;
        upiAccountInformation = upiAccountInformation == null ? "UPI123456" : upiAccountInformation;
        expirationTimestamp = expirationTimestamp == null ? 15 : expirationTimestamp;
        billNumber = billNumber == null ? "BILL123456" : billNumber;
        storeLabel = storeLabel == null ? "STORE" : storeLabel;
        terminalLabel = terminalLabel == null ? "TERMINAL1" : terminalLabel;
        mobileNumber = mobileNumber == null ? "012345678" : mobileNumber;
        purposeOfTransaction = purposeOfTransaction == null ? "Payment" : purposeOfTransaction;
        merchantAlternateLanguagePreference = merchantAlternateLanguagePreference == null ? "km" : merchantAlternateLanguagePreference;
        merchantNameAlternateLanguage = merchantNameAlternateLanguage == null ? "អ្នកលក់" : merchantNameAlternateLanguage;
        merchantCityAlternateLanguage = merchantCityAlternateLanguage == null ? "ភ្នំពេញ" : merchantCityAlternateLanguage;
    }
}
