package com.example.orchardcommon.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data; // 修正：使用泛型T，不要Object
    private boolean success;

    // 全参构造
    public Result(int code, String msg, T data, boolean success) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.success = success;
    }

    // 成功返回重载
    public static <T> Result<T> ok(T data, String msg, int code) {
        return new Result<>(code, msg, data, true);
    }

    // 快捷成功：默认200、提示成功
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data, true);
    }

    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(200, msg, data, true);
    }

    // 失败返回
    public static <T> Result<T> error(T data, String msg, int code) {
        return new Result<>(code, msg, data, false);
    }

    // 默认500失败
    public static <T> Result<T> error(String msg, T data) {
        return new Result<>(500, msg, data, false);
    }
    public static <T> Result<T> error(int code,String msg) {
          return new Result<>(code, msg, null, false);
    }     // 最简失败：仅提示信息，无数据
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null, false);
    }

    // 无数据成功
    public static <T> Result<T> ok() {
        return new Result<>(200, "操作成功", null, true);
    }
}