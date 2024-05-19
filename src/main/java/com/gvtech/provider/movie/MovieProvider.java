package com.gvtech.provider.movie;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.movie.MovieEntity;
import com.gvtech.model.movie.Movie;
import com.gvtech.repository.movie.MovieRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class MovieProvider extends AbstractCacheableContentProvider<Movie> {

    // PATTERN LANGUAGE/COUNTRY/ID
    @Inject
    MovieRepository movieRepository;

    public MovieProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(Movie o1, Movie o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Movie> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final Long movieId = Long.valueOf(parts[2]);

        final MovieEntity movieEntity = movieRepository.findBy(language, movieId);

        if (movieEntity == null) {
            return null;
        }

        return new ContentBuild<>(Movie.map(movieEntity, country));
    }


    @Override
    public ContentType type() {
        return new ContentType("MOVIE");
    }
}
