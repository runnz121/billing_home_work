package com.music.apiservice.controller;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.model.dto.SongLikeCountDto;
import com.music.apiservice.service.MusicService;
import com.music.apiservice.testUtils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


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

        when(musicService.getAlbumCounts(year, artistId, limit, offset))
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

    @Test
    void 노래별_좋아요증가_API_호출_성공() {
        Long userId = 123L;
        Long songId = 456L;

        when(musicService.likeSong(userId, songId)).thenReturn(Mono.empty());

        webClient.post()
                .uri("/api/users/{userId}/songs/{songId}/like", userId, songId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void 노래별_좋아요이미존재시_에러발생한다() {
        Long userId = 123L;
        Long songId = 456L;

        when(musicService.likeSong(userId, songId))
                .thenReturn(Mono.error(new RuntimeException("이미 좋아요 한 노래입니다")));

        webClient.post()
                .uri("/api/users/{userId}/songs/{songId}/like", userId, songId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void 최근1시간_좋아요증가_TOP10_조회_API_호출_성공() {

        SongLikeCountDto dto1 = new SongLikeCountDto(101L, 50L);
        SongLikeCountDto dto2 = new SongLikeCountDto(202L, 30L);

        when(musicService.getTop10InLastHour())
                .thenReturn(Flux.just(dto1, dto2));

        webClient.get()
                .uri("/api/songs/top-liked")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(SongLikeCountDto.class)
                .hasSize(2)
                .value(list -> {
                    // dto1
                    assertThat(list.get(0).songId()).isEqualTo(101L);
                    assertThat(list.get(0).count()).isEqualTo(50L);

                    // dto2
                    assertThat(list.get(1).songId()).isEqualTo(202L);
                    assertThat(list.get(1).count()).isEqualTo(30L);
                });
    }

    @Test
    void 최근1시간_좋아요증가_TOP10_조회_API_결과없을때_빈리스트반환() {

        when(musicService.getTop10InLastHour())
                .thenReturn(Flux.empty());

        webClient.get()
                .uri("/api/songs/top-liked")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SongLikeCountDto.class)
                .hasSize(0);
    }
}