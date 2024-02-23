package com.example.demo.httpclient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class HttpClientTest {
    private static final AtomicInteger counter = new AtomicInteger(0);


    public static void main(String[] args) {
        System.out.println("Java version: " + Runtime.version() + ", vendor: " + System.getProperty("java.vm.vendor"));

        URI requestUri = URI.create("https://localhost:8080/hello");
        String requestJson = """
            {
                "message": "This is the message"
            }
            """;

        System.setProperty("jdk.httpclient.keepalive.timeout.h2", "20");
        System.setProperty("jdk.httpclient.connectionPoolSize", "20");

        HttpClient httpClient = getHttpClient();


        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(100);

        IntStream.range(0, 100)
                .forEach(c -> executor.scheduleAtFixedRate(() -> {
                    try {
                        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                                .uri(requestUri);

                        HttpRequest request = requestBuilder.build();

                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        counter.getAndIncrement();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, 0, 30, TimeUnit.SECONDS));


        while (true) {
            try {
                Thread.sleep(5000);
                System.out.println("Sent: " + counter.get());

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static HttpClient getHttpClient() {
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

        try {
            HttpClient.Builder builder = HttpClient.newBuilder();
            Duration connectionTimeout = Duration.ofSeconds(100);
            builder.connectTimeout(connectionTimeout);
            builder.version(HttpClient.Version.HTTP_2);
            builder.followRedirects(HttpClient.Redirect.NORMAL);

            SSLContext selectedSslContext;
            selectedSslContext = SSLContext.getInstance("SSL");
            selectedSslContext.init(null, TRUST_ALL_CERTS, new SecureRandom());

            builder.sslContext(selectedSslContext);

            HttpClient httpClient = builder.build();
            return httpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final static TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
    };

}
