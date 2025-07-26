package com.music.apiservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

@Builder
@Getter
@Table("album")
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    @Id
    private Long id;

    private Long artistId;

    private String title;

    private String titleHash;

    private LocalDate releasedAt;

    private Integer releasedYear;

    public static Album of(Long artistId,
                           String title,
                           String titleHash,
                           LocalDate releasedAt,
                           Integer releasedYear) {

        return Album.builder()
                .artistId(artistId)
                .title(title)
                .titleHash(titleHash)
                .releasedAt(releasedAt)
                .releasedYear(releasedYear)
                .build();
    }
}
