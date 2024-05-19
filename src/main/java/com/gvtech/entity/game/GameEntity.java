package com.gvtech.entity.game;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameEntity extends PanacheEntityBase {

    private Long game_id;
    private String cover;
    private String name;
    private Double rate;
    private Integer ratingCount;
    private String url;
    private Integer releaseYear;
    private List<GameDetailsEntity> details;
    private List<GenreEntity> genres;
    private List<PlatformEntity> platformEntities;
    private List<GameModeEntity> gameModeEntities;


}
