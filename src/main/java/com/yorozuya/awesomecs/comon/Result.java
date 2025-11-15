package com.yorozuya.awesomecs.comon;


import java.io.Serial;
import java.io.Serializable;

/**
 * @author wjc28
 * @version 1.0
 * @description: 常量
 * @date 2024-08-01
 */
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -3826891916021780628L;

    private String code;
    private String info;
    private T data;

    public static <T> Result<T> buildResult(Constants.ResponseCode code) {
        return new Result<>(code.getCode(), code.getInfo(), null);
    }

    public static <T> Result<T> buildResult(Constants.ResponseCode code, String info) {
        return new Result<>(code.getCode(), info, null);
    }
    public static <T> Result<T> buildResult(Constants.ResponseCode code, T data) {
        return new Result<>(code.getCode(), code.getInfo(), data);
    }

    public static <T> Result<T> buildResult(String code, String info) {
        return new Result<>(code, info);
    }

    public static <T> Result<T> buildResult(Constants.ResponseCode code, Constants.ResponseCode info) {
        return new Result<>(code.getCode(), info.getInfo());
    }

    public static <T> Result<T> buildSuccessResult(T data) {
        return new Result<>(Constants.ResponseCode.SUCCESS.getCode(), Constants.ResponseCode.SUCCESS.getInfo(), data);
    }

    public static <T> Result<T> buildErrorResult() {
        return new Result<>(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
    }

    public static <T> Result<T> buildErrorResult(String info) {
        return new Result<>(Constants.ResponseCode.UN_ERROR.getCode(), info);
    }

    public Result(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public Result(String code, String info, T data) {
        this.code = code;
        this.info = info;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code='" + code + '\'' +
                ", info='" + info + '\'' +
                ", data=" + data +
                '}';
    }
}
