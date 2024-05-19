package com.gvtech.provider.game;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.game.Game;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Random;


@ApplicationScoped
@Startup
public class GameRandomizedProvider implements ContentProvider<Game> {

    // PATTERN LANGUAGE/PLATFORMS/GENRES/YEARS/GAME_MODES
    @Inject
    GameIdsProvider gameIdsProvider;
    @Inject
    GameProvider gameProvider;

    final Random random = new Random();


    @Override
    public Game get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 5) {
            return null;
        }

        final String language = parts[0];
        final String platforms = parts[1];
        final String genres = parts[2];
        final String years = parts[3];
        final String gameModes = parts[4];

        final ContentId gameContentIds = new ContentId(String.format("%s/%s/%s/%s/%s", language, platforms, genres, years, gameModes));
        final List<Long> gameIds = this.gameIdsProvider.get(gameContentIds).stream().toList();
        if (gameIds == null) {
            return null;
        }

        final Long id = gameIds.get(random.nextInt(gameIds.size()));

        final ContentId gameContentId = new ContentId(String.format("%s/%s", language, id));
        return this.gameProvider.get(gameContentId);
    }

    @Override
    public ContentType type() {
        return new ContentType("GAME_RANDOMIZED");
    }

}
