package pl.rstepniewski.geminiapiservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiApiServiceRunner {

    private static final String TEXT_PATH = "src/main/resources/The_Witcher_3.pdf";
    private static final String FILE_NAME = "The_Witcher_3.pdf";
    private static final String QUESTION = "What needs to be done for the Witcher to draw his sword?";

    private final ResilientGeminiService resilientGeminiService;

    public void execute() {
        try {
            // Step 1: Initiate test upload
            Optional<String> uploadUrlOpt = resilientGeminiService.uploadFileTest(FILE_NAME);
            String uploadUrl = uploadUrlOpt.orElseThrow(() -> {
                log.error("Upload URL not available");
                return new RuntimeException("Upload URL not available");
            });

            // Step 2: Upload the actual file
            resilientGeminiService.uploadFile(uploadUrl, TEXT_PATH);

            // Step 3: Get file URI
            Optional<String> fileUri = resilientGeminiService.getFileUri(FILE_NAME);
            fileUri.ifPresentOrElse(
                    uri -> log.info("File URI: {}", uri),
                    () -> {
                        log.error("File URI not available");
                        throw new RuntimeException("File URI not available");
                    }
            );

            // Step 4: Generate content
            Optional<String> contentOpt = resilientGeminiService.generateContent(fileUri.get(), QUESTION);
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
