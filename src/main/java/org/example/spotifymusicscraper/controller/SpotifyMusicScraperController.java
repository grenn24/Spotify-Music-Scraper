package org.example.spotifymusicscraper.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.nio.charset.StandardCharsets;

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

    @GetMapping("/scraper/list")
    //Fetch all the songs in the database
    public Iterable<Song> getAllSongs() {
        return this.songRepository.findAll();
    }

    @PutMapping("/scraper/add/{playlistId}")
    //Fetches a list of Songs inside a playlist and maps each song to an entry in the database
    public List<Song> searchPlaylist(@PathVariable(name="playlistId", required = false) String playlistId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + getAPIAccessToken());
        JSONObject JSONSongs = new JSONObject(this.webClientHelper.request("get", headers, "", String.class, 1024, "https://api.spotify.com", "/v1/playlists/" + playlistId + "/tracks"));

        //Convert JSON file into a List of Songs
        List<Song> songs = songParser(JSONSongs);
        this.songRepository.saveAll(songs);
        return songs;
    }

    @GetMapping("/scraper/spotifytoken")
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

    @GetMapping("/scraper/list/fetch/url")
    //Retrieve YouTube URL for all songs in the database
    public List<String> getYouTubeURLAll() {
        Iterable<Song> songs = getAllSongs();
        List<String> songURLs = new ArrayList<>();
        for (Song song: songs) {
            String apikey = "AIzaSyDtARoJS03eCtKJJlpZhi_eExhSMM2kDWs";
            String URI = String.format("%s?part=snippet&type=video&videoCategoryId=10&topicId=/m/04rlf&order=relevance&key=%s&publishedAfter=%s&q=%s", "/youtube/v3/search", apikey, song.getReleaseYear() + "-01-01T00:00:00Z", song.getArtist().getFirst() + " " + song.getName() + " -remix");
            JSONObject matchedUrls = new JSONObject(this.webClientHelper.request("get", new HttpHeaders(), "", String.class, 1024, "https://www.googleapis.com", URI));
            songURLs.add("https://www.youtube.com/watch?v=" + matchedUrls.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId"));
        }
        return songURLs;
    }

    @GetMapping("/scraper/list/{songName}/fetch/url")
    //Retrieve YouTube URL for a specific song in the database
    public String getYouTubeURL(@PathVariable(name="songName") String songName) {
        Song song = null;
        try {
            song = this.songRepository.findByName(URLDecoder.decode(songName, "UTF-8")).getFirst();
        } catch (UnsupportedEncodingException e) {

        }
        String apikey = "AIzaSyDtARoJS03eCtKJJlpZhi_eExhSMM2kDWs";
        String URI = String.format("%s?part=snippet&type=video&videoCategoryId=10&topicId=/m/04rlf&order=relevance&key=%s&publishedAfter=%s&q=%s", "/youtube/v3/search", apikey, song.getReleaseYear() + "-01-01T00:00:00Z", song.getArtist().getFirst() + " " + song.getName() + " -remix");
        JSONObject matchedUrls = new JSONObject(this.webClientHelper.request("get", new HttpHeaders(), "", String.class, 1024, "https://www.googleapis.com", URI));
        String url = "https://www.youtube.com/watch?v=" + matchedUrls.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");

        return url;
    }

    @GetMapping("/scraper/list/fetch/insights")
    //Generates insights based on the songs in the database
    public Map<String, Object> insights() {
        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("Total Number of Songs", this.songRepository.count());
        insights.put("Your Favourite Artist", artistSort(this.songRepository.findAll(), "artists"));
        insights.put("Your Favourite Genre", artistSort(this.songRepository.findAll(), "genre"));
        insights.put("Top 3 Songs Currently Popular", this.songRepository.findAllByOrderByPopularityDesc().subList(0, Math.min(this.songRepository.findAllByOrderByPopularityDesc().size(), 3)));
        insights.put("Top 3 Songs Recently Released", this.songRepository.findAllByOrderByReleaseYearDesc().subList(0, Math.min(this.songRepository.findAllByOrderByReleaseYearDesc().size(), 3)));
        insights.put("Longest Duration Song", this.songRepository.findAllByOrderByDurationDesc().getFirst());
        insights.put("Shortest Duration Song", this.songRepository.findAllByOrderByDurationAsc().getFirst());

        return insights;
    }

    @DeleteMapping("/scraper/list/delete")
    //Delete all songs in the database
    public void resetDatabase() {
        this.songRepository.deleteAll();
    }

    @DeleteMapping("/scraper/list/{songName}/delete")
    //Delete a specific song in the database
    public void deleteSong(@PathVariable(name="songName") String songName) {
        this.songRepository.deleteByName(songName);
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
            Integer popularity = song.getInt("popularity");
            Integer duration = song.getInt("duration_ms");
            songs.add(new Song(name, albumName, artists, releaseYear, genre, popularity, duration));
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

    //Takes in a List of Songs, find the value that is most occurring for a specific Table Field
    public String artistSort(Iterable<Song> songs, String field) {
        Map<String, Integer> map = new HashMap<>();
        if (field.equalsIgnoreCase("artists")) {
            for (Song song: songs) {
                for (String artist: song.getArtist()) {
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
                if (map.containsKey(song.getGenre())) {
                    map.put(song.getGenre(), map.get(song.getGenre()) + 1);
                } else {
                    map.put(song.getGenre(), 1);
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
}
