# 과제 구현 설명서
### 작성자 : 박종빈
### 깃허브 리포지토리 : https://github.com/runnz121/billing_home_work

---
## 실행방법
- Active profiles : docker 로 설정합니다 (batch-app, api-service 모두 적용)
- 프로젝트 root path 에서(docker-compose.yml 파일 위치) docker-compose up -d 로 DB 컨테이너 환경을 로드 합니다.
- 배치 실행시 /resources/musicDateset.json 이름으로 파일이 존재해야합니다
---

## 추가 적용사항
- **docker-compose** API‑service & batch‑app 모두 동일한 MySQL 컨테이너를 사용하도록 구성하였습니다.
- **Testcontainer** 적용하여 테스트 수행시 컨테이너 환경에서 수행되도록 설정하였습니다. (batch-app 스프링 배치 job 테스트)
- **Swagger** 추가하였습니다. (http://localhost:8080/music/webjars/swagger-ui/index.html) (api-service)

---

## 추가 설명
- batch-app > git ignore 에 다음과 같이 설정하여 /src/main/resources/**/*.json 데이터 셋은 업로드 하지 않았습니다.
- 테스트 컨테이너 사용하여, 도커 없이 실행시 테스트 실패하는 케이스가 발생할 수 있습니다.
- docker-compose 로 실행된 mysql db에서 배치 실행시 약 30분정도 시간 소모됩니다 (1.3gb 데이터 셋 기준) 

---

## 모듈 설명
- 모듈 분리 이유
  - 대용량 데이터 처리와 api 호출 서비스 분리가 필요하였습니다.
  - 하나의 단일 모듈로 처리하기 보다는 batch 와 api 서비스를 분리하는게 도메인적으로 유지보수성 측면에서 유리하다고 판단했습니다.

## 1. batch-app
- 스프링 배치로 구성한 이유
  - 적은 메모리, 대용량 데이터 처리가 필요한 케이스입니다.
  - 추후 확정성과 OOM 방지를 위해 배치 청크단위로 적재 하도록 구성하였습니다.
  - NDJson 파일을 스트리밍 방식으로 읽고 반영하기 위해 FlatFileItemReader 를 적용하였습니다.
  - 중복으로 들어오는 이름이 있어, 해당 이름을 해시값으로 바꾸고 중복처리되어 저장하지 않도록 설정하였습니다. 

## 2. api-service
- webflux, r2dbc 적용 이유
  - 제한된 리소스 환경에서 높은 동시성과 응답 성능 확보를 위해, Spring WebFlux와 R2DBC 기반의 논블로킹 리액티브 아키텍처를 적용하였습니다. 
  - 특히 좋아요 증가, 목록 조회 등 단순 I/O 중심 API에서 퍼포먼스를 극대화할 수 있다고 판단하여 적용하였습니다. 

### 1. 연도 & 가수별 발매 앨범 수 조회 API
- 페이지네이션 적용되도록 파라미터 받아서 사이즈 만큼 반환되도록 구현하였습니다.
- 연도, 아티스트 아이디를 각각 받아 필터링하여 조회할 수 있도록 구현하였습니다. (없으면 전체 조회)

### 2. 노래별 좋아요 API
- transactionalOperator 적용하여, 유저-좋아요 테이블에 적재 후 좋아요 상태 증가하도록 설정하였습니다.
- 같은 유저가 같은 노래에 이미 좋아요를 한 경우 에러 발생하도록 처리하였습니다. 

### 3. 최근 1시간 동안 좋아요 증가 TOP 10 API
- 유저-좋아요 테이블에서 최근 한시간 기준으로 좋아요가 높은 순으로 반환하도록 설정하였습니다. 

---
## 테이블 설계

```sql
-- 아티스트 저장 테이블
CREATE TABLE artists (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(2000) NOT NULL,
    name_hash  CHAR(32)      NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_artists_name_hash (name_hash)

) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

```
- artists 테이블 설멍
  - name_hash 컬럼을 만들어서 해당 컬럼의 데이터를 기준으로 중복으로 들어오는 아티스트를 구분하였습니다. 
  - unique_key 제약조건으로 해당 컬럼의 유일값을 보장합니다. 


---

```sql
-- 앨범 저장 테이블
CREATE TABLE album (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    artist_id     BIGINT       NOT NULL,
    title         VARCHAR(2000)    NULL,
    title_hash    CHAR(32)         NULL,
    released_at   DATE             NULL,
    released_year INT              NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_album_artist_title (artist_id, title_hash),

    INDEX idx_album_artist (artist_id),
    INDEX idx_album_release_year (released_year),
    INDEX idx_album_year_artist (released_year, artist_id),

    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artists(id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

```
- album 테이블 설명
  - title_hash 컬럼을 만들어서 해당 컬럼의 데이터 기준으로 중복으로 들어오는 앨범명을 구분하였습니다.
  - 연도 & 가수별 발매 앨범 수 조회시 필터링 조회조건에 따른 컬럼을 인덱스 추가하였습니다. 
  - 연도별 집계쿼리에서 사용되는 집계쿼리에 인덱스 적용되도록 복합인덱스 설정하였습니다. (released_year, artist_id)


---

```sql
-- 노래 저장 테이블
CREATE TABLE song (
    id            BIGINT         NOT NULL AUTO_INCREMENT,
    album_id      BIGINT         NOT NULL,
    title         VARCHAR(2000)      NULL,
    title_hash    CHAR(32)           NULL,
    like_count    BIGINT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uq_song_album_title (album_id, title_hash),

    INDEX idx_song_album (album_id),

    CONSTRAINT fk_song_album
       FOREIGN KEY (album_id) REFERENCES album(id)
) DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

```
- song 테이블 설명
  - title_hash 컬럼을 만들어서 해당 컬럼의 데이터 기준으로 중복으로 들어오는 노래명을 구분하였습니다.


```sql
-- 유저-노래 저장 테이블 
CREATE TABLE user_song_like (
    id         BIGINT     NOT NULL AUTO_INCREMENT,
    user_id    BIGINT     NOT NULL,
    song_id    BIGINT     NOT NULL,
    liked_at   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_user_song (user_id, song_id),

    INDEX idx_usl_user (user_id),
    INDEX idx_usl_liked_at_song (liked_at, song_id),

    CONSTRAINT fk_usl_song FOREIGN KEY (song_id) REFERENCES song(id)
) DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

```
- user_song_like 테이블 설명
  - user_id 단독 검색을 위한 인덱스 추가하였습니다
  - liked_at 기준 시간범위 필터링과 그룹핑을 위한 복합 인덱스 추가하였습니다. (liked_at, song_id)
