package com.yorozuya.awesomecs.service.ai;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import cn.hutool.json.JSONObject;
import com.google.gson.Gson;
import com.yorozuya.awesomecs.comon.Constants;
import com.yorozuya.awesomecs.comon.exception.BusinessException;
import org.springframework.ai.chat.messages.Message;
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

@Component
public class ModelClient {
    private final DeepSeekChatModel chatModel;

    private static final String MINIMAX_API_URL = "https://api.minimaxi.com/v1/t2a_v2";
    private static final String MINIMAX_API_KEY = System.getenv("MINIMAX_API_KEY");
    private static final String QWEN_API_KEY = System.getenv("QWEN_API_KEY");
    private static final String CONTENT_TYPE = "application/json";


    private Map<String, List<Message>> userHistory = new ConcurrentHashMap<>();

    @Autowired
    public ModelClient(DeepSeekChatModel chatModel) {
        this.chatModel = chatModel;
    }


    public String chat(String text){
        return "";
    }

    public static String audio2Text(InputStream stream, Long userId) throws IOException {
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

        return recognizer.call(param, new File(tmpFileName));
    }

    

    public static boolean textToSpeech(String text) throws Exception {
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

        try (FileOutputStream fos = new FileOutputStream("./test.wav")) {
            fos.write(audioBytes);
        }

        JSONObject extraInfo = jsonResponse.getJSONObject("extra_info");
        if (extraInfo != null) {
            System.out.println("音频信息: ");
            System.out.println("  时长: " + extraInfo.getInt("audio_length") + "ms");
            System.out.println("  大小: " + extraInfo.getInt("audio_size") + "字节");
            System.out.println("  采样率: " + extraInfo.getInt("audio_sample_rate") + "Hz");
            System.out.println("  格式: " + extraInfo.getStr("audio_format"));
        }

        System.out.println("追踪ID: " + jsonResponse.getStr("trace_id"));
        System.exit(0);
        return false;
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
