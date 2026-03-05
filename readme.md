# bakong-spring-boot-starter

A **Spring Boot Starter** for integrating with the **Bakong Open API** and **KHQR SDK** — Cambodia's official QR payment system developed by the **National Bank of Cambodia (NBC)**.

> 🇰🇭 Add Bakong KHQR payment to any Spring Boot application with a single dependency — no boilerplate, no manual token management.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Token Auto-Renewal](#token-auto-renewal)
- [QR Expiration](#qr-expiration)
- [Resources & Documentation](#resources--documentation)
- [Notes](#notes)

---

## Prerequisites

Before using this starter, you need to:

1. **Create a Bakong Account** — Register at [https://bakong.nbc.gov.kh](https://bakong.nbc.gov.kh) and get your account verified.
2. **Register with Bakong Open API** — Sign up with your email at [https://api-bakong.nbc.gov.kh/](https://api-bakong.nbc.gov.kh/) to activate API access.

---

## Installation

### Gradle

```groovy
dependencies {
    implementation 'io.github.tongbora:bakong-spring-boot-starter:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.tongbora</groupId>
    <artifactId>bakong-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

> ✅ Available on [Maven Central](https://central.sonatype.com/artifact/io.github.tongbora/bakong-spring-boot-starter)

---

## Configuration

Add the following to your `application.yml`:

```yaml
bakong:
  base-url: https://api-bakong.nbc.gov.kh
  email: your_registered_email@example.com
  account-id: your_bakong_id@bank
  acquiring-bank: your_acquiring_bank_code
  merchant-name: Your Merchant Name
  mobile-number: "012345678"
  store-label: Your Store Label
```

Or using `application.properties`:

```properties
bakong.base-url=https://api-bakong.nbc.gov.kh
bakong.email=your_registered_email@example.com
bakong.account-id=your_bakong_id@bank
bakong.acquiring-bank=your_acquiring_bank_code
bakong.merchant-name=Your Merchant Name
bakong.mobile-number=012345678
bakong.store-label=Your Store Label
```

> ⚠️ **Security Warning:** Never commit real credentials to version control. Use environment variables or a secrets manager in production.

---

## Usage

Inject `BakongService` anywhere in your Spring application:

```java
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final BakongService bakongService;

    // Step 1 — Generate QR code
    @PostMapping("/generate-qr")
    public KHQRResponse<KHQRData> generateQR(@RequestBody BakongRequest request) {
        return bakongService.generateQR(request);
    }

    // Step 2 — Get QR as PNG image
    @PostMapping("/qr-image")
    public ResponseEntity<byte[]> getQRImage(@RequestBody KHQRData qrData) {
        byte[] image = bakongService.getQRImage(qrData);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }

    // Step 3 — Check if transaction is paid
    @PostMapping("/check-transaction")
    public BakongResponse checkTransaction(@RequestBody CheckTransactionRequest request) {
        return bakongService.checkTransactionByMD5(request);
    }
}
```

### Payment Flow

```
1. POST /generate-qr     → returns { qr, md5 }
2. POST /qr-image        → returns PNG image (show to customer to scan)
3. POST /check-transaction { md5 } → returns responseCode 0 (paid) or 1 (not yet paid)
```

### Response — Generate QR

```json
{
    "KHQRStatus": {
        "code": 0,
        "errorCode": null,
        "message": null
    },
    "data": {
        "md5": "2e8787edaddc31ffe9c572923db06d33",
        "qr": "0002010102121511KH12345678930360014bora_tong@aclb..."
    }
}
```

### Response — Check Transaction

```json
{
    "responseCode": 0,
    "responseMessage": "Success",
    "data": {
        "hash": "bf917e9534cac...",
        "fromAccountId": "customer@bank",
        "toAccountId": "bora_tong@aclb",
        "currency": "USD",
        "amount": 0.1,
        "createdDateMs": 1772125349000,
        "acknowledgedDateMs": 1772125351000,
        "externalRef": "100FT36931627892"
    }
}
```

> ✅ `responseCode: 0` — Payment confirmed
> ❌ `responseCode: 1` — Not yet paid or MD5 is invalid

---

## Token Auto-Renewal

No need to manually copy or manage Bearer Tokens. This starter handles everything automatically:

1. On first request, calls `/v1/renew_token` using your registered **email**
2. Decodes the returned **JWT** to read the real expiry time (`exp` claim)
3. **Caches the token in memory** — reuses it on subsequent requests
4. Automatically **renews the token** when it expires

You only need to provide your `bakong.email` in the config — nothing else.

> 🔒 The token is stored in memory only (not on disk). It resets on application restart.

---

## QR Expiration

This starter uses **KHQR SDK `1.0.0.16`** which supports QR code expiration — generated QR codes are valid for **15 minutes** by default:

```java
merchantInfo.setExpirationTimestamp(System.currentTimeMillMillis() + 15 * 60 * 1000);
```

After expiration, the QR code becomes invalid and the customer must request a new one. This improves security by preventing stale QR codes from being scanned.

---

## Resources & Documentation

| Resource | Link |
|---|---|
| Bakong Open API Document | [Download PDF](https://bakong.nbc.gov.kh/download/KHQR/integration/Bakong%20Open%20API%20Document.pdf) |
| KHQR SDK Document | [Download PDF](https://bakong.nbc.gov.kh/download/KHQR%20SDK.pdf) |
| KHQR Card Guideline | [Download PDF](https://bakong.nbc.gov.kh/en/download/KHQR/guideline/KHQR%20Card%20Guideline.pdf) |
| Bakong Open API Portal | [https://api-bakong.nbc.gov.kh/](https://api-bakong.nbc.gov.kh/) |
| Maven Central | [io.github.tongbora:bakong-spring-boot-starter](https://central.sonatype.com/artifact/io.github.tongbora/bakong-spring-boot-starter) |

---

## Notes

- **Production Restriction:** The `check-transaction` endpoint can only be called from servers **located in Cambodia** in production. Calls from servers outside Cambodia will be blocked by Bakong.
- **Currency:** The default currency is USD. Change `KHQRCurrency` in `MerchantInfo` to use KHR.
- **Customization:** To override the default `BakongService` or `BakongTokenService` bean, simply declare your own `@Bean` — the starter uses `@ConditionalOnMissingBean` and will back off automatically.
- **Branding Compliance:** When displaying KHQR in any customer-facing UI, always follow the official [KHQR Card Guideline](https://bakong.nbc.gov.kh/en/download/KHQR/guideline/KHQR%20Card%20Guideline.pdf).

---

## License

MIT License — see [LICENSE](LICENSE) for details.