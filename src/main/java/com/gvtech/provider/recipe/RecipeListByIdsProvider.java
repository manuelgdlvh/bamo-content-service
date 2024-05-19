package com.gvtech.provider.recipe;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.recipe.Recipe;
import com.gvtech.support.AbstractContentListProvider;
import com.gvtech.support.ContentBucket;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
@Startup
public class RecipeListByIdsProvider extends AbstractContentListProvider implements ContentProvider<List<Recipe>> {

    // PATTERN LANGUAGE/IDS
    @Inject
    RecipeProvider recipeProvider;


    @Override
    public List<Recipe> get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 2) {
            return null;
        }

        final String language = parts[0];
        final List<Long> ids = new ArrayList<>();

        for (String id : parts[1].split(",")) {
            ids.add(Long.valueOf(id));
        }


        final ContentBucket<Recipe> recipeBucket = this.emptyBucket();
        for (Long recipeId : ids) {
            recipeBucket.add(() -> recipeProvider.get(new ContentId(String.format("%s/%s", language, recipeId))));
        }

        return recipeBucket.get();
    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_LIST_BY_IDS");
    }

}
