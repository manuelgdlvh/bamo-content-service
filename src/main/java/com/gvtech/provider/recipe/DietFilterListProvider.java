package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.recipe.DietEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.recipe.DietRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class DietFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    DietRepository dietRepository;


    // PATTERN LANGUAGE
    public DietFilterListProvider() {
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

        final List<DietEntity> dietEntities = dietRepository.findAll(language);
        final List<Filter> filters = Filter.mapRecipeDiets(dietEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("RECIPE_DIET_FILTER_LIST");
    }

}
