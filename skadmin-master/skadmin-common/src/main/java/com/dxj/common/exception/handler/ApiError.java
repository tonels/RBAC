package com.dxj.common.exception.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dxj
 * @date 2019-03-23
 */
@Data
class ApiError {

    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String message;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    ApiError(Integer status, String message) {
        this();
        this.status = status;
        this.message = message;
    }
}


