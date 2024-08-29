package pl.rstepniewski.geminiapiservice;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

public class RestClientWrapper {
    private final RestClient restClient;

    private RestClientWrapper(RestClient restClient) {
        this.restClient = restClient;
    }

    public static RestClientWrapper create() {
        return new RestClientWrapper(RestClient.create());
    }

    public ResponseEntity<String> get(String uri) {
        return restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> post(String uri, String jsonBody) {
        return restClient.post()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                .body(jsonBody)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> put(String uri, byte[] body) {
        return restClient.put()
                .uri(uri)
                .header(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> uploadFile(String uri, byte[] fileBytes, String mimeType, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Tworzenie wieloczęściowej mapy
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        ByteArrayResource byteArrayResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        bodyMap.add("file", byteArrayResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        return restClient.post()
                .uri(uri)
                .body(requestEntity)
                .retrieve()
                .toEntity(String.class);
    }
}