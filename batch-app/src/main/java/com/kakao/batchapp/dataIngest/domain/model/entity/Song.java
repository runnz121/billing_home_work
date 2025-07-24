package com.kakao.batchapp.dataIngest.domain.model.entity;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    private String artistHash;

    private String albumHash;

    private String title;

    private String titleHash;
}
