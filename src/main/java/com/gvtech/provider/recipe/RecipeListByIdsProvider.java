package com.gvtech.provider.recipe;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.recipe.Recipe;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class RecipeListByIdsProvider implements ContentProvider<List<Recipe>> {

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


        final List<Recipe> recipes;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<Recipe>> subtasks =
                    ids.stream().map(recipeId -> scope.fork(() -> recipeProvider.get(new ContentId(String.format("%s/%s", language, recipeId))))).toList();
            scope.join();
            recipes = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        return recipes;
    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_LIST_BY_IDS");
    }

}
