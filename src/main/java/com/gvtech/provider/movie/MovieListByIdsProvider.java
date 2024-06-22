package com.gvtech.provider.movie;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.movie.Movie;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class MovieListByIdsProvider implements ContentProvider<List<Movie>> {

    // PATTERN LANGUAGE/COUNTRY/IDS
    @Inject
    MovieProvider movieProvider;


    @Override
    public List<Movie> get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final List<Long> ids = new ArrayList<>();

        for (String id : parts[2].split(",")) {
            ids.add(Long.valueOf(id));
        }


        final List<Movie> movies;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<Movie>> subtasks =
                    ids.stream().map(movieId -> scope.fork(() -> movieProvider.get(new ContentId(String.format("%s/%s/%s", language, country, movieId))))).toList();
            scope.join();
            movies = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        return movies;
    }


    @Override
    public ContentType type() {
        return new ContentType("MOVIE_LIST_BY_IDS");
    }

}
