package com.gvtech.provider.recipe;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;


@ApplicationScoped
@Startup
public class HasRecipesRandomizedListProvider implements ContentProvider<Boolean> {

    // PATTERN LANGUAGE/CUISINES/DIETS/TYPES
    @Inject
    RecipeIdsProvider recipeIdsProvider;


    @Override
    public Boolean get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 4) {
            return false;
        }

        final Set<Long> ids = recipeIdsProvider.get(contentId);
        return ids != null && !ids.isEmpty();

    }

    @Override
    public ContentType type() {
        return new ContentType("HAS_RECIPE_RANDOMIZED_LIST");
    }

}
