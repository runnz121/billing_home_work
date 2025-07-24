package com.kakao.batchapp.dataIngest.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {

    @Test
    void 올바른_형식의_문자열을_LocalDate로_변환한다() {
        String input = "2021-04-29";
        LocalDate result = DateUtils.toLocalDate(input);
        assertThat(result).isEqualTo(LocalDate.of(2021, 4, 29));
    }

    @Test
    void 포맷이_잘못된_문자열은_2999_12_31을_반환한다() {
        String input = "20210429";
        LocalDate result = DateUtils.toLocalDate(input);
        assertThat(result).isEqualTo(LocalDate.of(2999, 12, 31));
    }

    @Test
    void null_입력은_예외없이_2999_12_31을_반환한다() {
        String input = null;
        LocalDate result = DateUtils.toLocalDate(input);
        assertThat(result).isEqualTo(LocalDate.of(2999, 12, 31));
    }

    @Test
    void 존재하지_않는_날짜는_2999_12_31을_반환한다() {
        String input = "2021-02-30";
        LocalDate result = DateUtils.toLocalDate(input);
        assertThat(result).isEqualTo(LocalDate.of(2999, 12, 31));
    }
}