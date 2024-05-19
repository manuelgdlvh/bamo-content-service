package com.gvtech.model;

import com.gvtech.model.game.Game;
import com.gvtech.model.movie.Movie;
import com.gvtech.model.recipe.Recipe;
import com.gvtech.model.tv.Tv;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class ContentWithMetadata {
    private Map<String, Content> content;
    private Metadata metadata;

    public static ContentWithMetadata buildMovie(final Movie movie, final Metadata metadata) {
        return new ContentWithMetadata(Map.of("MOVIE", movie), metadata);
    }

    public static ContentWithMetadata buildGame(final Game game, final Metadata metadata) {
        return new ContentWithMetadata(Map.of("GAME", game), metadata);
    }

    public static ContentWithMetadata buildTv(final Tv tv, final Metadata metadata) {
        return new ContentWithMetadata(Map.of("TV", tv), metadata);
    }

    public static ContentWithMetadata buildRecipe(final Recipe recipe, final Metadata metadata) {
        return new ContentWithMetadata(Map.of("RECIPE", recipe), metadata);
    }
}
