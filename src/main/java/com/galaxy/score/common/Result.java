package com.galaxy.score.common;

import lombok.Data;

/**
 * @Author hfr
 * @Date 2021/3/3 3:55
 */
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    private Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Result(Integer code, T data) {
        this.data = data;
        this.code = code;
    }
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result success(T data) {
        return new Result(1, "操作成功", data);
    }

    public static Result success(String msg) {
        return new Result(1, msg);
    }

    public static Result error(String msg) {
        return new Result(0, msg);
    }

}
