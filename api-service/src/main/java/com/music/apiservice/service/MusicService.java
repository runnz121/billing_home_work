package com.music.apiservice.service;

import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.repository.AlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final AlbumRepository albumRepository;

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
}
