package com.music.apiservice.repository;

import com.music.apiservice.annotation.R2dbcTestConfig;
import com.music.apiservice.model.dto.SongLikeCountDto;
import com.music.apiservice.model.entity.Album;
import com.music.apiservice.model.entity.Artist;
import com.music.apiservice.model.entity.Song;
import com.music.apiservice.model.entity.UserSongLike;
import com.music.apiservice.testUtils.TestTransactionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@R2dbcTestConfig
class SongRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserSongLikeRepository userSongLikeRepository;

    @Test
    void 좋아요_정상_갓수_1증가하고_유저_좋아요_적재되는지_확인하는_테스트() {

        Mono<String> flow = createTestSong("TestArtistHash", "TestArtist", "thash")
                .flatMap(savedSong -> {
                    Long id = savedSong.getId();
                    return songRepository.incrementLikeCount(id)
                            .flatMap(rows ->
                                    songRepository.findById(id)
                                            .map(song -> rows + ":" + song.getLikeCount())
                            );
                });

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(result -> {
                    String[] parts = result.split(":");
                    assertThat(Long.parseLong(parts[0])).isEqualTo(1L);
                    assertThat(Long.parseLong(parts[1])).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void 존재하지않는_노래는_0을_eemit하는지_확인하는_테스트() {

        Mono<Long> flow = songRepository.incrementLikeCount(99999L);

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void 최근1시간내_좋아요증가수_내림차순으로반환_하는지_확인하는_테스트() {

        LocalDateTime now = LocalDateTime.now();

        Mono<Void> setup = getSongs(now);

        Mono<List<SongLikeCountDto>> flow = setup
                .thenMany(songRepository.findTop10InLastHour())
                .collectList();

        StepVerifier.create(TestTransactionUtils.withRollBack(flow))
                .assertNext(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list.get(0).count()).isEqualTo(2L);
                    assertThat(list.get(1).count()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    private Mono<Void> getSongs(LocalDateTime now) {

        Mono<Void> song1 = createTestSong("A1", "Album1", "S1")
                .flatMap(song -> Mono.when(
                        userSongLikeRepository.save(UserSongLike.of(11L, song.getId(), now.minusMinutes(10))),
                        userSongLikeRepository.save(UserSongLike.of(12L, song.getId(), now.minusMinutes(20)))
                )).then();

        Mono<Void> song2 = createTestSong("A2", "Album2", "S2")
                .flatMap(song ->
                        userSongLikeRepository.save(UserSongLike.of(21L, song.getId(), now.minusMinutes(30)))
                ).then();

        Mono<Void> song3 = createTestSong("A3", "Album3", "S3")
                .flatMap(song -> Flux.just(
                                        now.minusHours(2),
                                        now.minusHours(3),
                                        now.minusHours(4)
                                )
                                .flatMap(dt -> userSongLikeRepository.save(UserSongLike.of(31L, song.getId(), dt)))
                                .then()
                );

        return Mono.when(song1, song2, song3);
    }

    @Test
    void 조건에맞는좋아요없으면_빈리스트반환하는지_확인하는_테스트() {

        StepVerifier.create(TestTransactionUtils.withRollBack(
                        songRepository.findTop10InLastHour().collectList()
                ))
                .assertNext(list -> assertThat(list).isEmpty())
                .verifyComplete();
    }

    private Mono<Song> createTestSong(String artistHash,
                                      String albumTitle,
                                      String songHash) {

        return artistRepository.save(new Artist(null, "Artist" + artistHash, artistHash))
                .flatMap(artist ->
                        albumRepository.save(new Album(null, artist.getId(),
                                albumTitle, albumTitle + "Hash", null, null))
                )
                .flatMap(album ->
                        songRepository.save(new Song(null,
                                album.getId(),
                                "Song" + songHash,
                                songHash,
                                0L))
                );
    }
}