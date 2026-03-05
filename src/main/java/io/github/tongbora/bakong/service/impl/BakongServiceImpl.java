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
import kh.gov.nbc.bakong_khqr.model.KHQRCurrency;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;
import kh.gov.nbc.bakong_khqr.model.MerchantInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class BakongServiceImpl implements BakongService {

    private final BakongTokenService bakongTokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final BakongProperties properties;

    @Override
    public KHQRResponse<KHQRData> generateQR(BakongRequest request) {

        // You Can Customize MerchantInfo Based on Your Requirement
        MerchantInfo merchantInfo = new MerchantInfo();
        merchantInfo.setExpirationTimestamp(System.currentTimeMillis() + 15 * 60 * 1000); // QR code valid for 15 minutes
        merchantInfo.setBakongAccountId(properties.getAccountId());
        merchantInfo.setAcquiringBank(properties.getAcquiringBank());
        merchantInfo.setMerchantName(properties.getMerchantName());
        merchantInfo.setMobileNumber(properties.getMobileNumber());
        merchantInfo.setStoreLabel(properties.getStoreLabel());
        merchantInfo.setMerchantId("123456");
        merchantInfo.setCurrency(KHQRCurrency.KHR);
        merchantInfo.setAmount(request.amount());
//        merchantInfo.setTerminalLabel("Terminal 1");
//        merchantInfo.setPurposeOfTransaction("Payment for Order #12345");
        merchantInfo.setMerchantCity("PHNOM PENH");
        merchantInfo.setBillNumber("#12345");
        merchantInfo.setUpiAccountInformation("KH123456789");
        merchantInfo.setMerchantAlternateLanguagePreference("km");
        merchantInfo.setMerchantNameAlternateLanguage("តុងបូរា");
        merchantInfo.setMerchantCityAlternateLanguage("ភ្នំពញ");
        return BakongKHQR.generateMerchant(merchantInfo);
    }

    @Override
    public byte[] getQRImage(KHQRData qr) {
        try {
            // Validate input
            if (qr == null || qr.getQr() == null || qr.getQr().isBlank()) {
                return "Invalid QR data".getBytes(StandardCharsets.UTF_8);
            }

            String qrCodeText = qr.getQr();

            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeText, BarcodeFormat.QR_CODE, 300, 300, hints);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return pngOutputStream.toByteArray();

        } catch (WriterException e) {
            // Thrown by QRCodeWriter.encode() if encoding fails (e.g., invalid data)
            return "Error encoding QR data".getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fallback for any unexpected error
            return ("Unexpected error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public BakongResponse checkTransactionByMD5(CheckTransactionRequest request) {
        String bearerToken = bakongTokenService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(bearerToken);

        Map<String, String> requestBody = Map.of("md5", request.md5());
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/v1/check_transaction_by_md5";

        ResponseEntity<String> upstream = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        try {
            // Deserialize data into BakongResponse
            return mapper.readValue(upstream.getBody(), BakongResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid upstream response", e);
        }
    }
}
