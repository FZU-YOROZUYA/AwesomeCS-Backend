package com.yorozuya.awesomecs.service.ai;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import cn.hutool.json.JSONObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import java.util.Base64;

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
        messages.add(new UserMessage(input));
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
        messages.add(output);
        if (output != null) {
            return output.getText();
        }
        return null;
    }

    public String audio2Text(InputStream stream, String userId) throws IOException {
        Recognition recognizer = new Recognition();
        String tmpFileName = userId + UUID.randomUUID().toString();
        Files.copy(stream, Paths.get(tmpFileName), StandardCopyOption.REPLACE_EXISTING);
        RecognitionParam param =
                RecognitionParam.builder()
                        .apiKey(QWEN_API_KEY)
                        .model("paraformer-realtime-v2")
                        .format("wav")
                        .sampleRate(16000)
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .build();
        File tmp = new File(tmpFileName);
        String rst = recognizer.call(param, tmp);
        tmp.delete();
        JsonObject obj = JsonParser.parseString(rst).getAsJsonObject();
        // sentences 是数组
        JsonArray sentences = obj.getAsJsonArray("sentences");
        JsonObject firstSentence = sentences.get(0).getAsJsonObject();

        // 取 text 字段
        String text = firstSentence.get("text").getAsString();
        return text;
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

        byte[] audioBytes = hexStringToByteArray(hexAudio);
        if (audioBytes == null || audioBytes.length == 0) {
            throw new BusinessException(Constants.ResponseCode.AUDIO_CHANGE_SERVICE_FAIL);
        }

        // 将字节数组编码为 base64 字符串
        String base64Audio = Base64.getEncoder().encodeToString(audioBytes);
        return base64Audio;
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
