package org.example.spotifymusicscraper.repository;

import java.util.List;

import org.springframework.data.repository.*;
import org.example.spotifymusicscraper.model.Song;

public interface SongRepository extends CrudRepository<Song, Integer> {
    List<Song> findByName(String name);
    void deleteByName(String name);
    List<Song> findAllByOrderByPopularityDesc();
    List<Song> findAllByOrderByReleaseYearDesc();
    List<Song> findAllByOrderByDurationDesc();
    List<Song> findAllByOrderByDurationAsc();
}
