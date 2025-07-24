package com.kakao.batchapp.dataIngest.infrastructure.job;

import com.kakao.batchapp.dataIngest.domain.MusicData;
import com.kakao.batchapp.dataIngest.domain.entity.Album;
import com.kakao.batchapp.dataIngest.domain.entity.Artist;
import com.kakao.batchapp.dataIngest.domain.entity.Song;
import com.kakao.batchapp.dataIngest.mapper.JsonCustomLineMapper;
import com.kakao.batchapp.dataIngest.util.DateUtils;
import com.kakao.batchapp.dataIngest.util.HashUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SongDataIngestBatchJobConfig {

    private static final int CHUNK_SIZE = 1000;
    private static final String MUSIC_DATA_INGEST_JOB = "musicDataIngestJob";
    private static final String ND_JSON_READER = "ndJsonMusicDataReader";
    private static final String ARTISTS_STEP = "artistStep";
    private static final String ALBUM_STEP = "albumStep";
    private static final String SONG_STEP = "songStep";
    private static final String RESOURCE_NAME = "spotifyDataset.json";

    @Bean
    public Job musicDataIngestJob(JobRepository jobRepository,
                                  Step artistStep,
                                  Step albumStep,
                                  Step songStep) {

        return new JobBuilder(MUSIC_DATA_INGEST_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(artistStep)
                .next(albumStep)
                .next(songStep)
                .build();
    }

    /**
     * NDJson 파일을 읽는 공통 Reader
     */
    @Bean
    @StepScope
    public FlatFileItemReader<MusicData> ndJsonMusicDataReader() {

        return new FlatFileItemReaderBuilder<MusicData>()
                .name(ND_JSON_READER)
                .resource(new ClassPathResource(RESOURCE_NAME))
                .lineMapper(new JsonCustomLineMapper())
                .build();
    }

    /**
     * 아티스트를 저장 하는 스탭
     */
    @Bean
    public Step artistStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           FlatFileItemReader<MusicData> ndJsonMusicDataReader,
                           ItemProcessor<MusicData, Artist> artistProcessor,
                           ItemWriter<Artist> artistWriter) {

        return new StepBuilder(ARTISTS_STEP, jobRepository)
                .<MusicData, Artist>chunk(CHUNK_SIZE, transactionManager)
                .reader(ndJsonMusicDataReader)
                .processor(artistProcessor)
                .writer(artistWriter)
                .build();
    }

    @Bean
    public ItemProcessor<MusicData, Artist> artistProcessor() {

        return dto -> new Artist(null, dto.getArtist(), HashUtils.toHash(dto.getArtist()));
    }

    @Bean
    public JdbcBatchItemWriter<Artist> artistWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Artist>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                             INSERT INTO artists (name, name_hash)
                             VALUES (:name, :nameHash)
                             ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)
                        """)
                .assertUpdates(false)
                .build();
    }


    /**
     * 앨범을 저장 하는 스탭
     */
    @Bean
    public Step albumStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          FlatFileItemReader<MusicData> ndJsonMusicDataReader,
                          ItemProcessor<MusicData, Album> albumProcessor,
                          ItemWriter<Album> albumWriter) {

        return new StepBuilder(ALBUM_STEP, jobRepository)
                .<MusicData, Album>chunk(CHUNK_SIZE, transactionManager)
                .reader(ndJsonMusicDataReader)
                .processor(albumProcessor)
                .writer(albumWriter)
                .build();
    }

    @Bean
    public ItemProcessor<MusicData, Album> albumProcessor() {

        return dto -> {
            String artistHash = HashUtils.toHash(dto.getArtist());
            String titleHash = HashUtils.toHash(dto.getAlbum());

            return Album.builder()
                    .artistHash(artistHash)
                    .title(dto.getAlbum())
                    .titleHash(titleHash)
                    .releaseDate(DateUtils.toLocalDate(dto.getReleaseDate()))
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<Album> albumWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Album>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                            INSERT INTO album (
                              artist_id,
                              title,
                              title_hash,
                              release_dt
                            )
                            VALUES (
                              (SELECT id FROM artists WHERE name_hash = :artistHash),
                              :title,
                              :titleHash,
                              :releaseDate
                            )
                            ON DUPLICATE KEY UPDATE
                              id = id
                        """)
                .assertUpdates(false)
                .build();
    }

    /**
     * 노래 저장 하는 스탭
     */
    @Bean
    public Step songStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager,
                         FlatFileItemReader<MusicData> ndJsonMusicDataReader,
                         ItemProcessor<MusicData, Song> songProcessor,
                         ItemWriter<Song> songWriter) {

        return new StepBuilder(SONG_STEP, jobRepository)
                .<MusicData, Song>chunk(CHUNK_SIZE, transactionManager)
                .reader(ndJsonMusicDataReader)
                .processor(songProcessor)
                .writer(songWriter)
                .build();
    }

    @Bean
    public ItemProcessor<MusicData, Song> songProcessor() {

        return dto -> {
            String songTitle = StringUtils.hasText(dto.getSong()) == false ? "" : dto.getSong();

            return Song.builder()
                    .artistHash(HashUtils.toHash(dto.getArtist()))
                    .albumHash(HashUtils.toHash(dto.getAlbum()))
                    .title(songTitle)
                    .titleHash(HashUtils.toHash(songTitle))
                    .build();
        };
    }

    @Bean
    public JdbcBatchItemWriter<Song> songWriter(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Song>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                        INSERT INTO song (
                          album_id,
                          title,
                          title_hash
                        )
                        VALUES (
                        (
                          SELECT al.id
                            FROM album al
                            JOIN artists ar ON al.artist_id = ar.id
                           WHERE ar.name_hash  = :artistHash
                             AND al.title_hash = :albumHash
                          ),
                          :title,
                          :titleHash
                        )
                        ON DUPLICATE KEY UPDATE
                          id = id
                        """)
                .assertUpdates(false)
                .build();
    }
}
