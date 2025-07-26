package com.music.apiservice.repository;

import com.music.apiservice.model.entity.Song;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SongRepository extends ReactiveCrudRepository<Song, Long> {

    @Query("""
        UPDATE song
           SET like_count = like_count + 1
         WHERE id = :songId
        """)
    Mono<Long> incrementLikeCount(Long songId);

}