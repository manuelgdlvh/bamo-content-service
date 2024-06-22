package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.game.GameModeEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.game.GameModeRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class GameModeFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    GameModeRepository gameModeRepository;


    // PATTERN LANGUAGE
    public GameModeFilterListProvider() {
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

        final List<GameModeEntity> gameModeEntities = gameModeRepository.findAll(language);
        final List<Filter> filters = Filter.mapGameModes(gameModeEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("GAME_MODE_FILTER_LIST");
    }

}
