package com.yorozuya.awesomecs.comon;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wjc28
 * @version 1.0
 * @description: 常量
 * @date 2024-08-01
 */
public class Constants {
	/**
	 * 响应状态
	 */
	public enum ResponseCode {
		SUCCESS("0000", "成功"),
		UN_ERROR("0001","未知失败"),
		ILLEGAL_PARAMETER("0002","非法参数"),
		INDEX_DUP("0003","主键冲突"),
		NO_UPDATE("0004", "SQL操作无更新"),
		REPEATED_OPERATION("0005", "重复的操作"),
		INVALID_TOKEN("0006", "非法的token"),
		NO_USER("0007", "用户不存在"),
		PASSWORD_WRONG("0008", "密码错误"),
		USER_EXIST("0009", "用户已存在"),
		EMPTY_FILE("0010", "文件为空"),
		INVALID_FILE_EXTENSION("0011", "不支持的文件类型"),
		INVALID_SIZE("0012", "文件大小超出限制"),
		NO_OBJECT("0013", "对象不存在"),
		NO_SEND_CODE("0014", "请先发送验证码"),
		CODE_OUT("0015", "验证码过期"),
		CODE_ERROR("0016", "验证码错误"),
        ONLY_ONE_CONSULTATION("0017", "只能创建一个咨询"),
        AUDIO_CHANGE_SERVICE_FAIL("0018", "语音转化服务失败"),
        PASSWORD_SAME_AS_OLD("0019","新密码不能与旧密码相同");

		private final String code;
		private final String info;

		ResponseCode(String code, String info) {
			this.code = code;
			this.info = info;
		}

		public String getCode() {
			return code;
		}

		public String getInfo() {
			return info;
		}

	}


	public static class MQConstants {
		public static final String AI_EXCHANGE = "chat_with_ai_exchange";

		public static final String AI_ROUTER = "chat_with_ai_router";


	}

	public static class PageConstants {
		public static Integer PAGE_SIZE = 20;
	}

	public static class VerifyConstants {
		public static Map<String, Map<String, Code>> CODE_MAP = new HashMap<>();
		static {
			CODE_MAP.put("LOGIN", new HashMap<>());
			CODE_MAP.put("REGISTER", new HashMap<>());
		}


		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		public static class Code{
			private LocalDateTime createdTime;
			private Integer code;
		}

	}

	public static class PuzzleConstants {
		public static Integer PUZZLE_NUM = 3;
		public static Integer DEBRIS_NUM = 16;
	}

}
