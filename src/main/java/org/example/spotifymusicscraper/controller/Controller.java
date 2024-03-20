package org.example.spotifymusicscraper.controller;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.example.spotifymusicscraper.model.*;
import org.example.spotifymusicscraper.service.*;
import org.springframework.web.server.ResponseStatusException;

//Route incoming HTTP requests to different methods
@RestController
@RequestMapping("/scraper")
@ResponseBody
public class Controller {
    private final WebClientService webClientService;
    private final DataAccessService dataAccessService;
    private final SongParserService songParserService;

    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public Controller(WebClientService webClientService, DataAccessService dataAccessService, SongParserService songParserService) {
        this.webClientService = webClientService;
        this.dataAccessService = dataAccessService;
        this.songParserService = songParserService;
    }

    //Get Requests
    @GetMapping("/list")
    @ResponseStatus(code = HttpStatus.OK)
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongs() {
        return dataAccessService.fetchSongsFromDatabase();
    }
    @GetMapping("/list/url")
    @ResponseStatus(code = HttpStatus.OK)
    //Fetch YouTube URL for all songs in the database
    public List<String> getYouTubeURLAll() {
        return songParserService.fetchYouTubeURL(dataAccessService.fetchSongsFromDatabase());
    }
    @GetMapping("/list/{songName}/url")
    @ResponseStatus(code = HttpStatus.OK)
    //Fetch YouTube URL for a specific song in the database
    public List<String> getYouTubeURL(@PathVariable(name="songName") String songName) {
        List<Song> songs = null;
        try {
            songs = dataAccessService.fetchSongsFromDatabase(URLDecoder.decode(songName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
        return songParserService.fetchYouTubeURL(songs);
    }
    @GetMapping("/list/insights")
    @ResponseStatus(code = HttpStatus.OK)
    //Generates insights based on the songs in the database
    public Map<String, Object> insights() {
        Map<String, Object> insights = new LinkedHashMap<>();
        try {
            insights.put("Total Number of Songs", dataAccessService.countDatabase());
            insights.put("Your Favourite Artist", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "artists"));
            insights.put("Your Favourite Genre", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "genre"));
            insights.put("Most Popular Songs", dataAccessService.fetchMostPopularSongsFromDatabase());
            insights.put("Newest Songs", dataAccessService.fetchNewestSongsFromDatabase());
            insights.put("Longest Duration Song", dataAccessService.fetchLongestDurationSongFromDatabase());
            insights.put("Shortest Duration Song", dataAccessService.fetchShortestDurationSongFromDatabase());
        } catch (NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return insights;
    }
    @GetMapping("/spotifytoken")
    @ResponseStatus(code = HttpStatus.OK)
    //Fetch OAuth2 authentication token to attach as header for subsequent HTTP requests to Spotify API
    public String getAPIAccessToken() {
        return webClientService.getAPIAccessToken();
    }

    //Put Requests
    @PutMapping("/list/{playlistId}")
    @ResponseStatus(code = HttpStatus.CREATED, reason = "Songs from spotify playlist were successfully added to the database")
    //Fetches a list of Songs inside a playlist and maps each song to an entry in the database
    public void addNewPlaylist(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = null;
        try {
            JSONSongs = webClientService.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks");
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }

        //Convert JSON file into a List of Songs
        List<Song> songs = songParserService.convertJSONObjectToListOfSongs(JSONSongs);
        dataAccessService.addSongsToDatabase(songs);
    }

    //Delete Requests
    @DeleteMapping("/list")
    @ResponseStatus(code = HttpStatus.OK, reason = "All songs deleted from database")
    //Delete all songs in the database
    public void resetDatabase() {
        dataAccessService.deleteSongsFromDatabase();
    }
    @DeleteMapping("/list/{songName}")
    @ResponseStatus(code = HttpStatus.OK, reason = "Specified song deleted from database")
    //Delete a specific song in the database
    public void deleteSong(@PathVariable(name="songName") String name) {
        try {
            dataAccessService.deleteSongsFromDatabase(name);
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    //Exception Handlers
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleNullPointerException(NullPointerException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(FileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<String> handleFileNotFoundException(FileNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(UnsupportedEncodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleUnsupportedEncodingException(UnsupportedEncodingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
