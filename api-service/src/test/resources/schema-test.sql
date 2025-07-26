DROP TABLE IF EXISTS song;
DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS artists;

CREATE TABLE artists (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(2000) NOT NULL,
    name_hash  CHAR(32)     NOT NULL,
    CONSTRAINT uq_artists_name_hash UNIQUE (name_hash)
);

-- album 테이블
CREATE TABLE album (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    artist_id   BIGINT       NOT NULL,
    title       VARCHAR(2000),
    title_hash  CHAR(32),
    released_at  DATE,
    released_year INT              NULL,
    CONSTRAINT uq_album_artist_title UNIQUE (artist_id, title_hash),
    CONSTRAINT fk_album_artist FOREIGN KEY (artist_id) REFERENCES artists(id)
);

-- song 테이블
CREATE TABLE song (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    album_id   BIGINT       NOT NULL,
    title      VARCHAR(2000),
    title_hash CHAR(32),
    like_count    BIGINT             NOT NULL DEFAULT 0,
    CONSTRAINT uq_song_album_title UNIQUE (album_id, title_hash),
    CONSTRAINT fk_song_album FOREIGN KEY (album_id) REFERENCES album(id)
);

-- user_song_like 테이블
CREATE TABLE user_song_like (
    id         BIGINT     NOT NULL AUTO_INCREMENT,
    user_id    BIGINT     NOT NULL,
    song_id    BIGINT     NOT NULL,
    liked_at   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);