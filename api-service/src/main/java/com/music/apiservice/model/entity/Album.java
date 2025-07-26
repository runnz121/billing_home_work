package com.music.apiservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

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

    private LocalDate releaseDt;

    private Integer releaseYear;
}
