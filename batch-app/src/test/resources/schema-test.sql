DROP TABLE IF EXISTS song;
DROP TABLE IF EXISTS album;
DROP TABLE IF EXISTS artists;

-- artists
CREATE TABLE artists (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(2000) NOT NULL,
    name_hash  CHAR(32)      NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_artists_name_hash (name_hash)

) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- album
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


-- song
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