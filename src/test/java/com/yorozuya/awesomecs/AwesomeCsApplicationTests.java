package com.yorozuya.awesomecs;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
class AwesomeCsApplicationTests {
    private static final String QWEN_API_KEY = System.getenv("QWEN_API_KEY");

    @Test
    void contextLoads() {
        Recognition recognizer = new Recognition();
        RecognitionParam param =
                RecognitionParam.builder()
                        .apiKey(QWEN_API_KEY)
                        .model("paraformer-v2")
                        .format("wav")
                        .sampleRate(16000)
                        .parameter("language_hints", new String[]{"zh"})
                        .build();
        File tmp = new File("/Users/wujinchao/dev/AwesomeCS-Backend/" +
                "19917562036963000323403cf8f-6e2e-4b69-8151-eca10d6d0d3d.wav");
//        File tmp = new File("/Users/wujinchao/dev/AwesomeCS-Backend/" +
//                "asr_example.wav");
        String rst = recognizer.call(param, tmp);
        System.out.println(rst);
        JsonObject obj = JsonParser.parseString(rst).getAsJsonObject();
        // sentences 是数组
        JsonArray sentences = obj.getAsJsonArray("sentences");
        JsonObject firstSentence = sentences.get(0).getAsJsonObject();

        // 取 text 字段
        String text = firstSentence.get("text").getAsString();
        System.out.println("text:" +text);
    }

}
