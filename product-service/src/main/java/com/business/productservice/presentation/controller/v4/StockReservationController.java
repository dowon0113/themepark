package com.business.productservice.presentation.controller.v4;

import com.business.productservice.application.service.v4.StockReservationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products")
public class StockReservationController {

    private final StockReservationService reservationService;
    // (선택) private final ProductRepository productRepository;

    @PostMapping("/{id}/reserve")
    @Transactional
    public ResponseEntity<Void> reserve(@PathVariable("id") UUID productId,
                                        @RequestParam("qty") Integer quantity) {

        // (선택) 404를 명확히 주고 싶다면 주석 해제
        // if (!productRepository.existsById(productId)) {
        //     return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        // }

        boolean ok = reservationService.reserve(productId, quantity);
        if (ok) return ResponseEntity.ok().build();
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
