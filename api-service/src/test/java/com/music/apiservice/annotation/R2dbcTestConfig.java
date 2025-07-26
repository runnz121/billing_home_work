package com.music.apiservice.annotation;

import com.music.apiservice.config.R2dbcConfig;
import com.music.apiservice.testUtils.R2dbcTestDatabaseConfig;
import com.music.apiservice.testUtils.TestTransactionUtils;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ActiveProfiles("test")
@DataR2dbcTest
@Import({
        R2dbcConfig.class,
        TestTransactionUtils.class,
        R2dbcTestDatabaseConfig.class
})
public @interface R2dbcTestConfig {
}
