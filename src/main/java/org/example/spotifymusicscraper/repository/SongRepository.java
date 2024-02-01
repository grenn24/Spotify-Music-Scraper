package org.example.spotifymusicscraper.repository;

import org.springframework.data.repository.*;
import org.example.spotifymusicscraper.model.Song;
public interface SongRepository extends CrudRepository<Song, Integer> {

}
