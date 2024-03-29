package org.example.spotifymusicscraper.service;

import org.example.spotifymusicscraper.model.APIAccessToken;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WebClientService {
    //Using value injection to automatically initialise credentials from application.properties file
    @Value("${spotify.client.id}")
    private String spotifyClientId;
    @Value("${spotify.client.secret}")
    private String spotifyClientSecret;

    public <T> T request(String requestType, HttpHeaders headers, String requestBody, Class<T> responseType, Integer bufferSize, String baseURL, String URI) {
        //Custom Max In-Memory Size for Buffer Codec to support large JSON responses from APIs
        WebClient webClient = WebClient.builder().codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(bufferSize * 1024)
        ).baseUrl(baseURL).build();

        if (requestType.equalsIgnoreCase("get")) {
            T response = webClient.get()
                    .uri(URI)
                    //Add HTTP request headers and body according to the format specified by the Spotify API
                    .headers(httpHeaders ->
                            //Encode the client id and client credentials using Base64
                            httpHeaders.addAll(headers))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    //Retrieve the JSON response body asynchronously in a POJO type
                    .bodyToMono(responseType)
                    .block();
            return response;
        }
        if (requestType.equalsIgnoreCase("post")) {
            T response = webClient.post()
                    .uri(URI)
                    //Add HTTP request headers and body according to the format specified by the Spotify API
                    .headers(httpHeaders ->
                            //Encode the client id and client credentials using Base64
                            httpHeaders.addAll(headers))
                    .bodyValue(requestBody)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    //Retrieve the JSON response body asynchronously in a POJO type
                    .bodyToMono(responseType)
                    .block();

            return response;
        }
        if (requestType.equalsIgnoreCase("put")) {
            T response = webClient.put()
                    .uri(URI)
                    //Add HTTP request headers and body according to the format specified by the Spotify API
                    .headers(httpHeaders ->
                            //Encode the client id and client credentials using Base64
                            httpHeaders.addAll(headers))
                    .bodyValue(requestBody)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    //Retrieve the JSON response body asynchronously in a POJO type
                    .bodyToMono(responseType)
                    .block();
            return response;
        }
        if (requestType.equalsIgnoreCase("delete")) {
            T response = webClient.delete()
                    .uri(URI)
                    //Add HTTP request headers and body according to the format specified by the Spotify API
                    .headers(httpHeaders ->
                            //Encode the client id and client credentials using Base64
                            httpHeaders.addAll(headers))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    //Retrieve the JSON response body asynchronously in a POJO type
                    .bodyToMono(responseType)
                    .block();
            return response;
        }
        return null;
    }

    public JSONObject requestJSONObject(String requestType, HttpHeaders headers, String requestBody, Integer bufferSize, String baseURL, String URI) throws FileNotFoundException {
        //Custom Max In-Memory Size for Buffer Codec to support large JSON responses from APIs
        WebClient webClient = WebClient.builder().codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(bufferSize * 1024)
        ).baseUrl(baseURL).build();

        if (requestType.equalsIgnoreCase("get")) {
            ResponseEntity<String> responseEntity = webClient.get()
            .uri(URI)
            //Add HTTP request headers and body according to the format specified by the Spotify API
            .headers(httpHeaders ->
                    //Encode the client id and client credentials using Base64
                    httpHeaders.addAll(headers))
            .accept(MediaType.APPLICATION_JSON)
            //Retrieve the JSON response body asynchronously in a responseEntityType
            .retrieve()
            .onStatus(
                    status -> status == HttpStatus.NOT_FOUND,
                    clientResponse -> Mono.empty()
            )
            .toEntity(String.class)
            .block();

            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new FileNotFoundException("No resource found at the specified URI");
            } else {
                return new JSONObject(responseEntity.getBody());
            }
        }
        return null;
    }

    public String getAPIAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((spotifyClientId + ":" + spotifyClientSecret).getBytes(StandardCharsets.UTF_8)));
        APIAccessToken accessToken = request("post", headers, "grant_type=client_credentials", APIAccessToken.class, 1024, "https://accounts.spotify.com/api", "/token");
        return accessToken.getAccess_token();
    }
}
