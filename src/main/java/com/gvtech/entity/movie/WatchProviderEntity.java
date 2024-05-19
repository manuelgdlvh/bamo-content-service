package com.gvtech.entity.movie;

import com.gvtech.entity.ContentEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WatchProviderEntity extends PanacheEntityBase {
    private ContentEntity content;
    private Long id;
    private Long watchProviderId;
    private String name;
    private Boolean enabled;
    private Integer displayOrder;
}
