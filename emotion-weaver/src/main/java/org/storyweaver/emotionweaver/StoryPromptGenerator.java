package org.storyweaver.emotionweaver;

import java.util.List;
import java.util.Map;

public class StoryPromptGenerator {

    public static void main(String[] args) {
        try {
            // JSONデータをパース
            JsonParser jsonParser = new JsonParser();
            Map<String, Object> variations = jsonParser.parseVariationsJson();

            // プロンプト生成
            String loraTag = "<lora:Natural_Breasts:0.4>";
            PromptBuilder promptBuilder = new PromptBuilder(variations, loraTag);
            List<String> prompts = promptBuilder.generatePrompts();

            // プロンプトを処理（HTTPリクエストとファイル書き込み）
            HttpClientWrapper httpClient = new HttpClientWrapper();
            FileWriter fileWriter = new FileWriter(loraTag);
            for (int i = 0; i < prompts.size(); i++) {
                String prompt = prompts.get(i);
                try {
                    httpClient.sendTxt2ImgRequest(prompt, promptBuilder.getPayload(i));
                    System.out.println("Generated image for prompt " + (i + 1) + ": Success");
                } catch (Exception e) {
                    System.err.println("Error generating image for prompt " + (i + 1) + ": " + e.getMessage());
                    fileWriter.writePromptToFile(i + 1, prompt);
                }
            }

            // すべてのプロンプトを出力
            for (String prompt : prompts) {
                System.out.println(prompt);
            }
        } catch (Exception e) {
            System.err.println("Error in main processing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}