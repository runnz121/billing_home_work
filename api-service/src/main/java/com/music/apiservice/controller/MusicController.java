package com.music.apiservice.controller;

import com.music.apiservice.annotation.Description;
import com.music.apiservice.controller.response.AlbumCountResponse;
import com.music.apiservice.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MusicController {

    private final MusicService musicService;

    @Description("연도 & 가수별 앨범 수 조회 API")
    @GetMapping("/albums/count")
    public Mono<AlbumCountResponse> countByArtist(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long artistId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        return musicService.getAlbumCounts(year, artistId, limit, offset);
    }

    @Description("노래별 좋아요 증가 API")
    @PostMapping("/users/{userId}/songs/{songId}/like")
    public Mono<ResponseEntity<Void>> likeSong(
            @PathVariable Long songId,
            @PathVariable Long userId) {

        return musicService.likeSong(userId, songId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
