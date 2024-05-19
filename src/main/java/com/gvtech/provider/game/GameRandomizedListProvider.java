package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.game.Game;
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
public class GameRandomizedListProvider extends AbstractCacheableContentProvider<List<Game>> {

    // PATTERN LANGUAGE/PLATFORMS/GENRES/YEARS/GAME_MODES/SIZE
    @Inject
    GameIdsProvider gameIdsProvider;
    @Inject
    GameListByIdsProvider gameListByIdsProvider;

    public GameRandomizedListProvider() {
        super((long) 1000 * 60);
    }

    @Override
    public boolean equals(List<Game> o1, List<Game> o2) {
        return true;
    }

    @Override
    public ContentBuild<List<Game>> build(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 6) {
            return null;
        }

        final String language = parts[0];
        final String platforms = parts[1];
        final String genres = parts[2];
        final String years = parts[3];
        final String gameModes = parts[4];
        final int size = Integer.parseInt(parts[5]);
        if (size <= 0) {
            return null;
        }

        final ContentId gameIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, platforms, genres, years, gameModes));
        final Set<Long> gameIdsSet = this.gameIdsProvider.get(gameIdsContentId);
        if (gameIdsSet == null) {
            return null;
        }
        List<Long> gameIds = new ArrayList<>(gameIdsSet);

        Collections.shuffle(gameIds);
        if (size < gameIds.size()) {
            gameIds = gameIds.subList(0, size);
        }

        final String gameIdsAsStr = gameIds.stream().map(Object::toString).collect(Collectors.joining(","));
        final ContentId gameByIdsContentId = new ContentId(String.format("%s/%s", language, gameIdsAsStr));
        return new ContentBuild<>(gameListByIdsProvider.get(gameByIdsContentId));
    }

    @Override
    public ContentType type() {
        return new ContentType("GAME_RANDOMIZED_LIST");
    }

}
