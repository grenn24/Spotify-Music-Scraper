package org.example.spotifymusicscraper.model;

import java.io.Serializable;
import java.util.Objects;

public class SongId implements Serializable {
    private String name;
    private String album;
    private String artists;
    private String releaseDate;
    private String genre;
    private Integer popularity;
    private Integer duration;

    public SongId(String name, String album, String artists, String releaseDate, String genre, Integer popularity, Integer duration) {
        this.name = name;
        this.album = album;
        this.artists = artists;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.popularity = popularity;
        this.duration = duration;
    }

    public SongId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SongId songId = (SongId) o;
        return Objects.equals(name, songId.name) && Objects.equals(album, songId.album) && Objects.equals(artists, songId.artists) && Objects.equals(releaseDate, songId.releaseDate) && Objects.equals(genre, songId.genre) && Objects.equals(popularity, songId.popularity) && Objects.equals(duration, songId.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, album, artists, releaseDate, genre, popularity, duration);
    }
}
