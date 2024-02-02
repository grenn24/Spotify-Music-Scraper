package org.example.spotifymusicscraper.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="SONG")
public class Song {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String albumName;
    @ElementCollection
    private List<String> artists;
    private Integer releaseYear;
    private String genre;

    public Song(String name, String albumName, List<String> artists, Integer releaseYear, String genre) {
        this.name = name;
        this.albumName = albumName;
        this.artists = artists;
        this.releaseYear = releaseYear;
        this.genre = genre;
    }

    public Song() {
    }

    //Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public List<String> getArtist() {
        return this.artists;
    }

    public void setArtist(List<String> artist) {
        this.artists = artist;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", albumName='" + albumName + '\'' +
                ", Artist='" + artists + '\'' +
                ", releaseYear=" + releaseYear +
                ", genre='" + genre + '\'' +
                '}';
    }
}
