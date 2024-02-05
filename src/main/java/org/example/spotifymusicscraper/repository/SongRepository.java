package org.example.spotifymusicscraper.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.*;
import org.example.spotifymusicscraper.model.Song;

public interface SongRepository extends CrudRepository<Song, Integer> {
    List<Song> findByName(String name);
    void deleteByName(String name);
    List<Song> findAllByOrderByDurationDesc();
    List<Song> findAllByOrderByDurationAsc();
    @Query(value = "SELECT * FROM song ORDER BY popularity DESC LIMIT 3", nativeQuery = true)
    List<Song> findMostPopularSongs();
    @Query(value = "SELECT * FROM song ORDER BY release_date DESC LIMIT 3", nativeQuery = true)
    List<Song> findNewestSongs();
}
