package com.music.apiservice.service;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.exception.CustomException;
import com.music.apiservice.exception.error.LikeError;
import com.music.apiservice.model.dto.SongLikeCountDto;
import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.repository.AlbumRepository;
import com.music.apiservice.repository.SongRepository;
import com.music.apiservice.repository.UserSongLikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MusicServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private UserSongLikeRepository userSongLikeRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private TransactionalOperator transactionalOperator;

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

    @Test
    void 좋아요가_성공하면_빈_완료가_반환된다() {

        Long userId = 1L;
        Long songId = 2L;

        doReturn(Flux.just(1L))
                .when(transactionalOperator)
                .execute(any(TransactionCallback.class));

        StepVerifier.create(musicService.likeSong(userId, songId))
                .verifyComplete();
    }

    @Test
    void 이미_좋아요된_경우_CustomException_발생() {

        doReturn(Flux.error(new DuplicateKeyException("dup")))
                .when(transactionalOperator)
                .execute(any(TransactionCallback.class));

        StepVerifier.create(musicService.likeSong(1L, 2L))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(CustomException.class);
                    assertThat(((CustomException) ex).getCode())
                            .isEqualTo(LikeError.ALREADY_LIKE_ERROR);
                })
                .verify();
    }

    @Test
    void 최근1시간_좋아요증가_TOP10_조회_결과가_있으면_반환한다() {

        SongLikeCountDto dto1 = new SongLikeCountDto(1L, 5L);
        SongLikeCountDto dto2 = new SongLikeCountDto(2L, 3L);

        when(songRepository.findTop10InLastHour())
                .thenReturn(Flux.just(dto1, dto2));

        Flux<SongLikeCountDto> result = musicService.getTop10InLastHour();

        StepVerifier.create(result)
                .expectNext(dto1)
                .expectNext(dto2)
                .verifyComplete();
    }

    @Test
    void 최근1시간_좋아요증가_TOP10_조회_결과가_없으면_빈_리스트를_반환한다() {

        when(songRepository.findTop10InLastHour())
                .thenReturn(Flux.empty());

        Flux<SongLikeCountDto> result = musicService.getTop10InLastHour();

        StepVerifier.create(result)
                .verifyComplete();
    }
}