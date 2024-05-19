package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.recipe.Recipe;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@ApplicationScoped
@Startup
public class RecipeRandomizedListProvider extends AbstractCacheableContentProvider<List<Recipe>> {

    // PATTERN LANGUAGE/CUISINES/DIETS/TYPES/SIZE
    @Inject
    RecipeIdsProvider recipeIdsProvider;
    @Inject
    RecipeListByIdsProvider recipeListByIdsProvider;

    public RecipeRandomizedListProvider() {
        super((long) 1000 * 60);
    }

    @Override
    public boolean equals(List<Recipe> o1, List<Recipe> o2) {
        return true;
    }

    @Override
    public ContentBuild<List<Recipe>> build(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 5) {
            return null;
        }

        final String language = parts[0];
        final String cuisines = parts[1];
        final String diets = parts[2];
        final String types = parts[3];
        final int size = Integer.parseInt(parts[4]);
        if (size <= 0) {
            return null;
        }

        final ContentId recipeIdsContentId = new ContentId(String.format("%s/%s/%s/%s", language, cuisines, diets, types));
        final Set<Long> recipeIdsSet = this.recipeIdsProvider.get(recipeIdsContentId);
        if (recipeIdsSet == null) {
            return null;
        }
        List<Long> recipeIds = new ArrayList<>(recipeIdsSet);

        Collections.shuffle(recipeIds);
        if (size < recipeIds.size()) {
            recipeIds = recipeIds.subList(0, size);
        }


        final String recipeIdsAsStr = recipeIds.stream().map(Object::toString).collect(Collectors.joining(","));
        final ContentId recipeByIdsContentId = new ContentId(String.format("%s/%s", language, recipeIdsAsStr));
        return new ContentBuild<>(this.recipeListByIdsProvider.get(recipeByIdsContentId));
    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_RANDOMIZED_LIST");
    }

}
