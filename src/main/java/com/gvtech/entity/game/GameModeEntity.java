package com.gvtech.entity.game;

import com.gvtech.entity.ContentEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameModeEntity extends PanacheEntityBase {
    private ContentEntity content;
    private String language;
    private Long gameModeId;
    private String name;
    private Boolean enabled;
}
