package io.github.tongbora.bakong.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import io.github.tongbora.bakong.config.BakongProperties;
import io.github.tongbora.bakong.dto.BakongRequest;
import io.github.tongbora.bakong.dto.BakongResponse;
import io.github.tongbora.bakong.dto.CheckTransactionRequest;
import io.github.tongbora.bakong.service.BakongService;
import io.github.tongbora.bakong.service.BakongTokenService;
import kh.gov.nbc.bakong_khqr.BakongKHQR;
import kh.gov.nbc.bakong_khqr.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class BakongServiceImpl implements BakongService {

    private final BakongTokenService bakongTokenService;
    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final BakongProperties properties;

    public BakongServiceImpl(BakongTokenService bakongTokenService, RestClient restClient, ObjectMapper mapper, BakongProperties properties) {
        this.bakongTokenService = bakongTokenService;
        this.restClient = restClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public KHQRResponse<KHQRData> generateQR(BakongRequest request) {
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setExpirationTimestamp(System.currentTimeMillis() + 15 * 60 * 1000);
        merchantInfo.setBakongAccountId(properties.getAccountId());
        merchantInfo.setMerchantId("123456");
        merchantInfo.setAcquiringBank(properties.getAcquiringBank());
        merchantInfo.setCurrency(KHQRCurrency.KHR);
        merchantInfo.setAmount(request.amount());
        merchantInfo.setMerchantName(properties.getMerchantName());
        merchantInfo.setMerchantCity("PHNOM PENH");
        merchantInfo.setBillNumber("#12345");
        merchantInfo.setMobileNumber(properties.getMobileNumber());
        merchantInfo.setStoreLabel(properties.getStoreLabel());
        merchantInfo.setUpiAccountInformation("KH123456789");
        merchantInfo.setMerchantAlternateLanguagePreference("km");
        merchantInfo.setMerchantNameAlternateLanguage("តុងបូរា");
        merchantInfo.setMerchantCityAlternateLanguage("ភ្នំពញ");
        return BakongKHQR.generateMerchant(merchantInfo);
    }

    @Override
    public byte[] getQRImage(KHQRData qr) {
        try {
            if (qr == null || qr.getQr() == null || qr.getQr().isBlank()) {
                return "Invalid QR data".getBytes(StandardCharsets.UTF_8);
            }

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix bitMatrix = qrCodeWriter.encode(qr.getQr(), BarcodeFormat.QR_CODE, 300, 300, hints);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();

        } catch (WriterException e) {
            return "Error encoding QR data".getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return ("Unexpected error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public BakongResponse checkTransactionByMD5(CheckTransactionRequest request) {
        String bearerToken = bakongTokenService.getToken();
        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/v1/check_transaction_by_md5";

        String responseBody = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearerToken)
                .body(Map.of("md5", request.md5()))
                .retrieve()
                .body(String.class);

        log.info("Data response from Bakong API: {}", responseBody);

        try {
            return mapper.readValue(responseBody, BakongResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid upstream response", e);
        }
    }
}