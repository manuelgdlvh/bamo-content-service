package com.gvtech.resource;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Request {
    private String contentType;
    private Map<String, Object> contentFilter;


    public String getPlatformsAsString() {
        final List<BigDecimal> platforms = ((List<BigDecimal>) this.getContentFilter().get("platforms"));
        if (platforms == null || platforms.isEmpty()) {
            return "NONE";
        }

        return platforms.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String getGenresAsString() {
        final List<BigDecimal> genres = ((List<BigDecimal>) this.getContentFilter().get("genres"));
        if (genres == null || genres.isEmpty()) {
            return "NONE";
        }

        return genres.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String getYearsAsString() {
        final List<String> years = ((List<String>) this.getContentFilter().get("years"));
        if (years == null || years.isEmpty()) {
            return "NONE";
        }
        return String.join(",", years);
    }

    public String getGameModesAsString() {
        final List<BigDecimal> gameModes = ((List<BigDecimal>) this.getContentFilter().get("gameModes"));
        if (gameModes == null || gameModes.isEmpty()) {
            return "NONE";
        }
        return gameModes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String getCuisinesAsString() {
        final List<BigDecimal> cuisines = ((List<BigDecimal>) this.getContentFilter().get("cuisines"));
        if (cuisines == null || cuisines.isEmpty()) {
            return "NONE";
        }
        return cuisines.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String getDietsAsString() {
        final List<BigDecimal> diets = ((List<BigDecimal>) this.getContentFilter().get("diets"));
        if (diets == null || diets.isEmpty()) {
            return "NONE";
        }
        return diets.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }

    public String getTypesAsString() {
        final List<BigDecimal> types = ((List<BigDecimal>) this.getContentFilter().get("types"));
        if (types == null || types.isEmpty()) {
            return "NONE";
        }
        return types.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }
}
