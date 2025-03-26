package org.storyweaver.emotionweaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

    @SuppressWarnings("unchecked")
    public Map<String, Object> parseVariationsJson() {
        try {
            // variations.jsonをリソースから読み込む
            String jsonContent = new String(
                StoryPromptGenerator.class.getClassLoader()
                    .getResourceAsStream("variations.json")
                    .readAllBytes()
            );
            try {
                JSONObject jsonObject = new JSONObject(jsonContent);
                // デバッグ用ログ：parameters の内容を確認
                System.out.println("Debugging variations.json parameters:");
                System.out.println("parameters: " + jsonObject.getJSONObject("parameters").toString());

                // JSONからMapに変換
                Map<String, Object> variations = new HashMap<>();

                // 配列フィールドを変換（String[] に確実に変換）
                variations.put("model", toStringArray(jsonObject.getJSONArray("model")));
                variations.put("age", toStringArray(jsonObject.getJSONArray("age")));
                variations.put("appearance", toStringArray(jsonObject.getJSONArray("appearance")));
                variations.put("expression", toStringArray(jsonObject.getJSONArray("expression")));
                variations.put("hair", toStringArray(jsonObject.getJSONArray("hair")));
                variations.put("skin", toStringArray(jsonObject.getJSONArray("skin")));
                variations.put("emotion", toStringArray(jsonObject.getJSONArray("emotion")));
                variations.put("lighting", toStringArray(jsonObject.getJSONArray("lighting")));
                variations.put("reflection", toStringArray(jsonObject.getJSONArray("reflection")));
                variations.put("pose", toStringArray(jsonObject.getJSONArray("pose")));
                variations.put("style", toStringArray(jsonObject.getJSONArray("style")));
                variations.put("others", toStringArray(jsonObject.getJSONArray("others")));
                // negative_prompt をカンマ区切りの文字列に変換
                String[] negativePromptArray = toStringArray(jsonObject.getJSONArray("negative_prompt"));
                variations.put("negative_prompt", new String[]{String.join(", ", negativePromptArray)});

                // locationをList<Map<String, String>>として保持
                List<Map<String, String>> locationList = new ArrayList<>();
                JSONArray locationArray = jsonObject.getJSONArray("location");
                for (int i = 0; i < locationArray.length(); i++) {
                    JSONObject locationObj = locationArray.getJSONObject(i);
                    Map<String, String> locationEntry = new HashMap<>();
                    locationEntry.put("name", locationObj.getString("name"));
                    locationEntry.put("storyStep", locationObj.getString("storyStep"));
                    locationList.add(locationEntry);
                }
                variations.put("location", locationList);

                // clothingをMap<String, String[]>に変換
                Map<String, String[]> clothingMap = new HashMap<>();
                JSONObject clothingObj = jsonObject.getJSONObject("clothing");
                for (String key : clothingObj.keySet()) {
                    JSONArray clothingArray = clothingObj.getJSONArray(key);
                    clothingMap.put(key, toStringArray(clothingArray));
                }
                variations.put("clothing", clothingMap);

                // parametersとpost_processingをそのままJSONObjectとして保持
                variations.put("parameters", jsonObject.getJSONObject("parameters"));
                variations.put("post_processing", jsonObject.getJSONObject("post_processing"));

                return variations;
            } catch (JSONException e) {
                // JSONパースエラーの詳細を出力
                System.err.println("Error parsing variations.json: " + e.getMessage());
                System.err.println("Problematic JSON content (first 1000 characters):");
                System.err.println(jsonContent.length() > 1000 ? jsonContent.substring(0, 1000) : jsonContent);
                // 行番号と文字位置の周辺を表示
                String[] lines = jsonContent.split("\n");
                int errorLine = Math.min(e.getMessage().contains("line") ? Integer.parseInt(e.getMessage().replaceAll(".*line (\\d+).*", "$1")) : 1, lines.length);
                int errorChar = Math.min(e.getMessage().contains("character") ? Integer.parseInt(e.getMessage().replaceAll(".*character (\\d+).*", "$1")) : 1, lines[errorLine - 1].length());
                System.err.println("Error at line " + errorLine + ", character " + errorChar + ":");
                for (int i = Math.max(1, errorLine - 2); i <= Math.min(lines.length, errorLine + 2); i++) {
                    System.err.println("Line " + i + ": " + lines[i - 1]);
                    if (i == errorLine) {
                        System.err.println(" ".repeat(Math.max(0, "Line X: ".length() + errorChar - 1)) + "^");
                    }
                }
                throw e; // 再スローしてプログラムを停止
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading variations.json: " + e.getMessage(), e);
        }
    }

    // JSONArray を String[] に変換するヘルパーメソッド
    private String[] toStringArray(JSONArray jsonArray) {
        String[] result = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            result[i] = jsonArray.getString(i);
        }
        return result;
    }
}