package com.music.batchapp.dataIngest.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilsTest {

    @Test
    void 일반문자열_hash_생성검증() {

        String input = "hello";
        String result = HashUtils.toHash(input);
        assertThat(result).isEqualTo("5d41402abc4b2a76b9719d911017c592");
    }

    @Test
    void null_또는_빈문자열은_empty_hash_반환() {

        // null 입력 시
        assertThat(HashUtils.toHash(null)).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        // 빈 문자열 입력 시
        assertThat(HashUtils.toHash("")).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
        // 공백만 있는 문자열 입력 시
        assertThat(HashUtils.toHash("   ")).isEqualTo("d41d8cd98f00b204e9800998ecf8427e");
    }

    @Test
    void 악센트문자_및_구분자_제거후_hash_생성검증() {

        String input = "Á é ñ, test";
        String result = HashUtils.toHash(input);
        // "Á é ñ, test" → normalize → "aentest" → MD5
        assertThat(result).isEqualTo("c3a867e8857168427425dbfcbc605c50");
    }

    @Test
    void 구분자와공백제거_hash_일관성검증() {

        String input1 = "a, b   c";
        String input2 = "a b,c";
        // 둘 다 normalize → "abc"
        String hash1 = HashUtils.toHash(input1);
        String hash2 = HashUtils.toHash(input2);
        assertThat(hash1).isEqualTo(hash2)
                .isEqualTo(HashUtils.toHash("abc"));
    }
}