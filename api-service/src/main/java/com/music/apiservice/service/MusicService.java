package com.music.apiservice.service;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.exception.CustomException;
import com.music.apiservice.exception.error.LikeError;
import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.model.entity.UserSongLike;
import com.music.apiservice.repository.AlbumRepository;
import com.music.apiservice.repository.SongRepository;
import com.music.apiservice.repository.UserSongLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final UserSongLikeRepository userSongLikeRepository;

    private final TransactionalOperator transactionalOperator;

    public Mono<AlbumCountResponse> getAlbumCounts(Integer year,
                                                   Long artistId,
                                                   int limit,
                                                   int offset) {

        Mono<List<YearArtistsCountDto>> contentMono = albumRepository
                .findAlbumCountsGroupedByArtistAndYear(year, artistId, limit, offset)
                .collectList();

        Mono<Long> totalElementsMono = albumRepository.countArtistsByYearAndArtistId(year, artistId);
        return Mono.zip(contentMono, totalElementsMono)
                .map(tuple -> {
                    List<YearArtistsCountDto> content = tuple.getT1();
                    Long totalElements = tuple.getT2();
                    return AlbumCountResponse.of(year, artistId, content, totalElements, offset / limit, limit);
                });
    }

    public Mono<Void> likeSong(Long userId, Long songId) {

        return transactionalOperator.execute(status ->
                        userSongLikeRepository
                                .save(UserSongLike.toEntity(userId, songId))
                                .flatMap(saved -> songRepository.incrementLikeCount(songId))
                )
                .onErrorMap(DuplicateKeyException.class,
                        ex -> new CustomException(LikeError.ALREADY_LIKE_ERROR))
                .then();
    }
}
