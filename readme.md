# bakong-spring-boot-starter

A **Spring Boot Starter** for integrating with the **Bakong Open API** and **KHQR SDK** — Cambodia's official QR payment system developed by the **National Bank of Cambodia (NBC)**.

> 🇰🇭 Add Bakong KHQR payment to any Spring Boot application with a single dependency — no boilerplate, no manual token management.

---

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [BakongRequest Fields](#bakongrequest-fields)
- [Payment Flow](#payment-flow)
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
    implementation 'io.github.tongbora:bakong-spring-boot-starter:1.0.5'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.tongbora</groupId>
    <artifactId>bakong-spring-boot-starter</artifactId>
    <version>1.0.5</version>
</dependency>
```

> ✅ Available on [Maven Central](https://central.sonatype.com/artifact/io.github.tongbora/bakong-spring-boot-starter)

---

## Configuration

The only required config is your Bakong account credentials. Add to `application.yml`:

```yaml
bakong:
  base-url: https://api-bakong.nbc.gov.kh
  email: your_registered_email@example.com
  account-id: your_bakong_id@bank
```

Or using `application.properties`:

```properties
bakong.base-url=https://api-bakong.nbc.gov.kh
bakong.email=your_registered_email@example.com
bakong.account-id=your_bakong_id@bank
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

    // Step 2 — Get QR as PNG image bytes
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

---

## BakongRequest Fields

`BakongRequest` is flexible — only `amount` is required. All other fields are optional and fall back to sensible defaults if not provided.

```java
public record BakongRequest(
        KHQRCurrency currency,                       // default: KHR
        Double amount,                               // REQUIRED
        String merchantName,                         // default: "DEFAULT MERCHANT"
        String merchantCity,                         // default: "PHNOM PENH"
        String merchantId,                           // default: "DEFAULT MERCHANT ID"
        String acquiringBank,                        // default: "DEFAULT BANK"
        String upiAccountInformation,               // default: "UPI123456"
        Integer expirationTimestamp,                // default: 15 minutes
        String billNumber,                           // default: "BILL123456"
        String storeLabel,                           // default: "STORE"
        String terminalLabel,                        // default: "TERMINAL1"
        String mobileNumber,                         // default: "012345678"
        String purposeOfTransaction,                // default: "Payment"
        String merchantAlternateLanguagePreference, // default: "km"
        String merchantNameAlternateLanguage,       // default: "អ្នកលក់"
        String merchantCityAlternateLanguage        // default: "ភ្នំពេញ"
)
```

### Field Reference

| Field | Type | Required | Default | Description |
|---|---|---|---|---|
| `amount` | `Double` | ✅ Yes | — | Payment amount |
| `currency` | `KHQRCurrency` | No | `KHR` | `KHR` or `USD` |
| `merchantName` | `String` | No | `"DEFAULT MERCHANT"` | Merchant display name |
| `merchantCity` | `String` | No | `"PHNOM PENH"` | Merchant city |
| `merchantId` | `String` | No | `"DEFAULT MERCHANT ID"` | Merchant identifier |
| `acquiringBank` | `String` | No | `"DEFAULT BANK"` | Bank code (e.g. `"ABA"`, `"ACLB"`) |
| `billNumber` | `String` | No | `"BILL123456"` | Invoice or bill reference number |
| `storeLabel` | `String` | No | `"STORE"` | Store label shown on QR |
| `mobileNumber` | `String` | No | `"012345678"` | Merchant mobile number |
| `terminalLabel` | `String` | No | `"TERMINAL1"` | Terminal identifier |
| `purposeOfTransaction` | `String` | No | `"Payment"` | Payment description |
| `expirationTimestamp` | `Integer` | No | `15` | QR validity in **minutes** |
| `upiAccountInformation` | `String` | No | `"UPI123456"` | UPI account info |
| `merchantAlternateLanguagePreference` | `String` | No | `"km"` | Alternate language code |
| `merchantNameAlternateLanguage` | `String` | No | `"អ្នកលក់"` | Merchant name in Khmer |
| `merchantCityAlternateLanguage` | `String` | No | `"ភ្នំពេញ"` | City name in Khmer |

### Minimal Request (amount only)

```json
{
    "amount": 1.50
}
```

All other fields will use their default values.

---

## Payment Flow

Follow these 3 steps in order to complete a payment:

### Step 1 — Generate QR

Only `amount` is required. All other fields have defaults.

```http
POST /generate-qr
Content-Type: application/json

{
    "amount": 1.50,
    "acquiringBank": "ABA"
}
```

**Full example with all fields:**

```http
POST /generate-qr
Content-Type: application/json

{
    "currency": "USD",
    "amount": 5.00,
    "merchantName": "My Coffee Shop",
    "merchantCity": "SIEM REAP",
    "merchantId": "SHOP001",
    "acquiringBank": "ABA",
    "billNumber": "INV-20240301",
    "storeLabel": "Main Branch",
    "mobileNumber": "012999888",
    "terminalLabel": "Counter 1",
    "purposeOfTransaction": "Coffee Payment",
    "expirationTimestamp": 15,
    "upiAccountInformation": "KH123456789",
    "merchantAlternateLanguagePreference": "km",
    "merchantNameAlternateLanguage": "ហាងកាហ្វេខ្ញុំ",
    "merchantCityAlternateLanguage": "សៀមរាប"
}
```

**Response:**

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

Save the `qr` string and `md5` — you need both in the next steps.

---

### Step 2 — Get QR Image

Pass the `data` object from Step 1 response to get a scannable PNG image:

```http
POST /qr-image
Content-Type: application/json

{
    "qr": "0002010102121511KH12345678930360014bora_tong@aclb...",
    "md5": "2e8787edaddc31ffe9c572923db06d33"
}
```

**Response:** PNG image bytes (`image/png`) — display this to the customer to scan with the Bakong app.

---

### Step 3 — Check Transaction

After the customer scans and pays, verify payment using the `md5` from Step 1:

```http
POST /check-transaction
Content-Type: application/json

{
    "md5": "2e8787edaddc31ffe9c572923db06d33"
}
```

**Response — Paid (`responseCode: 0`):**

```json
{
    "responseCode": 0,
    "responseMessage": "Success",
    "data": {
        "hash": "bf917e9534cac...",
        "fromAccountId": "customer@bank",
        "toAccountId": "bora_tong@aclb",
        "currency": "USD",
        "amount": 1.50,
        "createdDateMs": 1772125349000,
        "acknowledgedDateMs": 1772125351000,
        "externalRef": "100FT36931627892"
    }
}
```

**Response — Not Yet Paid (`responseCode: 1`):**

```json
{
    "responseCode": 1,
    "responseMessage": "Transaction could not be found. Please check and try again.",
    "errorCode": 1,
    "data": null
}
```

| `responseCode` | Meaning |
|---|---|
| `0` | ✅ Payment confirmed — proceed with order |
| `1` | ❌ Not yet paid or MD5 is invalid |

---

## Token Auto-Renewal

No need to manually copy or manage Bearer Tokens. This starter handles everything automatically:

1. On first request, calls `/v1/renew_token` using your configured `bakong.email`
2. Decodes the returned **JWT** to read the real expiry time (`exp` claim)
3. **Caches the token in memory** — reuses it on subsequent requests without hitting the API again
4. Automatically **renews the token** when it expires

You only need to provide `bakong.email` in your config — nothing else.

> 🔒 The token is stored in memory only (not on disk). It resets on application restart.

---

## QR Expiration

QR codes expire after **15 minutes by default**. After expiration, the customer must request a new QR code — this prevents stale codes from being scanned.

You can customize the expiration per request using the `expirationTimestamp` field (value in **minutes**):

```json
{
  "amount": 1.50,
  "expirationTimestamp": 15
}
```

| Value | Expiry |
|---|---|
| `5` | 5 minutes |
| `15` | 15 minutes (default) |
| `30` | 30 minutes |
| `60` | 1 hour |

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
- **Currency:** Pass `"USD"` or `"KHR"` in the `currency` field. Defaults to `KHR` if not provided.
- **Bean Override:** To customize behavior, declare your own `@Bean` of type `BakongService` or `BakongTokenService` — the starter uses `@ConditionalOnMissingBean` and will back off automatically.
- **Branding Compliance:** When displaying KHQR in any customer-facing UI, always follow the official [KHQR Card Guideline](https://bakong.nbc.gov.kh/en/download/KHQR/guideline/KHQR%20Card%20Guideline.pdf).

---

## License

MIT License — see [LICENSE](LICENSE) for details.