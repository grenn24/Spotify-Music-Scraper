package org.example.spotifymusicscraper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class SpotifyMusicScraperApplicationIntegrationTest {
    @Autowired
    private MockMvc mockMVC;
    @Test
    void contextLoads() {
    }

}
