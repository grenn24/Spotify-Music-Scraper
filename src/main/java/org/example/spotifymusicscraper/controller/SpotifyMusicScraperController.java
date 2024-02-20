package org.example.spotifymusicscraper.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import org.example.spotifymusicscraper.model.*;
import org.example.spotifymusicscraper.service.*;

//Route incoming HTTP requests to different methods
@RestController
@RequestMapping("/scraper")
@ResponseBody
public class SpotifyMusicScraperController {
    private final WebClientService webClientService;
    private final DataAccessService dataAccessService;
    private final SongParserService songParserService;

    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public SpotifyMusicScraperController(WebClientService webClientService, DataAccessService dataAccessService, SongParserService songParserService) {
        this.webClientService = webClientService;
        this.dataAccessService = dataAccessService;
        this.songParserService = songParserService;
    }

    //Get Requests
    @GetMapping("/list")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongs() {
        return dataAccessService.fetchSongsFromDatabase();
    }
    @GetMapping("/list/url")
    //Fetch YouTube URL for all songs in the database
    public List<String> getYouTubeURLAll() {
        return songParserService.fetchYouTubeURL(dataAccessService.fetchSongsFromDatabase());
    }
    @GetMapping("/list/{songName}/url")
    //Fetch YouTube URL for a specific song in the database
    public String getYouTubeURL(@PathVariable(name="songName") String songName) {
        Song song = null;
        try {
            song = dataAccessService.fetchSongsFromDatabase(URLDecoder.decode(songName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return songParserService.fetchYouTubeURL(song);
    }
    @GetMapping("/list/insights")
    //Generates insights based on the songs in the database
    public Map<String, Object> insights() {
        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("Total Number of Songs", dataAccessService.countDatabase());
        insights.put("Your Favourite Artist", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "artists"));
        insights.put("Your Favourite Genre", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "genre"));
        insights.put("Most Popular Songs", dataAccessService.fetchMostPopularSongsFromDatabase());
        insights.put("Newest Songs", dataAccessService.fetchNewestSongsFromDatabase());
        insights.put("Longest Duration Song", dataAccessService.fetchLongestDurationSongFromDatabase());
        insights.put("Shortest Duration Song", dataAccessService.fetchShortestDurationSongFromDatabase());
        return insights;
    }
    @GetMapping("/spotifytoken")
    //Fetch OAuth2 authentication token to attach as header for subsequent HTTP requests to Spotify API
    public String getAPIAccessToken() {
        return webClientService.getAPIAccessToken();
    }

    //Put Requests
    @PutMapping("/list/{playlistId}")
    //Fetches a list of Songs inside a playlist and maps each song to an entry in the database
    public List<Song> addNewPlaylist(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = webClientService.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks");

        //Convert JSON file into a List of Songs
        List<Song> songs = songParserService.convertJSONObjectToListOfSongs(JSONSongs);
        dataAccessService.addSongsToDatabase(songs);
        return songs;
    }

    //Delete Requests
    @DeleteMapping("/list")
    //Delete all songs in the database
    public void resetDatabase() {
        dataAccessService.deleteSongsFromDatabase();
    }
    @DeleteMapping("/list/{songName}")
    //Delete a specific song in the database
    public void deleteSong(@PathVariable(name="songName") String songName) {
        dataAccessService.deleteSongsFromDatabase(songName);
    }
}
