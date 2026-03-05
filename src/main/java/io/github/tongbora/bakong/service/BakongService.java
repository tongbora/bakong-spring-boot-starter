package io.github.tongbora.bakong.service;

import io.github.tongbora.bakong.dto.BakongRequest;
import io.github.tongbora.bakong.dto.BakongResponse;
import io.github.tongbora.bakong.dto.CheckTransactionRequest;
import kh.gov.nbc.bakong_khqr.model.KHQRData;
import kh.gov.nbc.bakong_khqr.model.KHQRResponse;


public interface BakongService {

    KHQRResponse<KHQRData> generateQR(BakongRequest request);
    byte[] getQRImage(KHQRData qr);
    BakongResponse checkTransactionByMD5(CheckTransactionRequest request);
}
