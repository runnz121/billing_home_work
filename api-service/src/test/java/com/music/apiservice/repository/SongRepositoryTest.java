package com.music.apiservice.repository;

import com.music.apiservice.annotation.R2dbcTestConfig;
import com.music.apiservice.model.entity.Album;
import com.music.apiservice.model.entity.Artist;
import com.music.apiservice.model.entity.Song;
import com.music.apiservice.testUtils.TestTransactionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@R2dbcTestConfig
class SongRepositoryTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private SongRepository songRepository;

    @Test
    void 좋아요_정상_갓수_1증가하고_유저_좋아요_적재되는지_확인하는_테스트() {

        Mono<String> flow = createTestSong()
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

    private Mono<Song> createTestSong() {

        return artistRepository.save(new Artist(null, "TestArtist", "thash"))
                .flatMap(artist ->
                        albumRepository.save(new Album(null,
                                artist.getId(),
                                "TestAlbum",
                                "ahash",
                                LocalDate.of(2025, 1, 1),
                                2025))
                )
                .flatMap(album ->
                        songRepository.save(new Song(null,
                                album.getId(),
                                "TestSong",
                                "shash",
                                0L))
                );
    }
}