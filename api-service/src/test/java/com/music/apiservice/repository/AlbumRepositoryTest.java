package com.music.apiservice.repository;

import com.music.apiservice.annotation.R2dbcTestConfig;
import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.model.entity.Album;
import com.music.apiservice.model.entity.Artist;
import com.music.apiservice.testUtils.TestTransactionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@R2dbcTestConfig
class AlbumRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Test
    void 전체_아티스트_정보를_찾아서_반환하는지_확인하는_테스트() {

        Mono<List<YearArtistsCountDto>> flow = setupData()
                .thenMany(albumRepository.findAlbumCountsGroupedByArtistAndYear(null, null, 20, 0))
                .collectList();

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(list -> {
                    assertThat(list).hasSize(3);
                    assertThat(list.get(0).artistName()).isEqualTo("BTS");
                    assertThat(list.get(0).albumCounts()).isEqualTo(4L);
                })
                .verifyComplete();
    }

    @Test
    void 연도로_필터링하여_앨범_판매_아티스트만_조회해서_반환하는지_확인하는_테스트() {

        Mono<List<YearArtistsCountDto>> flow = setupData()
                .thenMany(albumRepository.findAlbumCountsGroupedByArtistAndYear(2019, null, 10, 0))
                .collectList();

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list).extracting(YearArtistsCountDto::albumCounts).containsExactly(1L, 1L);
                    assertThat(list).extracting(YearArtistsCountDto::artistName).containsExactly("BTS", "IU");
                })
                .verifyComplete();
    }

    @Test
    void 아티스트_아이디로_필터링하여_특정_앨범만_조회되는지_확인하는_테스트() {

        Mono<List<YearArtistsCountDto>> flow = artistRepository.save(new Artist(null, "SoloArtist", "hashX"))
                .flatMapMany(artist -> Flux.just(
                                        Album.of(artist.getId(), "A1", "xh1", LocalDate.of(2020,1,1), 2020),
                                        Album.of(artist.getId(), "A2", "xh2", LocalDate.of(2021,1,1), 2021)
                                )
                                .flatMap(albumRepository::save)
                                .thenMany(albumRepository.findAlbumCountsGroupedByArtistAndYear(null, artist.getId(), 10, 0))
                )
                .collectList();

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).artistName()).isEqualTo("SoloArtist");
                    assertThat(list.get(0).albumCounts()).isEqualTo(2L);
                })
                .verifyComplete();
    }

    @Test
    void 페이징처리가_적용되는지_확인하는_테스트() {

        Mono<List<YearArtistsCountDto>> flow = setupData()
                .thenMany(albumRepository.findAlbumCountsGroupedByArtistAndYear(null, null, 1, 1))
                .collectList();

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).artistName()).isEqualTo("IU");
                })
                .verifyComplete();
    }

    @Test
    void 모든_아티스트_수를_반환하는지_확인하는_테스트() {

        Mono<Long> flow = setupData()
                .then(albumRepository.countArtistsByYearAndArtistId(null, null));

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void 연도별_카운트를_반환하는지_확인하는_테스트() {

        Mono<Long> flow = setupData()
                .then(albumRepository.countArtistsByYearAndArtistId(2019, null));

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void 아티스트별_카운트를_반환하는지_확인하는_테스트() {

        Mono<Long> flow = artistRepository.save(new Artist(null, "Solo", "hashX"))
                .flatMap(solo -> Flux.just(
                                        Album.of(solo.getId(), "S1", "sx1", LocalDate.of(2020,1,1), 2020),
                                        Album.of(solo.getId(), "S2", "sx2", LocalDate.of(2021,1,1), 2021)
                                )
                                .flatMap(albumRepository::save)
                                .then(albumRepository.countArtistsByYearAndArtistId(null, solo.getId()))
                );

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void 매치되는_경우가없을경우_0건이_반환되는지_확인하는_테스트() {
        Mono<Long> flow = setupData()
                .then(albumRepository.countArtistsByYearAndArtistId(1990, 9999L));

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .expectNext(0L)
                .verifyComplete();
    }


    private Mono<Void> setupData() {
        return Mono.when(
                artistRepository.save(new Artist(null, "IU", "hash1"))
                        .flatMapMany(iu -> Flux.just(
                                                Album.of(iu.getId(), "Palette",   "h2", LocalDate.of(2017,4,21), 2017),
                                                Album.of(iu.getId(), "Love Poem", "h3", LocalDate.of(2019,11,18), 2019),
                                                Album.of(iu.getId(), "LILAC",     "h4", LocalDate.of(2021,3,25), 2021)
                                        )
                                        .flatMap(albumRepository::save)
                        )
                        .then(),

                artistRepository.save(new Artist(null, "BTS", "hash5"))
                        .flatMapMany(bts -> Flux.just(
                                                Album.of(bts.getId(), "Love Yourself",   "h6", LocalDate.of(2017,9,18), 2017),
                                                Album.of(bts.getId(), "MAP OF THE SOUL", "h7", LocalDate.of(2019,4,12), 2019),
                                                Album.of(bts.getId(), "BE",              "h8", LocalDate.of(2020,11,20), 2020),
                                                Album.of(bts.getId(), "Butter",          "h9", LocalDate.of(2021,7,9), 2021)
                                        )
                                        .flatMap(albumRepository::save)
                        )
                        .then(),

                artistRepository.save(new Artist(null, "NewJeans", "h10"))
                        .flatMapMany(nj -> Flux.just(
                                                Album.of(nj.getId(), "New Jeans EP", "h11", LocalDate.of(2022,8,1), 2022),
                                                Album.of(nj.getId(), "OMG",          "h12", LocalDate.of(2023,1,2), 2023),
                                                Album.of(nj.getId(), "Get Up",       "h13", LocalDate.of(2023,7,21), 2023)
                                        )
                                        .flatMap(albumRepository::save)
                        )
                        .then()
        );
    }
}