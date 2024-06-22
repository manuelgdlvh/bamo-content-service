package com.gvtech.model.game;

import com.gvtech.entity.game.*;
import com.gvtech.model.Content;

import java.util.ArrayList;
import java.util.List;

public record Game(Long id, String name, String summary, Double rating, Integer ratingCount, String url,
                   String image, Integer year, List<String> gameModes, List<String> platforms,
                   List<String> genres) implements Content {

    public static Game map(final GameEntity gameEntity) {
        final GameDetailsEntity gameDetails = gameEntity.getDetails().getFirst();
        final List<String> gameModes = gameEntity.getGameModeEntities().stream().map(GameModeEntity::getName).toList();
        final List<String> platforms = gameEntity.getPlatformEntities().stream().map(PlatformEntity::getName).toList();
        final List<String> genres = gameEntity.getGenres().stream().map(GenreEntity::getName).toList();
        final String coverImage = String.format("https://images.igdb.com/igdb/image/upload/t_cover_big/%s.png", gameEntity.getCover());

        return new Game(gameEntity.getGame_id(), gameEntity.getName(), gameDetails.getSummary(), gameEntity.getRate(), gameEntity.getRatingCount(), gameEntity.getUrl(), coverImage,
                gameEntity.getReleaseYear(), gameModes, platforms, genres);
    }

    public static List<Game> mapList(final List<GameEntity> gameEntities) {

        final List<Game> result = new ArrayList<>();
        for (GameEntity gameEntity : gameEntities) {
            result.add(map(gameEntity));
        }

        return result;

    }

    @Override
    public String getContentType() {
        return "GAME";
    }
}

