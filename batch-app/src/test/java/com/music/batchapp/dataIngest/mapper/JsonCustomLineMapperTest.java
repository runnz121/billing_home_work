package com.music.batchapp.dataIngest.mapper;

import com.music.batchapp.dataIngest.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JsonCustomLineMapperTest {

    private final JsonCustomLineMapper mapper = new JsonCustomLineMapper();

    @Test
    void 객체가_정상적으로_파싱되는지_확인하는_테스트() {

        String json = TestUtils.asString("data/music-data.json");

        assertDoesNotThrow(() -> {
            mapper.mapLine(json, 1);
        });
    }
}