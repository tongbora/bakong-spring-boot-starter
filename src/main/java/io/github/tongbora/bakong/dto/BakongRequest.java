package io.github.tongbora.bakong.dto;

import jakarta.validation.constraints.NotBlank;

public record BakongRequest(
        @NotBlank
        Double amount
) {
}
