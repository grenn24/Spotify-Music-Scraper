package org.example.spotifymusicscraper.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import org.example.spotifymusicscraper.model.*;
import org.example.spotifymusicscraper.repository.*;
import org.example.spotifymusicscraper.config.*;

//Route incoming HTTP requests to different methods
@RestController
@RequestMapping("/scraper")
@ResponseBody
public class SpotifyMusicScraperController {
    private final SongRepository songRepository;
    private final WebClientHelper webClientHelper;

    //Using value injection to automatically initialise credentials from application.properties file
    @Value("${youtube.apikey}")
    private String youTubeAPIKey;
    @Value("${spotify.client.id}")
    private String spotifyClientId;
    @Value("${spotify.client.secret}")
    private String spotifyClientSecret;

    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public SpotifyMusicScraperController(SongRepository songRepository, WebClientHelper webClientHelper) {
        this.songRepository = songRepository;
        this.webClientHelper = webClientHelper;
    }

    //Get Requests
    @GetMapping("/list")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongs() {
        return this.songRepository.findAll();
    }
    @GetMapping("/list/url")
    //Fetch YouTube URL for all songs in the database
    public List<String> getYouTubeURLAll() {
        Iterable<Song> songs = getAllSongs();
        List<String> songURLs = new ArrayList<>();
        for (Song song: songs) {
            songURLs.add(getYouTubeURL(song));
        }
        return songURLs;
    }
    @GetMapping("/list/{songName}/url")
    //Fetch YouTube URL for a specific song in the database
    public String getYouTubeURL(@PathVariable(name="songName") String songName) {
        Song song = null;
        try {
            song = this.songRepository.findByName(URLDecoder.decode(songName, "UTF-8")).getFirst();
        } catch (UnsupportedEncodingException e) {

        }
        return getYouTubeURL(song);
    }
    @GetMapping("/list/insights")
    //Generates insights based on the songs in the database
    public Map<String, Object> insights() {
        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("Total Number of Songs", this.songRepository.count());
        insights.put("Your Favourite Artist", sort(this.songRepository.findAll(), "artists"));
        insights.put("Your Favourite Genre", sort(this.songRepository.findAll(), "genre"));
        insights.put("Most Popular Songs", this.songRepository.findMostPopularSongs());
        insights.put("Newest Songs", this.songRepository.findNewestSongs());
        insights.put("Longest Duration Song", this.songRepository.findAllByOrderByDurationDesc().getFirst());
        insights.put("Shortest Duration Song", this.songRepository.findAllByOrderByDurationAsc().getFirst());
        return insights;
    }
    @GetMapping("/spotifytoken")
    //Fetch OAuth2 authentication token to attach as header for subsequent HTTP requests to Spotify API
    public String getAPIAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded");
        headers.add("Authorization", "Basic " + Base64.getEncoder().encodeToString((this.spotifyClientId + ":" + this.spotifyClientSecret).getBytes(StandardCharsets.UTF_8)));
        APIAccessToken accessToken = this.webClientHelper.request("post", headers, "grant_type=client_credentials", APIAccessToken.class, 1024, "https://accounts.spotify.com/api", "/token");
        return accessToken.getAccess_token();
    }

    //Put Requests
    @PutMapping("/list/{playlistId}")
    //Fetches a list of Songs inside a playlist and maps each song to an entry in the database
    public List<Song> addNewPlaylist(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = this.webClientHelper.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks");
        //Convert JSON file into a List of Songs
        List<Song> songs = getListOfSongsFromJSONObject(JSONSongs);
        this.songRepository.saveAll(songs);
        return songs;
    }

    //Delete Requests
    @DeleteMapping("/list")
    //Delete all songs in the database
    public void resetDatabase() {
        this.songRepository.deleteAll();
    }
    @DeleteMapping("/list/{songName}")
    //Delete a specific song in the database
    public void deleteSong(@PathVariable(name="songName") String songName) {
        this.songRepository.deleteByName(songName);
    }

    //Takes in a JSON Playlist file and extracts every song track contained inside with information
    public List<Song> getListOfSongsFromJSONObject(JSONObject jsonObject) {
        List<Song> songs = new ArrayList<>();
        JSONArray arrayOfSongs = jsonObject.getJSONArray("items");
        for (int i = 0; i < arrayOfSongs.length(); i++) {
            JSONObject song = arrayOfSongs.getJSONObject(i).getJSONObject("track");
            String name = song.getString("name");
            String albumName = song.getJSONObject("album").getString("name");
            String artists = getArtistName(song.getJSONArray("artists"));
            String releaseDate  = song.getJSONObject("album").getString("release_date");
            String genre = getSongGenreFromJSONObject(song);
            Integer popularity = song.getInt("popularity");
            Integer duration = song.getInt("duration_ms");

            //Create new Song object and fetch its YouTube URL;
            Song newSong = new Song(name, albumName, artists, releaseDate, genre, popularity, duration);
            newSong.setYouTubeURL(getYouTubeURL(newSong));
            songs.add(newSong);
        }
        return songs;
    }

    public String getSongGenreFromJSONObject(JSONObject song) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        String artistId = song.getJSONArray("artists").getJSONObject(0).getString("id");
        JSONArray JSONGenres = this.webClientHelper.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/artists/" + artistId).getJSONArray("genres");
        if (JSONGenres.isEmpty()) {
            return "";
        }
        //Use a regex pattern to properly format the genre string
        String genre = JSONGenres.toString().replaceAll("[\\[]*\\\"[\\]]*", " ").trim();
        return genre;
    }

    //Takes in a JSONArray of Artists, returns a list of artists
    public String getArtistName(JSONArray artists) {
        String names = "";
        if (artists.isEmpty()) {
            return names;
        }
        for (int i = 0; i < artists.length(); i++) {
            names += (artists.getJSONObject(i).getString("name"));
            if (i != artists.length() - 1) {
                names += ", ";
            }
        }
        return names;
    }

    //Takes in a List of Songs, find the value that is most occurring for a specific Table Field
    public String sort(Iterable<Song> songs, String field) {
        Map<String, Integer> map = new HashMap<>();
        if (field.equalsIgnoreCase("artists")) {
            for (Song song: songs) {
                if (song.getArtist().isEmpty()) {
                    continue;
                }
                String[] artists = song.getArtist().split(", ");
                for (String artist: artists) {
                    if (map.containsKey(artist)) {
                        map.put(artist, map.get(artist) + 1);
                    } else {
                        map.put(artist, 1);
                    }
                }
            }
            Integer maxValue = Collections.max(map.values());
            for (String key: map.keySet()) {
                if (map.get(key).equals(maxValue)) {
                    return key;
                }
            }
        }
        if (field.equalsIgnoreCase("genre")) {
            for (Song song: songs) {
                if (song.getGenre().isEmpty()) {
                    continue;
                }
                String[] genres = song.getGenre().split(" , ");
                for (String genre: genres) {
                    if (map.containsKey(genre)) {
                        map.put(genre, map.get(genre) + 1);
                    } else {
                        map.put(genre, 1);
                    }
                }
            }
            Integer maxValue = Collections.max(map.values());
            for (String key: map.keySet()) {
                if (map.get(key).equals(maxValue)) {
                    return key;
                }
            }
        }
        return null;
    }

    //Get the YouTube URL from a Song object
    public String getYouTubeURL (Song song) {
        String apikey = this.youTubeAPIKey;
        String URI = String.format("%s?part=snippet&type=video&videoCategoryId=10&topicId=/m/04rlf&order=relevance&key=%s&publishedAfter=%s&q=%s", "/youtube/v3/search", apikey, song.getReleaseDate() + "T00:00:00Z", song.getArtist().replaceAll(", ", " ") + " " + song.getName() + " -remix");
        JSONObject matchedUrls = new JSONObject(this.webClientHelper.request("get", new HttpHeaders(), "", String.class, 1024, "https://www.googleapis.com", URI));
        String url = "https://www.youtube.com/watch?v=" + matchedUrls.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        return url;
    }
}
