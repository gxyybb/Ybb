package com.example.ybb.config;



public class ResultVO<T> {
    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private ResultVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应
    public static <T> ResultVO<T> success(String msg, T data) {
        return new ResultVO<>(200, msg, data);
    }

    public static <T> ResultVO<T> success(String msg) {
        return success(msg, null);
    }

    public static <T> ResultVO<T> success(T data) {
        return success(null, data);
    }

    public static <T> ResultVO<T> success() {
        return success(null, null);
    }

    // 特殊成功响应
    public static <T> ResultVO<T> successYY(String msg, T data) {
        return new ResultVO<>(205, msg, data);
    }

    public static <T> ResultVO<T> successYY(String msg) {
        return successYY(msg, null);
    }

    public static <T> ResultVO<T> successYY(T data) {
        return successYY(null, data);
    }

    // 错误响应
    public static <T> ResultVO<T> error(String msg, T data) {
        return new ResultVO<>(500, msg, data);
    }

    public static <T> ResultVO<T> error(String msg) {
        return error(msg, null);
    }

    public static <T> ResultVO<T> error(T data) {
        return error(null, data);
    }

    public static <T> ResultVO<T> error() {
        return error(null, null);
    }

    // 失败响应（与错误响应一致）
    public static <T> ResultVO<T> failure(String msg, T data) {
        return error(msg, data);
    }

    public static <T> ResultVO<T> failure(String msg) {
        return error(msg);
    }

    public static <T> ResultVO<T> failure(T data) {
        return error(data);
    }

    public static <T> ResultVO<T> failure() {
        return error();
    }

    // 判断是否成功
    public boolean isSuccess() {
        return this.code == 200;
    }
}
