package com.yorozuya.awesomecs.comon.exception;




import com.yorozuya.awesomecs.comon.Constants;

import java.io.Serial;

/**
 * @author wjc28
 * @version 1.0
 * @description: 业务异常类
 * @date 2024-08-08
 */
public class BusinessException extends RuntimeException{
	@Serial
	private static final long serialVersionUID = 1L;

	private Constants.ResponseCode responseCode;
	public BusinessException(Constants.ResponseCode responseCode) {
		this.responseCode = responseCode;
	}

	public Constants.ResponseCode getResponseCode() {
		return responseCode;
	}
}
