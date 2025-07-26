package com.music.apiservice.model.dto;

import com.music.apiservice.annotation.Description;

public record YearArtistsCountDto(

        @Description("아티스트 식별자")
        Long artistId,

        @Description("아티스트 이름")
        String artistName,

        @Description("아티스트별 앨범 갯수")
        Long albumCounts
) {
}
