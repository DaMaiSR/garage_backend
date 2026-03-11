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
        return ResultVo.fail("token verify failed", "token_error");
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseBody
    public ResultVo<Object> malformedJwtExceptionHandler(MalformedJwtException e) {
        return ResultVo.fail("token parse failed", "token_error");
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseBody
    public ResultVo<Object> expiredJwtExceptionHandler(ExpiredJwtException e) {
        return ResultVo.fail("token expired", "token_error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResultVo<Object> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("invalid request");
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
        if (message != null && message.toLowerCase().contains("token")) {
            return ResultVo.fail(message, "token_error");
        }
        return ResultVo.fail(message == null ? "system error" : message);
    }
}
