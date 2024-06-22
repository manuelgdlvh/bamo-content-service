package com.gvtech.model.recipe;


import com.gvtech.entity.recipe.RecipeDetailsEntity;
import com.gvtech.entity.recipe.RecipeEntity;
import com.gvtech.model.Content;

import java.io.Serializable;
import java.util.List;

public record Recipe(Long id, String title, String summary, String image, Integer servings,
                     Integer readyInMinutes, String url,
                     List<Type> types, List<Diet> diets, List<Cuisine> cuisines) implements Content {

    public static Recipe map(final RecipeEntity recipeEntity) {

        final RecipeDetailsEntity recipeDetailsEntity = recipeEntity.getDetails().getFirst();
        final List<Type> types = recipeEntity.getTypes().stream().map(typeEntity -> new Type(null, typeEntity.getName())).toList();
        final List<Cuisine> cuisines = recipeEntity.getCuisines().stream().map(cuisineEntity -> new Cuisine(null, cuisineEntity.getName())).toList();
        final List<Diet> diets = recipeEntity.getDiets().stream().map(dietEntity -> new Diet(null, dietEntity.getName())).toList();


        return new Recipe(recipeEntity.getRecipeId(), recipeDetailsEntity.getTitle(), recipeDetailsEntity.getSummary(), recipeEntity.getImage(), recipeEntity.getServings(),
                recipeEntity.getReadyInMinutes(), recipeEntity.getUrl(), types, diets, cuisines);
    }

    public static List<Recipe> mapList(final List<RecipeEntity> recipeEntities) {
        return recipeEntities.stream().map(Recipe::map).toList();
    }

    @Override
    public String getContentType() {
        return "RECIPE";
    }

    public record Diet(String code, String name) implements Serializable {

    }

    public record Cuisine(String code, String name) implements Serializable {

    }

    public record Type(String code, String name) implements Serializable {

    }


}
