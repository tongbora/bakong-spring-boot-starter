package io.github.tongbora.bakong.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckTransactionRequest(
        @NotBlank
        String md5
) {
}
