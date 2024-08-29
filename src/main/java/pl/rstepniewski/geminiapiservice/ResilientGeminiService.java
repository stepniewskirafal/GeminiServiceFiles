package pl.rstepniewski.geminiapiservice;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class ResilientGeminiService {
    private final GeminiService geminiService;
    private final Retry retry;

    public Optional<String> uploadFile(String mimeType, long numBytes, String displayName) {
        return Decorators.ofSupplier(() -> Optional.of(geminiService.initiateResumableUpload(mimeType, numBytes, displayName)))
                .withRetry(retry)
                .withFallback(e -> Optional.empty())
                .get();
    }

    public Optional<String> generateContent(String fileUri, String question) {
        return Decorators.ofSupplier(() -> Optional.of(geminiService.generateContent(fileUri, question)))
                .withRetry(retry)
                .withFallback(e -> Optional.empty())
                .get();
    }
}