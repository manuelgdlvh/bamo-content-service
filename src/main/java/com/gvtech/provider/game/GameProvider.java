package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.game.GameEntity;
import com.gvtech.model.game.Game;
import com.gvtech.repository.game.GameRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class GameProvider extends AbstractCacheableContentProvider<Game> {

    // PATTERN LANGUAGE/ID
    @Inject
    GameRepository gameRepository;

    public GameProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(Game o1, Game o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Game> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 2) {
            return null;
        }

        final String language = parts[0];
        final Long gameId = Long.valueOf(parts[1]);

        final GameEntity gameEntity = gameRepository.findBy(language, gameId);

        if (gameEntity == null) {
            return null;
        }

        return new ContentBuild<>(Game.map(gameEntity));
    }


    @Override
    public ContentType type() {
        return new ContentType("GAME");
    }
}
