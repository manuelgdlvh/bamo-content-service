package com.gvtech.provider.recipe;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.recipe.Recipe;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Random;


@ApplicationScoped
@Startup
public class RecipeRandomizedProvider implements ContentProvider<Recipe> {

    final Random random = new Random();
    // PATTERN LANGUAGE/CUISINES/DIETS/TYPES
    @Inject
    RecipeIdsProvider recipeIdsProvider;
    @Inject
    RecipeProvider recipeProvider;

    @Override
    public Recipe get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 4) {
            return null;
        }

        final String language = parts[0];
        final String cuisines = parts[1];
        final String diets = parts[2];
        final String types = parts[3];

        final ContentId recipeContentIds = new ContentId(String.format("%s/%s/%s/%s", language, cuisines, diets, types));
        final List<Long> recipeIds = this.recipeIdsProvider.get(recipeContentIds).stream().toList();
        // MAYBE THROW EXCEPTION TO AVOID NULL CHECK IN EVERY POINT???
        if (recipeIds == null) {
            return null;
        }

        final long id = recipeIds.get(random.nextInt(recipeIds.size()));

        final ContentId recipeContentId = new ContentId(String.format("%s/%s", language, id));
        return this.recipeProvider.get(recipeContentId);
    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_RANDOMIZED");
    }

}
