package com.gvtech.entity.movie;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieDetailsEntity extends PanacheEntityBase {
    private Long id;
    private Long movieId;
    private String language;
    private String title;
    private String overview;
    private String posterPath;
}
