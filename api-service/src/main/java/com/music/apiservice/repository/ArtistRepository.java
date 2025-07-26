package com.music.apiservice.repository;

import com.music.apiservice.model.entity.Artist;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ArtistRepository extends ReactiveCrudRepository<Artist, Long> {
}
