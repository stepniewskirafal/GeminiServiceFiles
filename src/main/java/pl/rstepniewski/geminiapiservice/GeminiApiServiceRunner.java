package pl.rstepniewski.geminiapiservice;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Component
public class GeminiApiServiceRunner {

    private static final String TEXT_PATH = "src/main/resources/The_Witcher_3.pdf";
    private static final String FILE_NAME = "The_Witcher_3.pdf";
    private static final String QUESTION = "What needs to be done for the Witcher to draw his sword?";

    private final ResilientGeminiService resilientGeminiService;
    private final GeminiService geminiService;

    public GeminiApiServiceRunner(ResilientGeminiService resilientGeminiService, GeminiService geminiService) {
        this.resilientGeminiService = resilientGeminiService;
        this.geminiService = geminiService;
    }

    public void execute() {
        try {
            // Step 1: Get MIME type and file size
            String mimeType = Files.probeContentType(Paths.get(TEXT_PATH));
            long numBytes = Files.size(Paths.get(TEXT_PATH));
            String displayName = FILE_NAME;

            // Step 2: Initiate resumable upload
            Optional<String> uploadUrlOpt = resilientGeminiService.uploadFile(mimeType, numBytes, displayName);
            String uploadUrl = uploadUrlOpt.orElseThrow(() -> {
                log.error("Upload URL not available");
                return new RuntimeException("Upload URL not available");
            });

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

                log.info("Generated Content Response: {}", text);
            });
        } catch (Exception e) {
            log.error("An error occurred during execution", e);
        }
    }
}
