package pl.rstepniewski.geminiapiservice;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResilientGeminiService {

    private final GeminiService geminiService;
    private final Retry retry;

    public Optional<String> uploadFile(String mimeType, long numBytes, String displayName) {
        return Optional.of(io.github.resilience4j.decorators.Decorators.ofSupplier(() -> geminiService.initiateResumableUpload(mimeType, numBytes, displayName))
                .withRetry(retry)
                .get());
    }

    public Optional<String> generateContent(String fileUri, String question) {
        return Optional.of(io.github.resilience4j.decorators.Decorators.ofSupplier(() -> geminiService.generateContent(fileUri, question))
                .withRetry(retry)
                .get());
    }
}
