package com.sparta.orderservice.order.application.service;

import com.sparta.orderservice.order.application.dto.reponse.ReqOrderPostDTOApiV1;
import com.sparta.orderservice.order.application.dto.reponse.ResOrderPostDTOApiV1;
import jakarta.validation.Valid;

public interface OrderServiceApiV1 {
    ResOrderPostDTOApiV1 postBy(@Valid ReqOrderPostDTOApiV1 reqDto);
    ResOrderPostDTOApiV1 postByWithLock(@Valid ReqOrderPostDTOApiV1 reqDto);
}
