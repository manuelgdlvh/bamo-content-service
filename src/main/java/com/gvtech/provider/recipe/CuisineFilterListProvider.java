package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.recipe.CuisineEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.recipe.CuisineRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class CuisineFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    CuisineRepository cuisineRepository;


    // PATTERN LANGUAGE
    public CuisineFilterListProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }


    @Override
    public boolean equals(List<Filter> o1, List<Filter> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<List<Filter>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 1) {
            return null;
        }

        final String language = parts[0];

        final List<CuisineEntity> cuisineEntities = cuisineRepository.findAll(language);
        final List<Filter> filters = Filter.mapRecipeCuisines(cuisineEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_CUISINE_FILTER_LIST");
    }

}
