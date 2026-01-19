package com.webczw.my.netty.common;

import lombok.Data;

@Data
public class ResponseResult<T> {
    private boolean success;
    private T data;
    private String message;
    
    private ResponseResult(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }
    
    public static <T> ResponseResult<T> success(T data, String message) {
        return new ResponseResult<>(true, data, message);
    }
    
    public static <T> ResponseResult<T> error(String message) {
        return new ResponseResult<>(false, null, message);
    }
}
