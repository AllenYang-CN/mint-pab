package com.mint.pab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

}
