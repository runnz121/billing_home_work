package com.music.apiservice.repository;

import com.music.apiservice.model.entity.UserSongLike;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserSongLikeRepository extends ReactiveCrudRepository<UserSongLike, Long> {
}
