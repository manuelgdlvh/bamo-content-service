package com.gvtech.entity.tv;

import com.gvtech.entity.ContentEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenreDetailsEntity extends PanacheEntityBase {
    private ContentEntity content;
    private Long id;
    private Long genreId;
    private String language;
    private String name;
}
