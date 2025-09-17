//package com.business.productservice.application.service.v4;
//
//import com.business.productservice.application.dto.v1.request.ReqProductPostDTOApiV1;
//import com.business.productservice.application.dto.v1.request.ReqProductPutDTOApiV1;
//import com.business.productservice.application.dto.v1.response.*;
//import com.business.productservice.application.exception.ProductExceptionCode;
//import com.business.productservice.application.service.v1.ProductServiceApiV1;
//import com.business.productservice.domain.product.entity.ProductEntity;
//import com.business.productservice.domain.product.entity.StockEntity;
//import com.business.productservice.infrastructure.feign.SlackFeignClientApiV1;
//import com.business.productservice.infrastructure.kafka.dto.ReqToSlackPostDTOApiV1;
//import com.business.productservice.infrastructure.persistence.product.ProductJpaRepository;
//import com.business.productservice.infrastructure.persistence.product.StockJpaRepository;
//import com.github.themepark.common.application.exception.CustomException;
//import com.querydsl.core.types.Predicate;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RedissonClient;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//@Slf4j
//public class ProductServiceImplApiV4 implements ProductServiceApiV4 {
//
//    private final ProductJpaRepository productRepository;
//    private final StockJpaRepository stockRepository;
//    private final SlackFeignClientApiV1 slackFeignClientApiV1;
//    private final RedissonClient redissonClient;
//
//    @Override
//    public ResProductPostDTOApiV1 postBy(ReqProductPostDTOApiV1 reqDto) {
//        ProductEntity product = reqDto.toEntityWithStock();
//
//        ProductEntity savedProduct = productRepository.save(product);
//
//        return ResProductPostDTOApiV1.of(savedProduct);
//    }
//
//    @Override
//    public ResProductGetByIdDTOApiV1 getBy(UUID id){
//        ProductEntity productEntity = productRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//        return ResProductGetByIdDTOApiV1.of(productEntity);
//    }
//
//
//    @Override
//    public ResProductGetDTOApiV1 getBy(Predicate predicate, Pageable pageable){
//
//        Page<ProductEntity> productEntityPage = productRepository.findAll(predicate, pageable);
//        return ResProductGetDTOApiV1.of(productEntityPage);
//    }
//
//    @Override
//    public ResProductPutDTOApiV1 putBy(UUID id, ReqProductPutDTOApiV1 dto) {
//
//        ProductEntity productEntity = productRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//
//        productEntity.update(
//                dto.getProduct().getName(),
//                dto.getProduct().getDescription(),
//                dto.getProduct().getProductType(),
//                dto.getProduct().getPrice(),
//                dto.getProduct().getEventStartAt(),
//                dto.getProduct().getEventEndAt(),
//                dto.getProduct().getLimitQuantity(),
//                dto.getProduct().getProductStatus()
//        );
//        return ResProductPutDTOApiV1.of(productEntity);
//    }
//
//    @Override
//    public void deleteBy(UUID id) {
//
//        ProductEntity productEntity = productRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//        StockEntity stockEntity = stockRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//
//        productEntity.deletedBy(1L);
//        stockEntity.deletedBy(1L);
//    }
//
//    /// /////////////////////////////////////////////////////////////////////////////////////////
//    //재고 차감 코드
//    @Override
//    @Transactional
//    public void postDecreaseById(UUID id) {
//        StockEntity stockEntity = stockRepository.findByIdWithPessimisticLock(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//
//        if (stockEntity.getStock() <= 0) {
//            throw new CustomException(ProductExceptionCode.PRODUCT_STOCK_SOLDOUT);
//        }
//
//        stockEntity.decrease();
//
//    }
//
//    @Override
//    public void postRestoreById(UUID id) {
//        StockEntity stockEntity = stockRepository.findByIdWithPessimisticLock(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//
//        stockEntity.restore();
//    }
//
//    @Override
//    public ResStockGetByIdDTOApiV1 getStockById(UUID id){
//        StockEntity stockEntity = stockRepository.findById(id)
//                .orElseThrow(() -> new CustomException(ProductExceptionCode.PRODUCT_NOT_FOUND));
//        return ResStockGetByIdDTOApiV1.of(stockEntity);
//    }
//
//}
