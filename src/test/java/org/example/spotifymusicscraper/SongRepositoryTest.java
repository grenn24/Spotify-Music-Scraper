package org.example.spotifymusicscraper;

import org.example.spotifymusicscraper.model.Song;
import org.example.spotifymusicscraper.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class SongRepositoryTest {

    SongRepository songRepository;

    @Autowired
    //Using Constructor Injection to autowire SongRepository bean as a dependency
    SongRepositoryTest(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Test
    void save() {
        Song song1 = new Song("WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        assertEquals(songRepository.save(song1), song1);
        assertEquals(songRepository.count(), 1);
        Song song2 = new Song("YOU", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        assertEquals(songRepository.save(song2), song2);
        assertEquals(songRepository.count(), 2);
        //Duplicate Songs Being Saved
        assertEquals(songRepository.save(song2), song2);
        assertEquals(songRepository.count(), 2);
    }

    @Test
    void saveAll() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));
        assertEquals(songRepository.saveAll(songs), songs);
        assertEquals(songRepository.count(), 3);
        //Duplicate Songs Being Saved
        songs.clear();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "K-Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Rock", 999, 64));
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        assertEquals(songRepository.saveAll(songs), songs);
        assertEquals(songRepository.count(), 5);
    }

    @Test
    void findAll() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));
        songRepository.saveAll(songs);
        assertEquals(songRepository.findAll(), songs);
    }

    @Test
    void findByName() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));
        songRepository.saveAll(songs);
        songs.remove(2);
        assertEquals(songRepository.findByName("WE"), songs);
    }

    @Test
    void deleteAll() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));
        songRepository.saveAll(songs);
        songRepository.deleteAll(songs);
        assertEquals(songRepository.count(), 0);
        assertEquals(songRepository.findAll(), new ArrayList<>());
    }

    @Test
    void deleteByName() {
        List<Song> songs = new ArrayList<>();
        songs.add(new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64));
        songs.add(new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 64));
        songs.add(new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32));
        songRepository.saveAll(songs);
        songRepository.deleteByName("WE");
        assertEquals(songRepository.count(), 1);
        assertEquals(songRepository.findAll(), songs.subList(2, 3));
    }

    @Test
    void findAllByOrderByDurationDesc() {
        Song song1 = new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 78);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        List<Song> songsToBeFetched = new ArrayList<>();
        songsToBeFetched.add(song2);
        songsToBeFetched.add(song1);
        songsToBeFetched.add(song3);
        songRepository.saveAll(songs);
        assertEquals(songRepository.findAllByOrderByDurationDesc(), songsToBeFetched);
    }

    @Test
    void findAllByOrderByDurationAsc() {
        Song song1 = new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 999, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 78);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        List<Song> songsToBeFetched = new ArrayList<>();
        songsToBeFetched.add(song3);
        songsToBeFetched.add(song1);
        songsToBeFetched.add(song2);
        songRepository.saveAll(songs);
        assertEquals(songRepository.findAllByOrderByDurationAsc(), songsToBeFetched);
    }

    @Test
    void findMostPopularSongs() {
        Song song1 = new Song( "WE", "Lover", "Taylor Swift", "2024", "Pop", 756, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020", "Pop", 999, 78);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2023", "Pop", 653, 32);
        Song song4 = new Song("swtw", "Be", "Miley Cyrus", "2023", "Pop", 567, 32);
        Song song5 = new Song("w4tw", "4ee", "Miley Cyrus", "2023", "Pop", 873, 32);
        Song song6 = new Song("rtjrdth", "Be", "Miley Cyrus", "2023", "Pop", 978, 32);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(song5);
        songs.add(song6);
        List<Song> songsToBeFetched = new ArrayList<>();
        songsToBeFetched.add(song2);
        songsToBeFetched.add(song6);
        songsToBeFetched.add(song5);
        songRepository.saveAll(songs);
        assertEquals(songRepository.findMostPopularSongs(), songsToBeFetched);
    }

    @Test
    void findMostNewestSongs() {
        Song song1 = new Song( "WE", "Lover", "Taylor Swift", "2024-10-21", "Pop", 756, 64);
        Song song2 = new Song("WE", "Lover", "Taylor Swift", "2020-08-14", "Pop", 999, 78);
        Song song3 = new Song("You", "Be", "Miley Cyrus", "2024-10-20", "Pop", 653, 32);
        Song song4 = new Song("swtw", "Be", "Miley Cyrus", "2023-09-15", "Pop", 567, 32);
        Song song5 = new Song("w4tw", "4ee", "Miley Cyrus", "2023-05-06", "Pop", 873, 32);
        Song song6 = new Song("rtjrdth", "Be", "Miley Cyrus", "2023-03-13", "Pop", 978, 32);
        List<Song> songs = new ArrayList<>();
        songs.add(song1);
        songs.add(song2);
        songs.add(song3);
        songs.add(song4);
        songs.add(song5);
        songs.add(song6);
        List<Song> songsToBeFetched = new ArrayList<>();
        songsToBeFetched.add(song1);
        songsToBeFetched.add(song3);
        songsToBeFetched.add(song4);
        songRepository.saveAll(songs);
        assertEquals(songRepository.findNewestSongs(), songsToBeFetched);
    }
}
