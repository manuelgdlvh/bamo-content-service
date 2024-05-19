package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.tv.WatchProviderEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.tv.WatchProviderRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class WatchProviderFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    WatchProviderRepository watchProviderRepository;


    // PATTERN COUNTRY
    public WatchProviderFilterListProvider() {
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

        final String country = parts[0];

        final List<WatchProviderEntity> watchProviderEntities = watchProviderRepository.findAllEnabledSortByDisplayOrderAsc(country);
        final List<Filter> filters = Filter.mapTvWatchProviders(watchProviderEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("TV_WATCH_PROVIDER_FILTER_LIST");
    }

}
