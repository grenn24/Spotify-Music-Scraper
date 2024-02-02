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

    @GetMapping("/spotifySearch/{playlistId}")
    //Fetches a list of Songs from Spotify API and creates a new song entry in the database table
    public List<Song> searchPlaylist(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = new JSONObject(this.webClientHelper.request("get", headers, "", String.class, 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks"));

        //Extract "items" array from original JSON response body
        List<Song> songs = songParser(JSONSongs);
        this.songRepository.saveAll(songs);
        return songs;
    }

    @GetMapping("/database/fetchAll")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongs() {
        return this.songRepository.findAll();
    }

    @DeleteMapping("/database/reset")
    //Delete all songs in the database
    public void resetDatabase() {
        this.songRepository.deleteAll();
    }

    @GetMapping("/api")
    //Retrieving OAuth2 authentication token to attach as header for subsequent HTTP requests to Spotify API
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

    @GetMapping("/database/fetchYoutubeURLAll")
    //Retrieve YouTube URL for all songs in the database
    public List<String> getSongURI() {
        Iterable<Song> songs = getAllSongs();
        List<String> songURLs = new ArrayList<>();
        for (Song song: songs) {
            String apikey = "AIzaSyBcQ7nf9tDGm1gVo0FqzRYQoDtPCTZcY2c";
            String URI = String.format("%s?part=snippet&type=video&videoCategoryId=10&key=%s&q=%s", "/youtube/v3/search", apikey, song.getName() + "|official");
            JSONObject matchedSongs = new JSONObject(this.webClientHelper.request("get", new HttpHeaders(), "", String.class, 1024, "https://www.googleapis.com", URI));
            songURLs.add("https://www.youtube.com/watch?v=" + matchedSongs.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId"));
        }
        return songURLs;
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
