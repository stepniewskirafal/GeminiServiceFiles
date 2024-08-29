package pl.rstepniewski.geminiapiservice;

import io.github.resilience4j.retry.Retry;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    private static final String TEXT_PATH = "src/main/resources/The_Witcher_3.pdf"; // Path to your file
    private static final String FILE_NAME = "The_Witcher_3.pdf"; // Path to your file
    private static final String QUESTION = "What needs to be done for the Witcher to draw his sword?"; // Path to your file

    public static void main(String[] args) {
        // Configuring Retry
        Retry retry = RetryConfiguration.createRetry();

        GeminiService geminiService = new GeminiService();
        ResilientGeminiService resilientGeminiService = new ResilientGeminiService(geminiService, retry);

        try {
            // Step 1: Get MIME type and file size
            String mimeType = Files.probeContentType(Paths.get(TEXT_PATH));
            long numBytes = Files.size(Paths.get(TEXT_PATH));
            String displayName = FILE_NAME; // Replace with your display name

            // Step 2: Initiate resumable upload
            Optional<String> uploadUrlOpt = resilientGeminiService.uploadFile(mimeType, numBytes, displayName);
            String uploadUrl = uploadUrlOpt.orElseThrow(() -> new RuntimeException("Upload URL not available"));

            // Step 3: Upload the actual file
            geminiService.uploadFile(uploadUrl, numBytes, TEXT_PATH);

            // Step 4: Get file URI
            String fileUri = geminiService.getFileUri(FILE_NAME);

            // Step 5: Generate content
            Optional<String> contentOpt = resilientGeminiService.generateContent(fileUri, QUESTION);
            contentOpt.ifPresent(content -> {
                String text = new JSONObject(content)
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                System.out.println("Generated Content Response: " + text);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}