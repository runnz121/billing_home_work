package com.kakao.batchapp.dataIngest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.batchapp.dataIngest.domain.MusicData;
import org.springframework.batch.item.file.LineMapper;

public class JsonCustomLineMapper implements LineMapper<MusicData> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SLASH_PLACEHOLDER = "__SLASH__";

    @Override
    public MusicData mapLine(String line, int lineNumber) throws Exception {

        String prepped = line.replace("\\/", SLASH_PLACEHOLDER);

        // jackson 파싱
        MusicData data = objectMapper.readValue(prepped, MusicData.class);

        // POJO 필드에 담긴 토큰만 다시 \/ 로 복원
        if (data.getSong() != null) {
            data.setSong(data.getSong().replace(SLASH_PLACEHOLDER, "\\/"));
        }

        if (data.getAlbum() != null) {
            data.setAlbum(data.getAlbum().replace(SLASH_PLACEHOLDER, "\\/"));
        }

        if (data.getArtist() != null) {
            data.setArtist(data.getArtist().replace(SLASH_PLACEHOLDER, "\\/"));
        }

        return data;
    }
}