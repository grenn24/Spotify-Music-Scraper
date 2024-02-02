# Spotify Music Scraper
________________
Spotify Music Scraper is Java Spring Boot REST application that interacts with Spotify playlists through its API

The backend server is connected to a H2 in-memory database that stores the entry of different songs

Feel free to test out the other functionalities of the endpoints:

1. Retrieve a list of songs inside a playlist
2. Fetch working YouTube URL corresponding to each song in the playlist
3. Generates insights like most favourite artist

## Usage
- Must be used with working API credentials
- Can be hosted on any server using Maven

## Maven Dependencies
- **Spring Boot** - Framework for building Java restful Applications.
- **Spring Data JPA** - Library for mapping entity classes to database queries
- **Spring WebClient** - Part of Spring WebFlux, used for making HTTP requests and handling them asynchronously
- **org.JSON** - Used for manipulating JSON files inside Java