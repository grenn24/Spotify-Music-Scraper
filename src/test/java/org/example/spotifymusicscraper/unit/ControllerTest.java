package org.example.spotifymusicscraper.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spotifymusicscraper.controller.Controller;
import org.example.spotifymusicscraper.model.Song;
import org.example.spotifymusicscraper.service.DataAccessService;
import org.example.spotifymusicscraper.service.SongParserService;
import org.example.spotifymusicscraper.service.WebClientService;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = Controller.class)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    WebClientService webClientService;
    @MockBean
    DataAccessService dataAccessService;
    @MockBean
    SongParserService songParserService;

    @Test
    void getAllSongs() throws Exception {
        //Request Mapping
        mockMvc.perform(get("/scraper/list"))
                .andExpect(status().isOk());

        //Output Serialisation
        Song song1 = new Song("ME", "YOU", "Taylor Swift", "2015", "Pop", 999, 999);
        Song song2 = new Song("ME", "YOU", "Taylor Swift", "2015", "Rock", 999, 999);
        Song song3 = new Song("ME", "YOU", "Katy Perry", "2015", "Rock", 999, 999);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        when(dataAccessService.fetchSongsFromDatabase()).thenReturn(songs);

        MvcResult mvcResult = mockMvc.perform(get("/scraper/list"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedJSONResponseBody = objectMapper.writeValueAsString(songs);
        String actualJSONResponseBody = mvcResult.getResponse().getContentAsString();

        assertEquals(actualJSONResponseBody, expectedJSONResponseBody);

        //Exception Handling
        when(dataAccessService.fetchSongsFromDatabase()).thenThrow(NullPointerException.class);
        mockMvc.perform(get("/scraper/list"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getYouTubeURLAll() throws Exception {
        //Request Mapping
        mockMvc.perform(get("/scraper/list/url"))
                .andExpect(status().isOk());

        //Output Serialisation
        String url1 = "aaa";
        String url2 = "bbb";
        String url3 = "ccc";
        List<String> urls = new ArrayList<>();
        urls.add(url1);
        urls.add(url2);
        urls.add(url3);
        when(songParserService.fetchYouTubeURL(dataAccessService.fetchSongsFromDatabase())).thenReturn(urls);

        MvcResult mvcResult = mockMvc.perform(get("/scraper/list/url"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedJSONResponseBody = objectMapper.writeValueAsString(urls);
        String actualJSONResponseBody = mvcResult.getResponse().getContentAsString();

        assertEquals(actualJSONResponseBody, expectedJSONResponseBody);

        //Exception Handling
        when(songParserService.fetchYouTubeURL(dataAccessService.fetchSongsFromDatabase())).thenThrow(NullPointerException.class);
        mockMvc.perform(get("/scraper/list/url"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getYouTubeURL() throws Exception {
        //Request Mapping (path variables)
        mockMvc.perform(get("/scraper/list/23/url"))
                .andExpect(status().isOk());

        //Output Serialisation
        Song song1 = new Song("ME", "YOU", "Taylor Swift", "2015", "Pop", 999, 999);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        String url1 = "aaa";
        List<String> urls = new ArrayList<>();
        urls.add(url1);

        when(dataAccessService.fetchSongsFromDatabase(URLDecoder.decode("21", "UTF-8"))).thenReturn(songs);
        when(songParserService.fetchYouTubeURL(songs)).thenReturn(urls);

        MvcResult mvcResult = mockMvc.perform(get("/scraper/list/21/url"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedJSONResponseBody = objectMapper.writeValueAsString(urls);
        String actualJSONResponseBody = mvcResult.getResponse().getContentAsString();

        assertEquals(actualJSONResponseBody, expectedJSONResponseBody);

        //Exception Handling (FileNotException)
        when(dataAccessService.fetchSongsFromDatabase("22")).thenThrow(FileNotFoundException.class);
        mockMvc.perform(get("/scraper/list/22/url"))
                .andExpect(status().isNotFound());
    }

    @Test
    void insights() throws Exception {
        //Request Mapping
        mockMvc.perform(get("/scraper/list/insights"))
                .andExpect(status().isOk());

        //Output Serialisation
        Map<String, Object> insights = new LinkedHashMap<>();
        when(dataAccessService.countDatabase()).thenReturn(5L);
        insights.put("Total Number of Songs", dataAccessService.countDatabase());
        when(songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "artists")).thenReturn("Taylor");
        insights.put("Your Favourite Artist", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "artists"));
        when(songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "genre")).thenReturn("Pop");
        insights.put("Your Favourite Genre", songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "genre"));
        List<Song> songs1 = new ArrayList<>();
        songs1.add(new Song("ME", "YOU", "Taylor Swift", "2015", "Pop", 999, 999));
        songs1.add(new Song("ME", "YOU", "Taylor Swift", "2015", "Rock", 999, 999));
        songs1.add(new Song("ME", "YOU", "Katy Perry", "2015", "Rock", 999, 999));
        when(dataAccessService.fetchMostPopularSongsFromDatabase()).thenReturn(songs1);
        insights.put("Most Popular Songs", dataAccessService.fetchMostPopularSongsFromDatabase());
        List<Song> songs2 = new ArrayList<>();
        songs2.add(new Song("YOU", "YOU", "Taylor Swift", "2015", "Pop", 999, 999));
        songs2.add(new Song("ME", "YOU", "Taylor Swift", "2015", "Rock", 999, 999));
        songs2.add(new Song("ME", "YOU", "Katy Perry", "2015", "Rock", 999, 999));
        when(dataAccessService.fetchNewestSongsFromDatabase()).thenReturn(songs2);
        insights.put("Newest Songs", dataAccessService.fetchNewestSongsFromDatabase());
        when(dataAccessService.fetchLongestDurationSongFromDatabase()).thenReturn(new Song("ME", "YOU", "Katy Perry", "2010", "Rock", 567, 785));
        insights.put("Longest Duration Song", dataAccessService.fetchLongestDurationSongFromDatabase());
        when(dataAccessService.fetchShortestDurationSongFromDatabase()).thenReturn(new Song("ME", "YOU", "Katy Perry", "2010", "Rock", 567, 67));
        insights.put("Shortest Duration Song", dataAccessService.fetchShortestDurationSongFromDatabase());

        MvcResult mvcResult = mockMvc.perform(get("/scraper/list/insights"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedJSONResponseBody = objectMapper.writeValueAsString(insights);
        String actualJSONResponseBody = mvcResult.getResponse().getContentAsString();

        assertEquals(actualJSONResponseBody, expectedJSONResponseBody);

        //Exception Handling (NullPointerException)
        when(songParserService.findMostFrequentFieldElement(dataAccessService.fetchSongsFromDatabase(), "artists")).thenThrow(NullPointerException.class);
        mockMvc.perform(get("/scraper/list/insights"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAPIAccessToken() throws Exception {
        //Request Mapping
        mockMvc.perform(get("/scraper/spotifytoken"))
                .andExpect(status().isOk());

        //Output Serialisation
        when(webClientService.getAPIAccessToken()).thenReturn("12345678abcdefg");

        MvcResult mvcResult = mockMvc.perform(get("/scraper/spotifytoken"))
                .andExpect(status().isOk())
                .andReturn();
        String expectedJSONResponseBody = "12345678abcdefg";
        String actualJSONResponseBody = mvcResult.getResponse().getContentAsString();

        assertEquals(expectedJSONResponseBody, actualJSONResponseBody);
    }

    @Test
    void addNewPlaylist() throws Exception {
        //Request Mapping
        mockMvc.perform(put("/scraper/list/456"))
                .andExpect(status().isCreated());

        //Business Logic
        when(webClientService.getAPIAccessToken()).thenReturn("123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + webClientService.getAPIAccessToken());
        mockMvc.perform(put("/scraper/list/456"));
        verify(webClientService).requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/playlists/" + "456" + "/tracks");

        //Exception Handling (FileNotFoundException)
        when(webClientService.requestJSONObject("get", headers, "", 1024, "https://api.spotify.com", "/v1/playlists/" + "456" + "/tracks")).thenThrow(FileNotFoundException.class);
        mockMvc.perform(put("/scraper/list/456"))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetDatabase() throws Exception {
        //Request Mapping
        mockMvc.perform(delete("/scraper/list"))
                .andExpect(status().isOk());

        //Business Logic
        verify(dataAccessService).deleteSongsFromDatabase();
    }

    @Test
    void deleteSong() throws Exception {
        //Request Mapping
        mockMvc.perform(delete("/scraper/list/YOU"))
                .andExpect(status().isOk());

        //Business Logic
        verify(dataAccessService).deleteSongsFromDatabase("YOU");

        //Exception Handling (FileNotFoundException)
        doThrow(FileNotFoundException.class).when(dataAccessService).deleteSongsFromDatabase("YOU");
        mockMvc.perform(delete("/scraper/list/YOU"))
                .andExpect(status().isNotFound());
    }
}
