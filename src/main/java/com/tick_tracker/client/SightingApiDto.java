package com.tick_tracker.client;

public record SightingApiDto(
        String id,
        String date, //String is used instead of LocalDateTime so all records arrives safely, otherwise if LocalDateTime is used instead Jackson would fail and the whole download will fail with it
        String location,
        String species,
        String latinName
) {
}

