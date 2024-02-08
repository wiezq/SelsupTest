package org.example;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

  private static final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

  public static final String CONTENT_TYPE = "application/json";

  private final Semaphore semaphore;
  private final Gson gson;
  private final HttpClient client;

  public CrptApi(int requestLimit, TimeUnit timeUnit) {
    this.semaphore = new Semaphore(requestLimit);
    this.gson = new Gson();
    this.client = HttpClient.newHttpClient();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(
        () -> semaphore.release(requestLimit - semaphore.availablePermits()), 1, 1, timeUnit);
  }

  public static void main(String[] args) {
    CrptApi api = new CrptApi(5, TimeUnit.SECONDS);

    Product product =
            Product.builder()
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

    Description description = Description.builder().participantInn("participantInn").build();

    Document document =
            Document.builder()
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
      System.out.println(api.createDocument(document, "signature"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String createDocument(Document document, String signature) throws Exception {
    semaphore.acquire();

    String json = gson.toJson(document);
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(URL))
            .header("Accept", CONTENT_TYPE)
            .header("Content-Type", CONTENT_TYPE)
            .header("Signature", signature)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
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
