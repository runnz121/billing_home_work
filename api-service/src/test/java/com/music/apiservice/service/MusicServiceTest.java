package com.music.apiservice.service;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.repository.AlbumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @InjectMocks
    private MusicService musicService;

    @Test
    void 필터없음일때_모든아티스트_앨범수조회하는지_확인하는_테스트() {

        YearArtistsCountDto dto1 = YearArtistsCountDto.of(1L, "IU", 3L);
        YearArtistsCountDto dto2 = YearArtistsCountDto.of(2L, "BTS", 4L);

        when(albumRepository.findAlbumCountsGroupedByArtistAndYear(null, null, 10, 0))
                .thenReturn(Flux.just(dto1, dto2));
        when(albumRepository.countArtistsByYearAndArtistId(null, null))
                .thenReturn(Mono.just(2L));

        Mono<AlbumCountResponse> result = musicService.getAlbumCounts(null, null, 10, 0);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.year()).isNull();
                    assertThat(resp.artistId()).isNull();
                    assertThat(resp.page().totalElements()).isEqualTo(2L);
                    assertThat(resp.page().pageSize()).isEqualTo(10);
                    assertThat(resp.content()).containsExactly(dto1, dto2);
                })
                .verifyComplete();
    }

    @Test
    void 연도필터일때_해당연도_앨범수조회하는지_확인하는_테스트() {

        YearArtistsCountDto dto = YearArtistsCountDto.of(1L, "IU", 1L);
        when(albumRepository.findAlbumCountsGroupedByArtistAndYear(2019, null, 5, 0))
                .thenReturn(Flux.just(dto));
        when(albumRepository.countArtistsByYearAndArtistId(2019, null))
                .thenReturn(Mono.just(1L));

        Mono<AlbumCountResponse> result = musicService.getAlbumCounts(2019, null, 5, 0);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.year()).isEqualTo(2019);
                    assertThat(resp.artistId()).isNull();
                    assertThat(resp.page().totalElements()).isEqualTo(1L);
                    assertThat(resp.page().pageSize()).isEqualTo(5);
                    assertThat(resp.content()).containsExactly(dto);
                })
                .verifyComplete();
    }

    @Test
    void 아티스트필터일때_해당아티스트_앨범수조회하는지_확인하는_테스트() {

        YearArtistsCountDto dto = YearArtistsCountDto.of(2L, "BTS", 4L);
        when(albumRepository.findAlbumCountsGroupedByArtistAndYear(null, 2L, 5, 0))
                .thenReturn(Flux.just(dto));
        when(albumRepository.countArtistsByYearAndArtistId(null, 2L))
                .thenReturn(Mono.just(1L));

        Mono<AlbumCountResponse> result = musicService.getAlbumCounts(null, 2L, 5, 0);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.year()).isNull();
                    assertThat(resp.artistId()).isEqualTo(2L);
                    assertThat(resp.page().totalElements()).isEqualTo(1L);
                    assertThat(resp.page().pageSize()).isEqualTo(5);
                    assertThat(resp.content()).containsExactly(dto);
                })
                .verifyComplete();
    }

    @Test
    void 일치하는결과없을때_빈내용반환하는지_확인하는_테스트() {

        when(albumRepository.findAlbumCountsGroupedByArtistAndYear(1990, 999L, 5, 0))
                .thenReturn(Flux.empty());
        when(albumRepository.countArtistsByYearAndArtistId(1990, 999L))
                .thenReturn(Mono.just(0L));

        Mono<AlbumCountResponse> result = musicService.getAlbumCounts(1990, 999L, 5, 0);

        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.content()).isEmpty();
                    assertThat(resp.page().totalElements()).isZero();
                    assertThat(resp.page().pageSize()).isEqualTo(5);
                })
                .verifyComplete();
    }
}