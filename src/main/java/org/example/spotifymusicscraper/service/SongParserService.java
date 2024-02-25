package org.example.spotifymusicscraper.service;

import org.example.spotifymusicscraper.model.Song;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileNotFoundException;
import java.util.*;

@Service
public class SongParserService {
    private final WebClientService webClientService;

    //Using value injection to automatically initialise credentials from application.properties file
    @Value("${youtube.apikey}")
    private String youTubeAPIKey;

    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public SongParserService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    //Fetch YouTube URL corresponding to a Song
    public String fetchYouTubeURL(Song song) {
        String apikey = this.youTubeAPIKey;
        String URI = String.format("%s?part=snippet&type=video&videoCategoryId=10&topicId=/m/04rlf&order=relevance&key=%s&publishedAfter=%s&q=%s", "/youtube/v3/search", apikey, song.getReleaseDate() + "T00:00:00Z", song.getArtist().replaceAll(", ", " ") + " " + song.getName() + " -remix");
        JSONObject matchedUrls = new JSONObject(webClientService.request("get", new HttpHeaders(), "", String.class, 1024, "https://www.googleapis.com", URI));
        String url = "https://www.youtube.com/watch?v=" + matchedUrls.getJSONArray("items").getJSONObject(0).getJSONObject("id").getString("videoId");
        return url;
    }

    //Fetch YouTube URL corresponding to a list of Songs
    public List<String> fetchYouTubeURL(Iterable<Song> songs) {
        if (((List<Song>) songs).isEmpty()) {
            throw new NullPointerException("No songs available for parsing");
        }
        List<String> songURLs = new ArrayList<>();
        for (Song song: songs) {
            songURLs.add(fetchYouTubeURL(song));
        }
        return songURLs;
    }

    //Finds the most frequent element in a given field (e.g. artists) among a list of songs
    public String findMostFrequentFieldElement(Iterable<Song> songs, String field) {
        if (((List<Song>) songs).isEmpty()) {
            throw new NullPointerException("No songs available for parsing");
        }
        Map<String, Integer> map = new HashMap<>();
        //Artists Field
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
        //Genre Field
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

    //Converts a JSONObject to a list of Songs
    public List<Song> convertJSONObjectToListOfSongs(JSONObject jsonObject) {
        List<Song> songs = new ArrayList<>();
        JSONArray arrayOfSongs = jsonObject.getJSONArray("items");
        for (int i = 0; i < arrayOfSongs.length(); i++) {
            JSONObject song = arrayOfSongs.getJSONObject(i).getJSONObject("track");
            String name = song.getString("name");
            String albumName = song.getJSONObject("album").getString("name");
            String artists = convertJSONArrayToArtists(song.getJSONArray("artists"));
            String releaseDate  = song.getJSONObject("album").getString("release_date");
            String genre = convertJSONObjectToGenre(song);
            Integer popularity = song.getInt("popularity");
            Integer duration = song.getInt("duration_ms");

            //Create new Song object and fetch its YouTube URL;
            Song newSong = new Song(name, albumName, artists, releaseDate, genre, popularity, duration);
            //newSong.setYouTubeURL(fetchYouTubeURL(newSong));
            songs.add(newSong);
        }
        return songs;
    }

    public String convertJSONObjectToGenre(JSONObject song) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + webClientService.getAPIAccessToken());
        String artistId = song.getJSONArray("artists").getJSONObject(0).getString("id");
        JSONArray JSONGenres = null;
        try {
            JSONGenres = webClientService.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/artists/" + artistId).getJSONArray("genres");
        } catch (FileNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artist ID does not exist", e);
        }
        if (JSONGenres.isEmpty()) {
            return "";
        }
        //Use a regex pattern to properly format the genre string
        String genre = JSONGenres.toString().replaceAll("[\\[]*\\\"[\\]]*", " ").trim();
        return genre;
    }

    //Takes in a JSONArray of Artists, returns a list of artists
    public String convertJSONArrayToArtists(JSONArray artists) {
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
}
