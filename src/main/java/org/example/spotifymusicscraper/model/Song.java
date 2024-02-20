package org.example.spotifymusicscraper.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(schema = "public", name = "Song")
@IdClass(SongId.class)
public class Song {
    @Id
    @Column(name = "Name")
    private String name;
    @Id
    @Column(name = "Album")
    private String album;
    @Id
    @Column(name = "Artists")
    private String artists;
    @Id
    @Column(name = "Release_Date")
    private String releaseDate;
    @Id
    @Column(name = "Genre")
    private String genre;
    @Id
    @Column(name = "Popularity")
    private Integer popularity;
    @Id
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
                "name='" + name + '\'' +
                ", album='" + album + '\'' +
                ", artists='" + artists + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", genre='" + genre + '\'' +
                ", popularity=" + popularity +
                ", duration=" + duration +
                ", youTubeURL='" + youTubeURL + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Song)) return false;
        Song song = (Song) o;
        return Objects.equals(name, song.name) && Objects.equals(album, song.album) && Objects.equals(artists, song.artists) && Objects.equals(releaseDate, song.releaseDate) && Objects.equals(genre, song.genre) && Objects.equals(popularity, song.popularity) && Objects.equals(duration, song.duration) && Objects.equals(youTubeURL, song.youTubeURL);
    }
}
