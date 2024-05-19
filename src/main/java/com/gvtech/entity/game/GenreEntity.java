package com.gvtech.entity.game;

import com.gvtech.entity.ContentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenreEntity {
    private ContentEntity content;
    private String language;
    private Long genreId;
    private String name;
    private Boolean enabled;
}
