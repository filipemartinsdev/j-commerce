package com.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private String status;
    private String message;
    private T data;

    private StandardResponse() {}

    private StandardResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> StandardResponse<T> success(T data){
        return new StandardResponse<T>("success", null, data);
    }

    public static StandardResponse<Void> fail(String message){
        return new StandardResponse<Void>("fail", message, null);
    }

    public static <T> StandardResponse<T> fail(String message, T data){
        return new StandardResponse<T>("fail", message, data);
    }

    public static <T> StandardResponse<T> error(){
        return new StandardResponse<T>("error", null, null);
    }

    public static <T> StandardResponse<T> error(String message){
        return new StandardResponse<T>("error", message, null);
    }

    public static <T> StandardResponse<T> error(String message, T data){
        return new StandardResponse<T>("error", message, data);
    }
}
