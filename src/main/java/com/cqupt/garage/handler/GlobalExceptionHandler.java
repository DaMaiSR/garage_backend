package com.cqupt.garage.handler;

import com.cqupt.garage.utils.ResultVo;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SignatureException.class)
    @ResponseBody
    public ResultVo<Object> signatureExceptionHandler(SignatureException e) {
        return ResultVo.fail("令牌校验失败", "token_error");
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseBody
    public ResultVo<Object> malformedJwtExceptionHandler(MalformedJwtException e) {
        return ResultVo.fail("令牌解析失败", "token_error");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ResultVo<Object> expiredJwtExceptionHandler(ExpiredJwtException e) {
        return ResultVo.fail("登录已过期，请重新登录", "token_error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultVo<Object> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("请求参数不合法");
        return ResultVo.fail(msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResultVo<Object> constraintViolationExceptionHandler(ConstraintViolationException e) {
        return ResultVo.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResultVo<Object> exceptionHandler(Exception e) {
        String message = e.getMessage();
        if (message != null
                && (message.toLowerCase().contains("token")
                || message.contains("登录状态无效")
                || message.contains("登录已过期"))) {
            return ResultVo.fail("登录状态无效，请重新登录", "token_error");
        }
        return ResultVo.fail(message == null ? "系统异常" : message);
    }
}
