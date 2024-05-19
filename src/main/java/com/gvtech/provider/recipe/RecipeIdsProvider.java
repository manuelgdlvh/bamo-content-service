package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.repository.recipe.RecipeRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@ApplicationScoped
@Startup
public class RecipeIdsProvider extends AbstractCacheableContentProvider<Set<Long>> {

    // PATTERN LANGUAGE/CUISINES/DIETS/TYPES
    @Inject
    RecipeRepository recipeRepository;

    public RecipeIdsProvider() {
        super((long) (1000 * 90));
    }

    @Override
    public boolean equals(Set<Long> o1, Set<Long> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Set<Long>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 4) {
            return null;
        }

        final String language = parts[0];

        final List<Long> cuisines = new ArrayList<>();
        if (!Objects.equals(parts[1], "NONE")) {
            for (String platform : parts[1].split(",")) {
                cuisines.add(Long.valueOf(platform));
            }
        }

        final List<Long> diets = new ArrayList<>();
        if (!Objects.equals(parts[2], "NONE")) {
            for (String genre : parts[2].split(",")) {
                diets.add(Long.valueOf(genre));
            }
        }

        final List<Long> types = new ArrayList<>();
        if (!Objects.equals(parts[3], "NONE")) {
            for (String type : parts[3].split(",")) {
                types.add(Long.valueOf(type));
            }
        }


        final Set<Long> ids = recipeRepository.findBy(language, cuisines, diets, types);
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(ids);
    }


    @Override
    public ContentType type() {
        return new ContentType("RECIPE_IDS");
    }
}
