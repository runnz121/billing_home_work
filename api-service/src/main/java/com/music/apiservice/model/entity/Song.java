package com.music.apiservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("song")
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    @Id
    private Long id;

    private Long albumId;

    private String title;

    private String titleHash;

    private Long likeCount;
}
