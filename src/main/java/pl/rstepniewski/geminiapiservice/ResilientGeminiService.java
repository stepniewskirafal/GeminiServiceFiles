package pl.rstepniewski.geminiapiservice;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResilientGeminiService {

    private final GeminiService geminiService;
    private final Retry retry;

    public Optional<String> uploadFileTest(String displayName) {
        return Optional.of(Decorators.ofSupplier(() -> geminiService.initiateResumableUpload(displayName))
                .withRetry(retry)
                .get());
    }

    public void uploadFile(String uploadUrl, String textPath) {
        Decorators.ofRunnable(() -> geminiService.uploadFile(uploadUrl, textPath))
                .withRetry(retry)
                .run();
    }

    public Optional<String> generateContent(String fileUri, String question) {
        return Optional.of(Decorators.ofSupplier(() -> geminiService.generateContent(fileUri, question))
                .withRetry(retry)
                .get());
    }

    public Optional<String> getFileUri(String fileName) {
        return Optional.of(Decorators.ofSupplier(() -> geminiService.getFileUri(fileName))
                .withRetry(retry)
                .get());
    }
}
