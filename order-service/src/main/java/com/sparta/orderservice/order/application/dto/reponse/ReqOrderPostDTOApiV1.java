package com.sparta.orderservice.order.application.dto.reponse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqOrderPostDTOApiV1 {

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @Min(value = 1, message = "최소 1개 이상 주문해야 합니다.")
    private Integer amount;

}
