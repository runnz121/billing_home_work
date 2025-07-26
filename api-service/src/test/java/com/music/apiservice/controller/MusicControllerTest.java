package com.music.apiservice.controller;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.service.MusicService;
import com.testUtils.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;


@WebFluxTest(controllers = MusicController.class)
class MusicControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private MusicService musicService;

    @Test
    void 연도_가수별_앨범_수_조회_전체_정상_호출() throws Exception {

        // 전체 조회시
        Integer year = null;
        Long artistId = null;
        int limit = 5;
        int offset = 0;

        AlbumCountResponse response = TestUtils.fileToObject("data/album-counts-ok.json", AlbumCountResponse.class);

        Mockito.when(musicService.getAlbumCounts(year, artistId, limit, offset))
                .thenReturn(Mono.just(response));

        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/albums/count")
                        .queryParam("year", year)
                        .queryParam("artistId", artistId)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(AlbumCountResponse.class)
                .value(res -> {
                    assertThat(res.content()).isNotEmpty();
                    assertThat(res.content().size()).isEqualTo(5);
                    assertThat(res.page()).isNotNull();
                });
    }
}