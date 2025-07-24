package com.kakao.batchapp.dataIngest.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class DateUtils {

    private static DateTimeFormatter YYYY_MM_DD_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);;

    public static LocalDate toLocalDate(String stringDate) {

        try {
            return LocalDate.parse(stringDate, YYYY_MM_DD_FORMAT);
        } catch (Exception ex) {
            return LocalDate.of(2999, 12, 31);
        }
    }
}
