package com.orainge.iptv.sources_merge.controller;

import com.orainge.tools.network.request.exception.HttpConnectException;
import com.orainge.tools.spring.exception.RequestParamsException;
import com.orainge.tools.spring.vo.Result;
import com.orainge.tools.spring.exception.ResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 错误处理器
 */
@Controller
@ControllerAdvice
@Slf4j
public class ErrorHandlerController implements ErrorController {
    @RequestMapping("/error")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public void handleError() {
    }

    @ExceptionHandler(HttpConnectException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleHttpConnectException(HttpConnectException e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(RequestParamsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleRequestParamsException(RequestParamsException e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(ResponseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleRequestParamsException(ResponseException e) {
        e.printStackTrace();
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public Result handleException(Exception e) {
        e.printStackTrace();
        log.error("[内部错误] - {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 404 返回错误页面
     */
    @Override
    public String getErrorPath() {
        return "/error";
    }
}
