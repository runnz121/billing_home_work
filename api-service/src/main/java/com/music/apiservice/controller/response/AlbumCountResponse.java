package com.music.apiservice.controller.response;


import com.music.apiservice.annotation.Description;
import com.music.apiservice.model.dto.PageDato;
import com.music.apiservice.model.dto.YearArtistsCountDto;

import java.util.List;

public record AlbumCountResponse(

        @Description("요청된 연도 (필터링에 사용된 값)")
        Integer year,

        @Description("요청된 가수 ID (필터링에 사용된 값)")
        Long artistId,

        @Description("실제 조회된 앨범 카운트 목록")
        List<YearArtistsCountDto> content,

        @Description("페이지네이션 정보")
        PageDato page
) {

    public static AlbumCountResponse of(Integer year,
                                        Long artistId,
                                        List<YearArtistsCountDto> content,
                                        long totalElements,
                                        int currentPage,
                                        int pageSize) {


        return new AlbumCountResponse(year, artistId, content, PageDato.of(totalElements, currentPage, pageSize));
    }
}
