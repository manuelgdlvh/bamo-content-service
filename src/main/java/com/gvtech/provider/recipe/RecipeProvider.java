package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.recipe.RecipeEntity;
import com.gvtech.model.recipe.Recipe;
import com.gvtech.repository.recipe.RecipeRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class RecipeProvider extends AbstractCacheableContentProvider<Recipe> {

    // PATTERN LANGUAGE/ID
    @Inject
    RecipeRepository recipeRepository;

    public RecipeProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(Recipe o1, Recipe o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Recipe> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 2) {
            return null;
        }

        final String language = parts[0];
        final Long recipeId = Long.valueOf(parts[1]);

        final RecipeEntity recipeEntity = recipeRepository.findBy(language, recipeId);

        if (recipeEntity == null) {
            return null;
        }

        return new ContentBuild<>(Recipe.map(recipeEntity));
    }


    @Override
    public ContentType type() {
        return new ContentType("RECIPE");
    }
}
