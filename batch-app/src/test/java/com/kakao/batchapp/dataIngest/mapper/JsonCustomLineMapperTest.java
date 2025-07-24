package com.kakao.batchapp.dataIngest.mapper;

import com.kakao.batchapp.dataIngest.domain.MusicData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonCustomLineMapperTest {

    private final JsonCustomLineMapper mapper = new JsonCustomLineMapper();

    @Test
    void 이스케이프된_슬래시는_복원된다() throws Exception {

        String line = "{\"artist\":\"A\\\\/B\",\"album\":\"X\\\\/Y\",\"song\":\"Hello\\\\/World\"}";
        MusicData data = mapper.mapLine(line, 1);

        assertThat(data.getArtist()).isEqualTo("A\\\\/B");       // "A\\/B"
        assertThat(data.getAlbum()).isEqualTo("X\\\\/Y");        // "X\\/Y"
        assertThat(data.getSong()).isEqualTo("Hello\\\\/World"); // "Hello\\/World"
    }

    @Test
    void 일반_슬래시는_그대로_유지된다() throws Exception {

        String line = "{\"artist\":\"A/B\",\"album\":\"X/Y\",\"song\":\"Hello/World\"}";
        MusicData data = mapper.mapLine(line, 2);

        assertThat(data.getArtist()).isEqualTo("A/B");
        assertThat(data.getAlbum()).isEqualTo("X/Y");
        assertThat(data.getSong()).isEqualTo("Hello/World");
    }
}