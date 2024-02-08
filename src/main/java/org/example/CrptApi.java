package org.example;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {


    private final Semaphore semaphore;
    private final Gson gson;

    private final static String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public CrptApi(int requestLimit, TimeUnit timeUnit) {
        this.semaphore = new Semaphore(requestLimit);
        this.gson = new Gson();
        scheduler.scheduleAtFixedRate(() -> semaphore.release(requestLimit - semaphore.availablePermits()), 1, 1, timeUnit);
    }

    public void createDocument(Document document, String signature) throws Exception {
        semaphore.acquire();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(URL);
            String json = gson.toJson(document);
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Signature", signature);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println(EntityUtils.toString(response.getEntity()));
            }
        }

    }

    public static void main(String[] args) {
        CrptApi api = new CrptApi(5, TimeUnit.SECONDS);

        Product product = Product.builder()
                .certificate_document("certificate_document")
                .certificate_document_date("2020-01-23")
                .certificate_document_number("certificate_document_number")
                .owner_inn("owner_inn")
                .producer_inn("producer_inn")
                .production_date("2020-01-23")
                .tnved_code("tnved_code")
                .uit_code("uit_code")
                .uitu_code("uitu_code")
                .build();

        Description description = Description.builder()
                .participantInn("participantInn")
                .build();

        Document document = Document.builder()
                .description(description)
                .doc_id("doc_id")
                .doc_status("doc_status")
                .doc_type("LP_INTRODUCE_GOODS")
                .importRequest(true)
                .owner_inn("owner_inn")
                .participant_inn("participant_inn")
                .producer_inn("producer_inn")
                .production_date("2020-01-23")
                .production_type("production_type")
                .products(Collections.singletonList(product))
                .reg_date("2020-01-23")
                .reg_number("reg_number")
                .build();

        try {
            api.createDocument(document, "siganture");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}


@Getter
@Setter
@Builder
class Document {
    private Description description;
    private String doc_id;
    private String doc_status;
    private String doc_type;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private String production_date;
    private String production_type;
    private List<Product> products;
    private String reg_date;
    private String reg_number;
}

@Getter
@Setter
@Builder
class Description {
    private String participantInn;
}

@Getter
@Setter
@Builder
class Product {
    private String certificate_document;
    private String certificate_document_date;
    private String certificate_document_number;
    private String owner_inn;
    private String producer_inn;
    private String production_date;
    private String tnved_code;
    private String uit_code;
    private String uitu_code;
}
