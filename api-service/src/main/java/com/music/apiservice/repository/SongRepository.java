package com.music.apiservice.repository;

import com.music.apiservice.model.dto.SongLikeCountDto;
import com.music.apiservice.model.entity.Song;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SongRepository extends ReactiveCrudRepository<Song, Long> {

    @Modifying
    @Query("""
        UPDATE song
           SET like_count = like_count + 1
         WHERE id = :songId
        """)
    Mono<Long> incrementLikeCount(Long songId);

    @Query("""
            SELECT song_id    AS song_id
                 , COUNT(*)   AS count
              FROM user_song_like
             WHERE liked_at >= TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP())
             GROUP BY song_id
             ORDER BY count DESC
             LIMIT 10
            """)
    Flux<SongLikeCountDto> findTop10InLastHour();
}