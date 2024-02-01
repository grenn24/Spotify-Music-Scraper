package org.example.spotifymusicscraper.controller;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.spotifymusicscraper.repository.*;
import org.example.spotifymusicscraper.model.*;

//Route incoming HTTP requests to different methods
@RestController
@ResponseBody
public class SpotifyMusicScraperController {
    //
    private final WebClient webClientForApi;
    private final WebClient webClientForPlaylist;
    private final SongRepository songRepository;

    //Using Constructor Injection to autowire songRepository bean as a dependency
    public SpotifyMusicScraperController(SongRepository songRepository) {
        this.webClientForApi = WebClient.builder().baseUrl("https://accounts.spotify.com/api").build();
        //Customise Max In-Memory Size for Buffer Codec to support large JSON responses from APIs
        this.webClientForPlaylist = WebClient.builder().codecs(configurer ->
            configurer.defaultCodecs().maxInMemorySize(1024 * 1024)
        ).baseUrl("https://api.spotify.com").build();
        this.songRepository = songRepository;
    }

    @GetMapping("/spotify/playlist/{playlistId}")
    //Fetches a list of Songs from Spotify API and saves it in the database
    public List<Song> getPlaylistFromSpotify(@PathVariable(name="playlistId", required = false) String playlistId) {

        //Fetch a list of songs
        JSONObject JSONResponse = this.webClientForPlaylist.get()
                .uri("/v1/playlists/" + playlistId + "/tracks")
                .header("Authorization", "Bearer " + getAPIAccessToken())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(JSONObject::new)
                .block();

        //Extract "items" array from original JSON response body
        List<Song> songs = songParser(JSONResponse);
        this.songRepository.saveAll(songs);
        return songs;
    }

    @GetMapping("/spotify/playlist/database")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongsFromDatabase() {
        return this.songRepository.findAll();
    }

    //Retrieving authentication token to attach as header for HTTP requests to Spotify API
    @GetMapping("/spotify")
    public String getAPIAccessToken() {
        String clientId = "077d57140732448996a7b50fedf3fe8f";
        String clientSecret = "b3531f89fdba47b691a0a22c0194988c";
        String authorization = clientId + ":" + clientSecret;

        //Request an access token and map it to a POJO
        APIAccessToken accessToken = this.webClientForApi.post()
                .uri("/token")
                //Add HTTP request headers and body according to the format specified by the Spotify API
                .headers(httpHeaders -> {
                    //Encode the client id and client credentials using Base64
                    httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");
                    httpHeaders.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)));
                })
                .bodyValue("grant_type=client_credentials")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                //Retrieve the JSON response body asynchronously in a POJO type
                .bodyToMono(APIAccessToken.class)
                .block();
        return accessToken.getAccess_token();
    }

    //Takes in a JSON Playlist file and extracts every song track contained inside with information
    public List<Song> songParser(JSONObject jsonObject) {
        List<Song> songs = new ArrayList<>();
        JSONArray arrayOfSongs = jsonObject.getJSONArray("items");
        for (int i = 0; i < arrayOfSongs.length(); i++) {
            JSONObject song = arrayOfSongs.getJSONObject(i).getJSONObject("track");
            String name = song.getString("name");
            String albumName = song.getJSONObject("album").getString("name");
            List<String> artists = getArtistName(song.getJSONArray("artists"));
            Integer releaseYear = Integer.valueOf(song.getJSONObject("album").getString("release_date").substring(0, 4));
            String genre = song.getJSONObject("album").getString("type");

            songs.add(new Song(name, albumName, artists, releaseYear, genre));
        }
        return songs;
    }

    //Takes in a JSONArray of Artists, returns a list of artists
    public List<String> getArtistName(JSONArray artists) {
        List<String> artistList = new ArrayList<>();
        for (int i = 0; i < artists.length(); i++) {
            artistList.add(artists.getJSONObject(i).getString("name"));
        }
        return artistList;
    }
}
