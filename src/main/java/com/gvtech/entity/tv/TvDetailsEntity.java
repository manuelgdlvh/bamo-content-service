package com.gvtech.entity.tv;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TvDetailsEntity extends PanacheEntityBase {
    private Long id;
    private Long tvId;
    private String language;
    private String title;
    private String overview;
    private String posterPath;
}
