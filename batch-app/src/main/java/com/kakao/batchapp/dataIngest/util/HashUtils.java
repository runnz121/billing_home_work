package com.kakao.batchapp.dataIngest.util;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Locale;

public class HashUtils {

    private static final String ALGORITHM = "MD5";

    public static String toHash(String target) {

        try {
            String normalized = normalize(target);
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);

            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException("해시 알고리즘을 찾을 수 없습니다: " + ALGORITHM, e);
        }
    }

    private static String normalize(String input) {

        if (StringUtils.hasText(input) == false) {
            return "";
        }

        String trimmed = input.trim();
        String decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFKD);
        String withoutMarks = decomposed.replaceAll("\\p{M}", "");
        String collapsed = withoutMarks.replaceAll("[,\\s]+", "");

        return collapsed.toLowerCase(Locale.ROOT);
    }
}
