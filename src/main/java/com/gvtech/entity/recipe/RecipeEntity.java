package com.gvtech.entity.recipe;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecipeEntity extends PanacheEntityBase {

    private Long recipeId;
    private String image;
    private Integer readyInMinutes;
    private Integer servings;
    private String url;
    private List<RecipeDetailsEntity> details;
    private List<CuisineEntity> cuisines;
    private List<DietEntity> diets;
    private List<TypeEntity> types;


}
