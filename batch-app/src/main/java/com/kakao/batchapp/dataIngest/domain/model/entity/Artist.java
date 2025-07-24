package com.kakao.batchapp.dataIngest.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

    private Long id;

    private String name;

    private String nameHash;
}
