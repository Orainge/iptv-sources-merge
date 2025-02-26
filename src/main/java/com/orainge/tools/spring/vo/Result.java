package com.orainge.tools.spring.vo;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 返回结果类
 *
 * @author Eason Huang
 * @date 2021/1/11
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    private Integer code;
    private String message;
    private Object data;

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }

    public static Result ok(Object data) {
        return new Result().setCode(0).setMessage("OK").setData(data);
    }

    public static Result error(String message) {
        return new Result().setCode(-1).setMessage("ERROR").setMessage(message);
    }
}