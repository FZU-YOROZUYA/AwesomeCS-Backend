package com.yorozuya.awesomecs.comon.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wjc28
 * @version 1.0
 * @description: 全集异常处理器
 * @date 2024-09-17
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public Result<String> exception(BusinessException e) {
		log.error(e.getResponseCode().toString());
		e.printStackTrace();
		return Result.buildResult(e.getResponseCode());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		return Result.buildResult(Constants.ResponseCode.ILLEGAL_PARAMETER ,errors);
	}

	@ExceptionHandler(NotLoginException.class)
	public Result<String> notLoginException(NotLoginException e) {
		log.error(e.getMessage());
		e.printStackTrace();
		return Result.buildErrorResult("Token 异常");
	}

	@ExceptionHandler(Exception.class)
	public Result<String> exception(Exception e) {
		log.error(e.getMessage());
		e.printStackTrace();
		return Result.buildResult(Constants.ResponseCode.UN_ERROR);

	}
}
