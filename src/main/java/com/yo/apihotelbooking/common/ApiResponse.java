package com.yo.apihotelbooking.common;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
public record ApiResponse<T>(boolean success, String message, T data, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp) {
    public  static  <T> ApiResponse<T> success(String message, T data){
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> success(T data){
        return success("Success", data);
    }
    public static ApiResponse<Void> successMessage(String message){
        return success(message, null);
    }
    public static ApiResponse<Void> error(String message){
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(String message, T data){
        return new ApiResponse<>(false,message,data,LocalDateTime.now());
    }
}
