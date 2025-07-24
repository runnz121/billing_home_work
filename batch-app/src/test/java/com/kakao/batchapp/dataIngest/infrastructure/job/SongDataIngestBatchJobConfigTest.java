package com.kakao.batchapp.dataIngest.infrastructure.job;

import com.kakao.batchapp.dataIngest.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@SpringBatchTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ SongDataIngestBatchJobConfig.class })
@Sql(
        scripts = "classpath:schema-test.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class SongDataIngestBatchJobConfigTest extends TestContainerConfig {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDb() {
        // 순서 중요: FK 제약 고려
        jdbcTemplate.execute("DELETE FROM song");
        jdbcTemplate.execute("DELETE FROM album");
        jdbcTemplate.execute("DELETE FROM artists");
    }

    @Test
    void 전체_배치_실행_및_테이블_채워짐_검증() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(params);
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer artistCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM artists", Integer.class);
        Integer albumCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM album", Integer.class);
        Integer songCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM song", Integer.class);

        assertThat(artistCount).isGreaterThan(0);
        assertThat(albumCount).isGreaterThan(0);
        assertThat(songCount).isGreaterThan(0);
    }

    @Test
    void 배치_멱등성_검증() throws Exception {

        JobParameters params1 = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params1);

        Integer artistsBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM artists", Integer.class);
        Integer albumsBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM album", Integer.class);
        Integer songsBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM song", Integer.class);

        Thread.sleep(50);
        JobParameters params2 = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        jobLauncherTestUtils.launchJob(params2);

        Integer artistsAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM artists", Integer.class);
        Integer albumsAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM album", Integer.class);
        Integer songsAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM song", Integer.class);

        // 중복 없이 그대로 유지되어야 함
        assertThat(artistsAfter).isEqualTo(artistsBefore);
        assertThat(albumsAfter).isEqualTo(albumsBefore);
        assertThat(songsAfter).isEqualTo(songsBefore);
    }

    @Test
    void 아티스트_스텝만_실행_검증() {

        JobExecution exec = jobLauncherTestUtils.launchStep("artistStep");
        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM artists", Integer.class);
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void 앨범_스텝만_실행_검증() {

        jobLauncherTestUtils.launchStep("artistStep");

        JobExecution exec = jobLauncherTestUtils.launchStep("albumStep");
        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM album", Integer.class);
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void 노래_스텝만_실행_검증(){

        jobLauncherTestUtils.launchStep("artistStep");
        jobLauncherTestUtils.launchStep("albumStep");

        JobExecution exec = jobLauncherTestUtils.launchStep("songStep");
        assertThat(exec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM song", Integer.class);
        assertThat(count).isGreaterThan(0);
    }
}