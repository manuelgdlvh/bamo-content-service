package com.gvtech.entity.recipe;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeDetailsEntity extends PanacheEntityBase {
    private Long id;
    private Long recipe_id;
    private String language;
    private String summary;
    private String title;
}
