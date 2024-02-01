package org.example.spotifymusicscraper.controller;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.example.spotifymusicscraper.SpotifyMusicScraperApplication;
import org.example.spotifymusicscraper.repository.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.spotifymusicscraper.model.*;

//Route incoming HTTP requests
@RestController
@ResponseBody
public class SpotifyMusicScraperController {
    //
    private final WebClient webClient;
    private final SongRepository songRepository;

    //Constructor Injection to autowire songRepository bean as a dependency
    public SpotifyMusicScraperController(SongRepository songRepository) {
        this.webClient = WebClient.builder().baseUrl("https://accounts.spotify.com/api").build();
        this.songRepository = songRepository;
    }
    @GetMapping("/spotify")
    //Fetches a list of Songs from Spotify API and returns it in the response body
    public APIAccessToken getPlaylistFromSpotify(@RequestBody(required = false) String playlistURL) {
        String clientId = "077d57140732448996a7b50fedf3fe8f";
        String clientSecret = "b3531f89fdba47b691a0a22c0194988c";
        String authorization = clientId + ":" + clientSecret;

        //Request an access token and store it in a POJO
        APIAccessToken response = this.webClient.post()
                .uri("/token")
                //Add HTTP request headers and body according to the format specified by the Spotify API
                .headers(httpHeaders -> {
                    //Encode the client id and client credentials using Base64
                    httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");
                    httpHeaders.add("Authorization", ("Basic " + Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))));
                })
                .bodyValue("grant_type=client_credentials")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                //Convert JSON-format access token into a POJO type
                .bodyToMono(APIAccessToken.class)
                .block();
        return response;
    }
}
