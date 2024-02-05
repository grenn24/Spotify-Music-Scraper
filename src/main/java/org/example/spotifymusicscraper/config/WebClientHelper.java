package org.example.spotifymusicscraper.config;

import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientHelper {
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

    public JSONObject requestJSONObject(String requestType, HttpHeaders headers, String requestBody, Integer bufferSize, String baseURL, String URI) {
        //Custom Max In-Memory Size for Buffer Codec to support large JSON responses from APIs
        WebClient webClient = WebClient.builder().codecs(configurer ->
                configurer.defaultCodecs().maxInMemorySize(bufferSize * 1024)
        ).baseUrl(baseURL).build();

        if (requestType.equalsIgnoreCase("get")) {
            JSONObject response = webClient.get()
                    .uri(URI)
                    //Add HTTP request headers and body according to the format specified by the Spotify API
                    .headers(httpHeaders ->
                            //Encode the client id and client credentials using Base64
                            httpHeaders.addAll(headers))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    //Retrieve the JSON response body asynchronously in a POJO type
                    .bodyToMono(String.class)
                    .map(JSONObject::new)
                    .block();
            return response;
        }
        return null;
    }
}
