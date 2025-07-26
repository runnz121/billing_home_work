package com.music.apiservice.repository;

import com.music.apiservice.model.dto.YearArtistsCountDto;
import com.music.apiservice.model.entity.Album;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AlbumRepository extends ReactiveCrudRepository<Album, Long> {

    @Query("""
            SELECT
                al.artist_id         AS artist_id,
                ar.name              AS artist_name,
                COUNT(*)             AS album_counts
            FROM album al
                JOIN artists ar ON al.artist_id = ar.id
            WHERE (:year IS NULL OR al.released_year = :year)
                AND (:artistId IS NULL OR al.artist_id = :artistId)
            GROUP BY al.artist_id, ar.name
            ORDER BY album_counts DESC, ar.name ASC
                LIMIT :limit
            OFFSET :offset
            """)
    Flux<YearArtistsCountDto> findAlbumCountsGroupedByArtistAndYear(Integer year, Long artistId, int limit, int offset);

    @Query("""
            SELECT
                COUNT(DISTINCT al.artist_id)
            FROM album al
                JOIN artists ar ON al.artist_id = ar.id
            WHERE (:year IS NULL OR al.released_year = :year)
                AND (:artistId IS NULL OR al.artist_id = :artistId)
            """)
    Mono<Long> countArtistsByYearAndArtistId(Integer year, Long artistId);
}


