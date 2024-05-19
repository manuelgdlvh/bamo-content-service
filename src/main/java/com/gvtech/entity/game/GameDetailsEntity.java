package com.gvtech.entity.game;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameDetailsEntity extends PanacheEntityBase {
    private Long id;
    private Long game_id;
    private String language;
    private String summary;
}
