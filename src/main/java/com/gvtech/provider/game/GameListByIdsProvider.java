package com.gvtech.provider.game;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.game.Game;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class GameListByIdsProvider implements ContentProvider<List<Game>> {

    // PATTERN LANGUAGE/IDS
    @Inject
    GameProvider gameProvider;


    @Override
    public List<Game> get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 2) {
            return null;
        }

        final String language = parts[0];
        final List<Long> ids = new ArrayList<>();

        for (String id : parts[1].split(",")) {
            ids.add(Long.valueOf(id));
        }

        final List<Game> games;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<Game>> subtasks =
                    ids.stream().map(gameId -> scope.fork(() -> gameProvider.get(new ContentId(String.format("%s/%s", language, gameId))))).toList();
            scope.join();
            games = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        return games;
    }

    @Override
    public ContentType type() {
        return new ContentType("GAME_LIST_BY_IDS");
    }

}
