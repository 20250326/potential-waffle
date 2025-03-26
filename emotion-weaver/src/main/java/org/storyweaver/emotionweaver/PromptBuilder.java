package org.storyweaver.emotionweaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

public class PromptBuilder {
    private final Map<String, Object> variations;
    private final String loraTag;
    private final Random random;
    private final List<String> prompts;
    private final List<JSONObject> payloads;
    private final List<String> locations;

    @SuppressWarnings("unchecked")
    public PromptBuilder(Map<String, Object> variations, String loraTag) {
        this.variations = variations;
        this.loraTag = loraTag;
        this.random = new Random();
        this.prompts = new ArrayList<>();
        this.payloads = new ArrayList<>();

        // location の型をチェック
        Object locationObj = variations.get("location");
        if (!(locationObj instanceof List)) {
            throw new IllegalArgumentException(
                    "location must be a List, but got: " + (locationObj != null ? locationObj.getClass().getName() : "null"));
        }

        List<Map<String, String>> locationList = (List<Map<String, String>>) locationObj;
        this.locations = new ArrayList<>();
        for (Map<String, String> location : locationList) {
            this.locations.add(location.get("name"));
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> generatePrompts() {
        JSONObject parameters = (JSONObject) variations.get("parameters");
        JSONObject postProcessing = (JSONObject) variations.get("post_processing");

        // locationMap を構築
        Map<String, String> locationMap = new HashMap<>();
        Object locationObj = variations.get("location");
        if (!(locationObj instanceof List)) {
            throw new IllegalArgumentException(
                    "location must be a List, but got: " + (locationObj != null ? locationObj.getClass().getName() : "null"));
        }

        List<Map<String, String>> locationList = (List<Map<String, String>>) locationObj;
        for (Map<String, String> location : locationList) {
            locationMap.put(location.get("name"), location.get("storyStep"));
        }

        for (int i = 0; i < locations.size(); i++) {
            String[] modelOptions = (String[]) variations.get("model");
            String selectedModel = modelOptions[random.nextInt(modelOptions.length)];
            JSONObject modelParams = parameters.has(selectedModel)
                    ? parameters.getJSONObject(selectedModel)
                    : parameters.getJSONObject("default");

            // デバッグ用ログ：selectedModel と modelParams を簡略化して出力
            System.out.println("Iteration " + (i + 1) + ": selectedModel=" + selectedModel + ", modelParams="
                    + modelParams.toString());

            String[] emotionOptions = (String[]) variations.get("emotion");
            String selectedEmotion = emotionOptions[random.nextInt(emotionOptions.length)];
            String[] lightingOptions = (String[]) variations.get("lighting");
            String selectedLighting = lightingOptions[random.nextInt(lightingOptions.length)];
            String[] reflectionOptions = (String[]) variations.get("reflection");
            String selectedReflection = reflectionOptions[random.nextInt(reflectionOptions.length)];

            String template = "%s, %s, %s, with %s and %s, %s, wearing %s, %s";
            String[] keys = { "lora", "age", "appearance", "expression", "hair", "skin", "location", "clothing", "pose",
                    "emotion", "lighting", "reflection", "negative_prompt" };
            String[] selectedValues = new String[keys.length];

            for (int j = 0; j < keys.length; j++) {
                if (keys[j].equals("lora")) {
                    selectedValues[j] = loraTag;
                } else if (keys[j].equals("location")) {
                    selectedValues[j] = locations.get(i);
                } else if (keys[j].equals("clothing")) {
                    String location = selectedValues[6];
                    Map<String, String[]> clothingMap = (Map<String, String[]>) variations.get("clothing");
                    String[] clothingOptions = clothingMap.get(location);
                    selectedValues[j] = clothingOptions[random.nextInt(clothingOptions.length)];
                } else if (keys[j].equals("emotion")) {
                    selectedValues[j] = selectedEmotion;
                } else if (keys[j].equals("lighting")) {
                    selectedValues[j] = selectedLighting;
                } else if (keys[j].equals("reflection")) {
                    selectedValues[j] = selectedReflection;
                } else {
                    String[] options = (String[]) variations.get(keys[j]);
                    selectedValues[j] = options[random.nextInt(options.length)];
                }
            }

            // モデルパラメータを取得
            ModelParameters modelParameters = new ModelParameters(modelParams);
            int steps = modelParameters.getSteps();
            double cfgScale = modelParameters.getCfgScale();
            String sampler = modelParameters.getSampler();

            // プロンプト生成
            String basePrompt = String.format(template,
                    selectedValues[0], selectedValues[1], selectedValues[2], selectedValues[4], 
                    selectedValues[5], selectedValues[6], selectedValues[7],
                    selectedValues[8]);

            String style = ((String[]) variations.get("style"))[random
                    .nextInt(((String[]) variations.get("style")).length)];
            String storyStep = locationMap.get(locations.get(i));

            StringBuilder postProcessingDetails = new StringBuilder();
            if (postProcessing != null) {
                if (postProcessing.has("ADetailer")) {
                    JSONObject adetailer = postProcessing.getJSONObject("ADetailer");
                    postProcessingDetails.append(", ADetailer: {");
                    postProcessingDetails.append("face: {model=")
                            .append(adetailer.getJSONObject("face").getString("model"))
                            .append(", prompt=\"").append(adetailer.getJSONObject("face").getString("prompt"))
                            .append("\", negative_prompt=\"")
                            .append(adetailer.getJSONObject("face").getString("negative_prompt"))
                            .append("\"}, ");
                    postProcessingDetails.append("hand: {model=")
                            .append(adetailer.getJSONObject("hand").getString("model"))
                            .append(", prompt=\"").append(adetailer.getJSONObject("hand").getString("prompt"))
                            .append("\", negative_prompt=\"")
                            .append(adetailer.getJSONObject("hand").getString("negative_prompt"))
                            .append("\"}}");
                }
                if (postProcessing.has("ControlNet")) {
                    JSONObject controlNet = postProcessing.getJSONObject("ControlNet");
                    postProcessingDetails.append(", ControlNet: {model=").append(controlNet.getString("model"))
                            .append(", control_weight=").append(controlNet.getDouble("control_weight"))
                            .append(", prompt=\"").append(controlNet.getString("prompt"))
                            .append("\"}");
                }
            }

            String fullPrompt = String.format("%s, %s, %s", basePrompt, style, storyStep);
            prompts.add(fullPrompt);

            // HTTPリクエスト用のペイロードを生成
            JSONObject payload = new JSONObject();
            payload.put("prompt", basePrompt + ", " + style);
            payload.put("negative_prompt", selectedValues[12]);
            payload.put("steps", steps);
            payload.put("cfg_scale", cfgScale);
            payload.put("sampler_name", sampler);
            payload.put("width", 512);
            payload.put("height", 768);

            JSONArray adetailer = new JSONArray();
            if (postProcessing.has("ADetailer")) {
                JSONObject adetailerSettings = postProcessing.getJSONObject("ADetailer");
                JSONObject faceDetailer = new JSONObject();
                faceDetailer.put("model", adetailerSettings.getJSONObject("face").getString("model"));
                faceDetailer.put("prompt", adetailerSettings.getJSONObject("face").getString("prompt"));
                faceDetailer.put("negative_prompt",
                        adetailerSettings.getJSONObject("face").getString("negative_prompt"));
                adetailer.put(faceDetailer);

                JSONObject handDetailer = new JSONObject();
                handDetailer.put("model", adetailerSettings.getJSONObject("hand").getString("model"));
                handDetailer.put("prompt", adetailerSettings.getJSONObject("hand").getString("prompt"));
                handDetailer.put("negative_prompt",
                        adetailerSettings.getJSONObject("hand").getString("negative_prompt"));
                adetailer.put(handDetailer);
            }
            payload.put("alwayson_scripts", new JSONObject().put("ADetailer", new JSONObject().put("args", adetailer)));

            if (postProcessing.has("ControlNet")) {
                JSONObject controlNetSettings = postProcessing.getJSONObject("ControlNet");
                JSONArray controlNet = new JSONArray();
                JSONObject controlNetUnit = new JSONObject();
                controlNetUnit.put("module", "openpose");
                controlNetUnit.put("model", "control_v11p_sd15_openpose");
                controlNetUnit.put("weight", controlNetSettings.getDouble("control_weight"));
                controlNetUnit.put("prompt", controlNetSettings.getString("prompt"));
                controlNet.put(controlNetUnit);
                payload.getJSONObject("alwayson_scripts").put("ControlNet", new JSONObject().put("args", controlNet));
            }

            payloads.add(payload);
        }

        return prompts;
    }

    public JSONObject getPayload(int index) {
        return payloads.get(index);
    }
}