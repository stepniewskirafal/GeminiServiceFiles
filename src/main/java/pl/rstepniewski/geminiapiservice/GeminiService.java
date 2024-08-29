package pl.rstepniewski.geminiapiservice;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Service
public class GeminiService {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com";
    private static final String GOOGLE_API_KEY = "AIzaSyBhtwlpYuEM5DvNSsTwwwVqRluIRqWmsS0";

    private final RestTemplate restTemplate;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }

    public String initiateResumableUpload(String displayName) {
        String endpoint = BASE_URL + "/upload/v1beta/files?key=" + GOOGLE_API_KEY;
        String jsonInputString = "{\"file\": {\"display_name\": \"" + displayName + "\"}}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Goog-Upload-Protocol", "resumable");
        headers.add("X-Goog-Upload-Command", "start");

        HttpEntity<String> request = new HttpEntity<>(jsonInputString, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

        String uploadUrl = response.getHeaders().getFirst("X-Goog-Upload-URL");
        if (uploadUrl == null) {
            log.error("Upload URL not found in the response. Response body: {}", response.getBody());
            throw new RuntimeException("Failed to initiate resumable upload");
        }

        return uploadUrl;
    }

    public void uploadFile(String uploadUrl, String filePath) {
        try {
            byte[] fileContents = Files.readAllBytes(Paths.get(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add("X-Goog-Upload-Command", "upload, finalize");
            headers.add("X-Goog-Upload-Offset", "0");

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContents, headers);

            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.PUT, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Failed to upload file. Status code: {} Response body: {}", response.getStatusCode(), response.getBody());
                throw new IOException("Failed to upload file: " + response.getStatusCode());
            }
        } catch (IOException e) {
            log.error("Error during file upload: {}", e.getMessage(), e);
        }
    }

    public String getFileUri(String fileName) {
        String endpoint = BASE_URL + "/v1beta/files?key=" + GOOGLE_API_KEY;

        ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
        String responseBody = response.getBody();

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray filesArray = jsonResponse.getJSONArray("files");

        for (int i = 0; i < filesArray.length(); i++) {
            JSONObject fileObject = filesArray.getJSONObject(i);
            if (fileObject.has("displayName") && fileObject.getString("displayName").equals(fileName)) {
                return fileObject.getString("uri");
            }
        }

        log.error("File with name {} not found in the response.", fileName);
        throw new RuntimeException("File with name " + fileName + " not found in the response.");
    }

    public String generateContent(String fileUri, String question) {
        String endpoint = BASE_URL + "/v1beta/models/gemini-1.5-flash:generateContent?key=" + GOOGLE_API_KEY;
        String jsonInputString = "{ \"contents\": [{ \"parts\": [ {\"text\": \"" + question + "\"}, {\"file_data\": {\"mime_type\": \"application/pdf\", \"file_uri\": \"" + fileUri + "\"}} ] } ] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonInputString, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, requestEntity, String.class);
        return response.getBody();
    }
}
