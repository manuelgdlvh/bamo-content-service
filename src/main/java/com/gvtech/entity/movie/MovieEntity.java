package com.gvtech.entity.movie;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieEntity extends PanacheEntityBase {

    private Long movieId;
    private Date releaseDate;
    private Short year;
    private Short runtime;
    private Float rate;
    private List<CreditsMember> credits;
    private String urlPath;
    private List<MovieDetailsEntity> details;
    private List<GenreDetailsEntity> genres;
    private List<MovieWatchProviderEntity> watchProviders;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class CreditsMember {
        private String name;
        private String type;
    }
}
