package com.sparta.orderservice.order.infrastructure.feign;

import com.github.themepark.common.application.dto.ResDTO;
import com.github.themepark.common.infrastructure.config.FeignConfig;
import com.sparta.orderservice.order.application.dto.reponse.ResProductGetByIdDTOApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "product-service",
        url = "http://localhost:8004",
        configuration = FeignConfig.class
)
public interface ProductFeignClientApi {
    @PostMapping("/v1/products/{id}/reserve")
    ResponseEntity<Void> reserve(@PathVariable("id") UUID productId,
                                 @RequestParam("qty") Integer quantity);

    @GetMapping("/v1/products/{id}/stock")
    void getStockById(@PathVariable UUID id);

    @PostMapping("/v1/products/internal/{id}/stocks-decrease")
    void postDecreaseById(@PathVariable UUID id);

    @PostMapping("/v1/products/internal/{id}/stocks-restore")
    void postRestoreById(@PathVariable UUID id);

    @GetMapping("/v1/products/{id}")
    ResDTO<ResProductGetByIdDTOApi> getBy(@PathVariable UUID id);
}
