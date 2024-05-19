package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.game.PlatformEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.game.PlatformRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class PlatformFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    PlatformRepository platformRepository;


    // PATTERN
    public PlatformFilterListProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }


    @Override
    public boolean equals(List<Filter> o1, List<Filter> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<List<Filter>> build(ContentId contentId) {

        final List<PlatformEntity> platformEntities = platformRepository.findAll();
        final List<Filter> filters = Filter.mapGamePlatforms(platformEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("GAME_PLATFORM_FILTER_LIST");
    }

}
