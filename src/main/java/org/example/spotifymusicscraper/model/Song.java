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
    private Integer popularity;
    private Integer duration;

    public Song(String name, String albumName, List<String> artists, Integer releaseYear, String genre, Integer popularity, Integer duration) {
        this.name = name;
        this.albumName = albumName;
        this.artists = artists;
        this.releaseYear = releaseYear;
        this.genre = genre;
        this.popularity = popularity;
        this.duration = duration;
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

    public Integer getPopularity() { return popularity; }

    public void setPopularity(Integer popularity) { this.popularity = popularity; }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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
                ", popularity=" + popularity +
                ", duration=" + duration +
                '}';
    }
}
