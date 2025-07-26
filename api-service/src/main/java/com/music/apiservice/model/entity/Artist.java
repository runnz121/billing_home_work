package com.music.apiservice.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("artists")
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

    @Id
    private Long id;

    private String name;

    private String nameHash;
}
