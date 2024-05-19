package com.gvtech.provider.movie;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.movie.Movie;
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
public class MovieRandomizedListProvider extends AbstractCacheableContentProvider<List<Movie>> {

    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/SIZE
    @Inject
    MovieIdsProvider movieIdsProvider;
    @Inject
    MovieListByIdsProvider movieListByIdsProvider;

    public MovieRandomizedListProvider() {
        super((long) 1000 * 60);
    }

    @Override
    public boolean equals(List<Movie> o1, List<Movie> o2) {
        return true;
    }

    @Override
    public ContentBuild<List<Movie>> build(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 6) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final String platforms = parts[2];
        final String genres = parts[3];
        final String years = parts[4];
        final int size = Integer.parseInt(parts[5]);
        if (size <= 0) {
            return null;
        }


        String exclusiveCountry = "TRUE";
        if (platforms.equals("NONE")) {
            exclusiveCountry = "FALSE";
        }

        final ContentId movieIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, exclusiveCountry, platforms, genres, years));
        final Set<Long> movieIdsSet = this.movieIdsProvider.get(movieIdsContentId);
        if (movieIdsSet == null) {
            return null;
        }
        List<Long> movieIds = new ArrayList<>(movieIdsSet);

        Collections.shuffle(movieIds);
        if (size < movieIds.size()) {
            movieIds = movieIds.subList(0, size);
        }

        final String movieIdsAsStr = movieIds.stream().map(Object::toString).collect(Collectors.joining(","));
        final ContentId movieByIdsContentId = new ContentId(String.format("%s/%s/%s", language, country, movieIdsAsStr));
        return new ContentBuild<>(movieListByIdsProvider.get(movieByIdsContentId));
    }

    @Override
    public ContentType type() {
        return new ContentType("MOVIE_RANDOMIZED_LIST");
    }

}
