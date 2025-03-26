package org.storyweaver.emotionweaver;

import org.json.JSONObject;

public class ModelParameters {
    private final int steps;
    private final double cfgScale;
    private final String sampler;

    public ModelParameters(JSONObject modelParams) {
        try {
            Object stepsObj = modelParams.get("steps");
            if (stepsObj instanceof Number) {
                this.steps = ((Number) stepsObj).intValue();
            } else if (stepsObj instanceof String) {
                this.steps = Integer.parseInt((String) stepsObj);
            } else {
                throw new IllegalArgumentException("steps must be a number or a string representing a number, but got: " + (stepsObj != null ? stepsObj.getClass().getName() : "null"));
            }

            Object cfgScaleObj = modelParams.get("cfg_scale");
            if (cfgScaleObj instanceof Number) {
                this.cfgScale = ((Number) cfgScaleObj).doubleValue();
            } else if (cfgScaleObj instanceof String) {
                this.cfgScale = Double.parseDouble((String) cfgScaleObj);
            } else {
                throw new IllegalArgumentException("cfg_scale must be a number or a string representing a number, but got: " + (cfgScaleObj != null ? cfgScaleObj.getClass().getName() : "null"));
            }

            this.sampler = modelParams.getString("sampler");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing model parameters: " + e.getMessage(), e);
        }
    }

    public int getSteps() {
        return steps;
    }

    public double getCfgScale() {
        return cfgScale;
    }

    public String getSampler() {
        return sampler;
    }

    // 追加：パラメータを文字列として取得するメソッド
    public String getStepsAsString() {
        return String.valueOf(steps);
    }

    public String getCfgScaleAsString() {
        return String.format("%.1f", cfgScale); // 小数点以下1桁にフォーマット
    }
}