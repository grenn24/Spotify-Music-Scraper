package org.example.spotifymusicscraper.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(schema = "public", name = "Song")
public class Song {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "Name")
    private String name;
    @Column(name = "Album")
    private String album;
    @Column(name = "Artists")
    private String artists;
    @Column(name = "Release_Date")
    private String releaseDate;
    @Column(name = "Genre")
    private String genre;
    @Column(name = "Popularity")
    private Integer popularity;
    @Column(name = "Duration")
    private Integer duration;
    @Column(name = "YouTube_URL")
    private String youTubeURL;

    public Song(String name, String album, String artists, String releaseDate, String genre, Integer popularity, Integer duration) {
        this.name = name;
        this.album = album;
        this.artists = artists;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.popularity = popularity;
        this.duration = duration;
        this.youTubeURL = youTubeURL;
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
        return album;
    }

    public void setAlbumName(String albumName) {
        this.album = albumName;
    }

    public String getArtist() {
        return this.artists;
    }

    public void setArtist(String artist) {
        this.artists = artist;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
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

    public String getYouTubeURL() {
        return youTubeURL;
    }

    public void setYouTubeURL(String youTubeURL) {
        this.youTubeURL = youTubeURL;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", album='" + album + '\'' +
                ", artists=" + artists +
                ", releaseDate=" + releaseDate +
                ", genre='" + genre + '\'' +
                ", popularity=" + popularity +
                ", duration=" + duration +
                ", youTubeURL='" + youTubeURL + '\'' +
                '}';
    }
}
