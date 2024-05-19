package com.gvtech.provider.game;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;


@ApplicationScoped
@Startup
public class HasGamesRandomizedListProvider implements ContentProvider<Boolean> {

    // PATTERN LANGUAGE/PLATFORMS/GENRES/YEARS/GAME_MODES
    @Inject
    GameIdsProvider gameIdsProvider;


    @Override
    public Boolean get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 5) {
            return false;
        }

        final Set<Long> ids = gameIdsProvider.get(contentId);
        return ids != null && !ids.isEmpty();

    }

    @Override
    public ContentType type() {
        return new ContentType("HAS_GAME_RANDOMIZED_LIST");
    }

}
