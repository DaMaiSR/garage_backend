package com.cqupt.garage.utils;

import lombok.Data;

@Data
public class ResultVo<T> {
    private boolean flag;
    private String message;
    private T data;

    public static <T> ResultVo<T> ok(String message) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(true);
        result.setMessage(message);
        return result;
    }

    public static <T> ResultVo<T> ok(T data) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(true);
        result.setData(data);
        return result;
    }

    public static <T> ResultVo<T> ok(T data, String message) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(true);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    public static <T> ResultVo<T> fail(String message) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(false);
        result.setMessage(message);
        return result;
    }

    public static <T> ResultVo<T> fail(String message, T data) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(false);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> ResultVo<T> error(Exception e) {
        ResultVo<T> result = new ResultVo<>();
        result.setFlag(false);
        result.setMessage("system error: " + e.getMessage());
        return result;
    }
}
