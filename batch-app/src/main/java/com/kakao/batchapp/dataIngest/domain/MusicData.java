package com.kakao.batchapp.dataIngest.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MusicData {

        @JsonProperty("Artist(s)")
        private String artist;

        private String song;

        private String text;

        @JsonProperty("Length")
        private String length;

        private String emotion;

        @JsonProperty("Genre")
        private String genre;

        @JsonProperty("Album")
        private String album;

        @JsonProperty("Release Date")
        private String releaseDate;

        @JsonProperty("Key")
        private String key;

        @JsonProperty("Tempo")
        private double tempo;

        @JsonProperty("Loudness (db)")
        private double loudness;

        @JsonProperty("Time signature")
        private String timeSignature;

        @JsonProperty("Explicit")
        private String explicit;

        @JsonProperty("Popularity")
        private int popularity;

        @JsonProperty("Energy")
        private int energy;

        @JsonProperty("Danceability")
        private int danceability;

        @JsonProperty("Positiveness")
        private int positiveness;

        @JsonProperty("Speechiness")
        private int speechiness;

        @JsonProperty("Liveness")
        private int liveness;

        @JsonProperty("Acousticness")
        private int acousticness;

        @JsonProperty("Instrumentalness")
        private int instrumentalness;

        @JsonProperty("Good for Party")
        private int goodForParty;

        @JsonProperty("Good for Work/Study")
        private int goodForWorkStudy;

        @JsonProperty("Good for Relaxation/Meditation")
        private int goodForRelaxation;

        @JsonProperty("Good for Exercise")
        private int goodForExercise;

        @JsonProperty("Good for Running")
        private int goodForRunning;

        @JsonProperty("Good for Yoga/Stretching")
        private int goodForYoga;

        @JsonProperty("Good for Driving")
        private int goodForDriving;

        @JsonProperty("Good for Social Gatherings")
        private int goodForSocial;

        @JsonProperty("Good for Morning Routine")
        private int goodForMorning;

        @JsonProperty("Similar Songs")
        private List<Map<String, Object>> similarSongs;

}