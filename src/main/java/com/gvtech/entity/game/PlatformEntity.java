package com.gvtech.entity.game;

import com.gvtech.entity.ContentEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlatformEntity {
    private ContentEntity content;
    private Long platformId;
    private String name;
    private Boolean enabled;
}
