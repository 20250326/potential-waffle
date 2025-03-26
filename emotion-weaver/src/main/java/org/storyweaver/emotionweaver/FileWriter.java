package org.storyweaver.emotionweaver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileWriter {
    private final Path filePath;

    public FileWriter(String loraTag) {
        // loraTagからlora名を抽出（例："<LoRA:default_model:0.8>" → "default_model"）
        String loraName = loraTag.replaceAll(".*<LoRA:([^:]+):[^>]+>", "$1");
        // ファイル名に使用できない文字を置換（安全なファイル名にする）
        loraName = loraName.replaceAll("[<>:\"/\\\\|?*]", "_");

        // ファイル名を生成（例：prompts_default_model_202503251430.txt）
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String fileName = String.format("prompts_%s_%s.txt", loraName, timestamp);

        // 出力ディレクトリとファイルパス
        Path outputDir = Paths.get("output");
        this.filePath = outputDir.resolve(fileName);
    }

    public void writePromptToFile(int promptNumber, String prompt) {
        try {
            // 出力ディレクトリが存在しない場合は作成
            Path outputDir = filePath.getParent();
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            String promptLine = "Prompt " + promptNumber + ": " + prompt + "\n";
            Files.write(
                filePath,
                promptLine.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
            System.out.println("Prompt " + promptNumber + " has been written to " + filePath);
        } catch (Exception e) {
            System.err.println("Error writing prompt to file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}