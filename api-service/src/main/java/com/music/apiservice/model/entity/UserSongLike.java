package com.music.apiservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Builder
@Getter
@Table("user_song_like")
@NoArgsConstructor
@AllArgsConstructor
public class UserSongLike {

    @Id
    private Long id;

    private Long userId;

    private Long songId;

    @CreatedDate
    @Column("liked_at")
    private LocalDateTime likedAt;

    public static UserSongLike toEntity(Long userId,
                                        Long songId) {

        return UserSongLike.builder()
                .userId(userId)
                .songId(songId)
                .build();
    }
}
