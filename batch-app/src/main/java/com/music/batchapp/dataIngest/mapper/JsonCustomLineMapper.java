package com.music.batchapp.dataIngest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.music.batchapp.dataIngest.domain.MusicData;
import org.springframework.batch.item.file.LineMapper;

public class JsonCustomLineMapper implements LineMapper<MusicData> {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public MusicData mapLine(String line,
                             int lineNumber) throws Exception {

        return objectMapper.readValue(line, MusicData.class);
    }
}