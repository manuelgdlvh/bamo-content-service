package com.gvtech.entity.tv;


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
public class TvEntity extends PanacheEntityBase {

    private Long tvId;
    private Date releaseDate;
    private Short year;
    private Short numberOfEpisodes;
    private Short numberOfSeasons;
    private Float rate;
    private List<CreditsMember> credits;
    private String urlPath;
    private List<TvDetailsEntity> details;
    private List<GenreDetailsEntity> genres;
    private List<TvWatchProviderEntity> watchProviders;


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class CreditsMember {
        private String name;
        private String type;
    }
}
