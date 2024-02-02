package org.example.spotifymusicscraper.controller;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.example.spotifymusicscraper.model.*;
import org.example.spotifymusicscraper.repository.*;
import org.example.spotifymusicscraper.config.*;

//Route incoming HTTP requests to different methods
@RestController
@ResponseBody
public class SpotifyMusicScraperController {
    private final SongRepository songRepository;
    private final WebClientHelper webClientHelper;

    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public SpotifyMusicScraperController(SongRepository songRepository, WebClientHelper webClientHelper) {
        this.songRepository = songRepository;
        this.webClientHelper = webClientHelper;
    }

    @GetMapping("/spotify/playlist/find/{playlistId}")
    //Fetches a list of Songs from Spotify API and creates a new song entry in the database table
    public List<Song> getPlaylistFromSpotify(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = new JSONObject(this.webClientHelper.request("get", headers, "", String.class, 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks"));

        //Extract "items" array from original JSON response body
        List<Song> songs = songParser(JSONSongs);
        this.songRepository.saveAll(songs);
        return songs;
    }

    @GetMapping("/spotify/playlist/database")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongsFromDatabase() {
        return this.songRepository.findAll();
    }

    @DeleteMapping("/spotify/playlist/database/reset")
    public void resetDatabase() {
        this.songRepository.deleteAll();
    }

    //Retrieving authentication token to attach as header for HTTP requests to Spotify API
    @GetMapping("/spotify")
    public String getAPIAccessToken() {
        String clientId = "077d57140732448996a7b50fedf3fe8f";
        String clientSecret = "b3531f89fdba47b691a0a22c0194988c";
        String authorization = clientId + ":" + clientSecret;

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8)));

        APIAccessToken accessToken = this.webClientHelper.request("post", headers, "grant_type=client_credentials", APIAccessToken.class, 1024, "https://accounts.spotify.com/api", "/token");

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
