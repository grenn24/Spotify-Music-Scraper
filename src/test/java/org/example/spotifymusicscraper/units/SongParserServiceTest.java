package org.example.spotifymusicscraper.units;

import org.example.spotifymusicscraper.model.Song;
import org.example.spotifymusicscraper.service.SongParserService;
import org.example.spotifymusicscraper.service.WebClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SongParserServiceTest {
    @Mock
    WebClientService webClientService;
    SongParserService songParserService;

    @BeforeEach
    void createInstance() {
        songParserService = new SongParserService(webClientService);
    }

    @Test
    void verifyInstance() {
        assertNotNull(songParserService);
    }

    @Test
    void findMostFrequentFieldElement() {
        Song song1 = new Song("ME", "YOU", "Taylor Swift", "2015", "Pop", 999, 999);
        Song song2 = new Song("ME", "YOU", "Taylor Swift", "2015", "Rock", 999, 999);
        Song song3 = new Song("ME", "YOU", "Katy Perry", "2015", "Rock", 999, 999);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        assertEquals(songParserService.findMostFrequentFieldElement(songs, "artists"), "Taylor Swift");
        assertEquals(songParserService.findMostFrequentFieldElement(songs, "genre"), "Rock");
        assertThrows(NullPointerException.class, () -> songParserService.findMostFrequentFieldElement(new ArrayList<>(), "artists"));
        assertThrows(NullPointerException.class, () -> songParserService.findMostFrequentFieldElement(new ArrayList<>(), "genre"));
    }
}
