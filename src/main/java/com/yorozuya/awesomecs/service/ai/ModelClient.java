package com.yorozuya.awesomecs.service.ai;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import cn.hutool.json.JSONObject;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;

@Component
@Slf4j
public class ModelClient {
    @Resource
    private DeepSeekChatModel chatModel;

    private static final String MINIMAX_API_URL = "https://api.minimaxi.com/v1/t2a_v2";
    private static final String MINIMAX_API_KEY = System.getenv("MINIMAX_API_KEY");
    private static final String QWEN_API_KEY = System.getenv("QWEN_API_KEY");

    private static final String CONTENT_TYPE = "application/json";


    @Resource
    private Tools tools;

    public String chat(List<Message> messages, String input, String userId) {
        Prompt fullPrompt = new Prompt(messages);
        ChatResponse response = ChatClient.create(chatModel)
                .prompt(fullPrompt)
                .tools(tools)
                .toolContext(Map.of("userId", userId))
                .call()
                .chatClientResponse()
                .chatResponse();

        AssistantMessage output = null;
        if (response != null) {
            output = response.getResult().getOutput();
        }
        if (output != null) {
            return output.getText();
        }
        return null;
    }

    public String audio2Text(InputStream stream, String userId) throws IOException, NoApiKeyException, UploadFileException {
        String tmpFileName = userId + UUID.randomUUID() + ".wav";
        String fileName = Paths.get(tmpFileName).getFileName().toString();
        Files.copy(stream, Paths.get(tmpFileName), StandardCopyOption.REPLACE_EXISTING);
        MultiModalConversation conv = new MultiModalConversation();
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("audio",
                                fileName)))
                .build();
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model("qwen-audio-asr")
                .message(userMessage)
                .apiKey(QWEN_API_KEY)
                .build();
        MultiModalConversationResult result = conv.call(param);
        String json = JsonUtils.toJson(result);
        new File(tmpFileName).delete();
        return extractFirstText(json);
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


    public String textToSpeech(String text) throws Exception {
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("model", "speech-2.6-hd");
        requestParams.put("text", text);
        requestParams.put("stream", false);
        Map<String, Object> voiceSetting = new HashMap<>();
        voiceSetting.put("voice_id", "male-qn-qingse");
        requestParams.put("voice_setting", voiceSetting);
        Gson gson = new Gson();
        String request = gson.toJson(requestParams, Map.class);

        HttpResponse response = HttpRequest.post(MINIMAX_API_URL)
                .header("Authorization", "Bearer " + MINIMAX_API_KEY)
                .header("Content-Type", CONTENT_TYPE)
                .body(request)
                .timeout(30000)
                .execute();

        if (response.getStatus() != 200) {
            throw new BusinessException(Constants.ResponseCode.AUDIO_CHANGE_SERVICE_FAIL);
        }

        String responseBody = response.body();
        JSONObject jsonResponse = JSONUtil.parseObj(responseBody);

        String envMinimaxApiKey = System.getenv("MINIMAX_API_KEY");
        log.info(String.valueOf((envMinimaxApiKey.equals(MINIMAX_API_KEY))));
        log.info(envMinimaxApiKey);
        log.info(MINIMAX_API_KEY);

        JSONObject baseResp = jsonResponse.getJSONObject("base_resp");
        if (baseResp != null && baseResp.getInt("status_code") != 0) {
            throw new BusinessException(Constants.ResponseCode.AUDIO_CHANGE_SERVICE_FAIL);
        }

        JSONObject data = jsonResponse.getJSONObject("data");
        if (data == null) {
            throw new BusinessException(Constants.ResponseCode.AUDIO_CHANGE_SERVICE_FAIL);
        }

        String hexAudio = data.getStr("audio");
        if (hexAudio == null || hexAudio.isEmpty()) {
            throw new BusinessException(Constants.ResponseCode.AUDIO_CHANGE_SERVICE_FAIL);
        }

        return hexAudio;
    }


    private static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("无效的十六进制字符串");
        }

        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }


}
