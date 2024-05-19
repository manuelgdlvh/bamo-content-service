package com.gvtech.entity.recipe;

import com.gvtech.entity.ContentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DietEntity {
    private ContentEntity content;
    private Long id;
    private Long dietId;
    private String language;
    private String name;
    private Boolean enabled;
}
