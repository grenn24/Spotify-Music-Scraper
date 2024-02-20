package org.example.spotifymusicscraper.service;

import org.example.spotifymusicscraper.model.Song;
import org.example.spotifymusicscraper.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataAccessService {
    private final SongRepository songRepository;
    @Autowired
    //Using Constructor Injection to autowire SongRepository and WebClientHelper bean as a dependency
    public DataAccessService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    //Fetch songs from the database
    public Song fetchSongsFromDatabase(String name) {
        return songRepository.findByName(name).getFirst();
    }
    public Optional<Song> fetchSongsFromDatabase(Integer id) {
        return songRepository.findById(id);
    }
    public Iterable<Song> fetchSongsFromDatabase() {
        return songRepository.findAll();
    }

    //Add new songs to the database
    public void addSongsToDatabase(Song song) {
        if (song != null) {
            songRepository.save(song);
        }
    }
    public void addSongsToDatabase(List<Song> songs) {
        if (songs != null) {
            songRepository.saveAll(songs);
        }
    }

    //Delete songs from database
    public void deleteSongsFromDatabase() {
        songRepository.deleteAll();
    }
    public void deleteSongsFromDatabase(String songName) {
        songRepository.deleteByName(songName);
    }

    //Count songs inside database
    public Long countDatabase() {
        return songRepository.count();
    }

    //Fetch most popular songs from database
    public List<Song> fetchMostPopularSongsFromDatabase() {
        return songRepository.findMostPopularSongs();
    }

    //Fetch the newest songs from database
    public List<Song> fetchNewestSongsFromDatabase() {
        return songRepository.findNewestSongs();
    }

    //Fetch the longest duration song from database
    public Song fetchLongestDurationSongFromDatabase() {
        return songRepository.findAllByOrderByDurationDesc().getFirst();
    }

    //Fetch the shortest duration song from database
    public Song fetchShortestDurationSongFromDatabase() {
        return songRepository.findAllByOrderByDurationAsc().getFirst();
    }
}
