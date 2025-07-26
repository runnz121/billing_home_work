package com.music.apiservice.model.dto;

import com.music.apiservice.annotation.Description;

public record PageDato(

        @Description("전체 항목 수")
        long totalElements,

        @Description("전체 페이지 수")
        int totalPages,

        @Description("현재 페이지 번호")
        int currentPage,

        @Description("페이지 당 항목 수")
        int pageSize,

        @Description("현재 페이지가 마지막 페이지인지 여부")
        boolean isLast

) {

    public static PageDato of(long totalElements,
                              int currentPage,
                              int pageSize) {

        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLast = currentPage >= (totalPages - 1);

        return new PageDato(totalElements, totalPages, currentPage, pageSize, isLast);
    }
}
