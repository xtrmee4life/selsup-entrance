package ru.selsup.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {

    @Getter
    @Setter
    public String API_REFERENCE = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private TimeUnit timeUnit;
    private int requestLimit;

    private final AtomicInteger requestCount;
    private final Semaphore semaphore;
    private final HttpClient httpClient;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        if (requestLimit > 0) {
            this.requestLimit = requestLimit;
        }
        this.requestCount = new AtomicInteger(0);
        this.semaphore = new Semaphore(requestLimit);
        httpClient = HttpClient.newHttpClient();
    }

    public void processDocument(Document document, String signature) throws IOException, InterruptedException {
        int i = requestCount.incrementAndGet();
        boolean isAcquired = semaphore.tryAcquire(1L, timeUnit);
        if(isAcquired) {
            ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String documentJson = objectMapper.writeValueAsString(document);
            HttpRequest.BodyPublisher postContent = HttpRequest.BodyPublishers.ofString(documentJson);
            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_REFERENCE))
                    .header("Content-Type", "application/json")
                    .POST(postContent)
                    .build();
            httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        }

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Document {
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        @JsonProperty("importRequest")
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private Date productionDate;
        @JsonProperty("production_type")
        private String productionType;
        @JsonProperty("products")
        private List<Product> products;
        @JsonProperty("reg_date")
        private Date regDate;
        @JsonProperty("reg_number")
        private String regNumber;

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        static class Description {
            @JsonProperty("participantInn")
            private String participantInn;
        }

        @Getter
        @AllArgsConstructor
        @NoArgsConstructor
        static class Product {
            @JsonProperty("certificate_document")
            private String certificateDocument;
            @JsonProperty("certificate_document_date")
            private Date certificateDocumentDate;
            @JsonProperty("certificate_document_number")
            private String certificateDocumentNumber;
            @JsonProperty("owner_inn")
            private String ownerInn;
            @JsonProperty("producer_inn")
            private String producerInn;
            @JsonProperty("production_date")
            private Date productionDate;
            @JsonProperty("tnved_code")
            private String tnvedCode;
            @JsonProperty("uit_code")
            private String uitCode;
            @JsonProperty("uitu_code")
            private String uituCode;
        }
    }

}
