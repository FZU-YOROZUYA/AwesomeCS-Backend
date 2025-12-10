package com.yorozuya.awesomecs.service;

import java.util.Arrays;
import java.util.Collections;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {
    public static void simpleMultiModalConversationCall()
            throws ApiException, NoApiKeyException, UploadFileException {
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("audio",
                                "/Users/wujinchao/dev/AwesomeCS-Backend/" +
                                        "19917562036963000323403cf8f-6e2e-4b69-8151-eca10d6d0d3d.wav")))
                .build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model("qwen-audio-asr")
                .message(userMessage)
                .apiKey(System.getenv("QWEN_API_KEY"))
                .build();
        MultiModalConversationResult result = conv.call(param);
        String json = JsonUtils.toJson(result);

        System.out.println(extractFirstText(json));
    }

    public static String extractFirstText(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonObject output = root.has("output") ? root.getAsJsonObject("output") : null;
        if (output == null) return null;

        JsonArray choices = output.has("choices") ? output.getAsJsonArray("choices") : null;
        if (choices == null || choices.size() == 0) return null;

        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject message = firstChoice.has("message") ? firstChoice.getAsJsonObject("message") : null;
        if (message == null) return null;

        JsonArray content = message.has("content") ? message.getAsJsonArray("content") : null;
        if (content == null || content.size() == 0) return null;

        JsonObject firstContent = content.get(0).getAsJsonObject();
        return firstContent.has("text") ? firstContent.get("text").getAsString() : null;
    }
    public static void main(String[] args) {
        try {
            simpleMultiModalConversationCall();
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }
}