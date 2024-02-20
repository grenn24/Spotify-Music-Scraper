package org.example.spotifymusicscraper;

import org.example.spotifymusicscraper.model.Song;
import org.example.spotifymusicscraper.repository.SongRepository;
import org.example.spotifymusicscraper.service.DataAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class DataAccessServiceTest {
    @Mock
    SongRepository songRepository;
    DataAccessService dataAccessService;

    @BeforeEach
    void createInstance() {
        dataAccessService = new DataAccessService(songRepository);
      }

    @Test
    void verifyInstance() {
        assertNotNull(dataAccessService);
    }

    @Test
    void fetchSongsFromDatabase() throws Exception{
        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);

        //Mocking the behaviour of songRepository
        when(songRepository.findById(1)).thenReturn(Optional.of(song1));
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        when(songRepository.findByName("WE")).thenReturn(songs);
        songs.add(song3);
        when(songRepository.findAll()).thenReturn(songs);

        //FindByID
        Optional<Song> fetchedSong1 = dataAccessService.fetchSongsFromDatabase(1);
        if (fetchedSong1.isEmpty()) {
            throw new Exception("No Song Fetched");
        }
        assertEquals(song1, fetchedSong1.get());
        //FindByName
        Song fetchedSong2 = dataAccessService.fetchSongsFromDatabase("WE");
        assertEquals(song1, fetchedSong2);
        //FindAllSongs
        Iterable<Song> fetchedSongs = dataAccessService.fetchSongsFromDatabase();
        assertEquals(songs, fetchedSongs);
    }

    @Test
    void addSongsToDatabase() {
        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);
        List<Song> songs = new ArrayList<>();

        //Mocking the behaviour of songRepository
        doAnswer(i -> {
            songs.add(i.getArgument(0));
            return null;
        }).when(songRepository).save(any(Song.class));
        doAnswer(i -> {
            songs.addAll(i.getArgument(0));
            return null;
        }).when(songRepository).saveAll(anyList());

        //Add specific song
        dataAccessService.addSongsToDatabase(song1);
        assertTrue(songs.contains(song1));
        assertEquals(songs.size(), 1);

        //Add a list of Songs
        List<Song> songsToAdd = new ArrayList<>();
        songsToAdd.add(song2);
        songsToAdd.add(song3);
        dataAccessService.addSongsToDatabase(songsToAdd);
        assertTrue(songs.contains(song1));
        assertTrue(songs.contains(song2));
        assertTrue(songs.contains(song3));
        assertEquals(songs.size(), 3);

        //Null Value Added
        dataAccessService.addSongsToDatabase((Song) null);
        assertEquals(songs.size(), 3);
        dataAccessService.addSongsToDatabase((List<Song>) null);
        assertEquals(songs.size(), 3);
    }

    @Test
    void deleteSongsFromDatabase() {
        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);

        //Mocking the behaviour of songRepository
        doAnswer(i -> {
            songs.clear();
            return null;
        }).when(songRepository).deleteAll();
        doAnswer(i -> {
            songs.remove(song3);
            return null;
        }).when(songRepository).deleteByName("You");

        //Delete specific song
        dataAccessService.deleteSongsFromDatabase("You");
        assertFalse(songs.contains(song3));
        assertEquals(songs.size(), 2);

        //Delete specific song but does not exist
        dataAccessService.deleteSongsFromDatabase("Hi");
        assertEquals(songs.size(), 2);

        //Delete all songs
        dataAccessService.deleteSongsFromDatabase();
        assertTrue(songs.isEmpty());
    }

    @Test
    void countDatabase() {
        //Mocking the behaviour of songRepository
        when(songRepository.count()).thenReturn(5L);

        //Test
        assertEquals(dataAccessService.countDatabase(), 5L);
    }

    @Test
    void fetchMostPopularSongsFromDatabase() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));

        //Mocking the behaviour of songRepository
        when(songRepository.findMostPopularSongs()).thenReturn(songs);

        //Test
        assertEquals(dataAccessService.fetchMostPopularSongsFromDatabase(), songs);
    }

    @Test
    void fetchNewestSongsFromDatabase() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));

        //Mocking the behaviour of songRepository
        when(songRepository.findNewestSongs()).thenReturn(songs);

        //Test
        assertEquals(dataAccessService.fetchNewestSongsFromDatabase(), songs);
    }

    @Test
    void fetchLongestDurationSongFromDatabase() {
        List<Song> songs = new ArrayList<>();
        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 86);
        songs.add(song1);
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));

        //Mocking the behaviour of songRepository
        when(songRepository.findAllByOrderByDurationDesc()).thenReturn(songs);

        //Test
        assertEquals(dataAccessService.fetchLongestDurationSongFromDatabase(), song1);
    }

    @Test
    void fetchShortestDurationSongFromDatabase() {
        List<Song> songs = new ArrayList<>();

        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 48);
        songs.add(song1);
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 67));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 89));

        //Mocking the behaviour of songRepository
        when(songRepository.findAllByOrderByDurationAsc()).thenReturn(songs);

        //Test
        assertEquals(dataAccessService.fetchShortestDurationSongFromDatabase(), song1);
    }
}
