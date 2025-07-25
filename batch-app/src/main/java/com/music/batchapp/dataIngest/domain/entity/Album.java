package com.music.batchapp.dataIngest.domain.entity;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Album {

    private Long albumId;

    private String artistHash;

    private String title;

    private String titleHash;

    private LocalDate releaseDate;
}
