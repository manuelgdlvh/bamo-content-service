package com.gvtech.provider.movie;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.movie.GenreDetailsEntity;
import com.gvtech.model.Filter;
import com.gvtech.repository.movie.GenreDetailsRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;


@ApplicationScoped
@Startup
public class GenreFilterListProvider extends AbstractCacheableContentProvider<List<Filter>> {


    @Inject
    GenreDetailsRepository genreDetailsRepository;


    // PATTERN LANGUAGE
    public GenreFilterListProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }


    @Override
    public boolean equals(List<Filter> o1, List<Filter> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<List<Filter>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 1) {
            return null;
        }

        final String language = parts[0];

        final List<GenreDetailsEntity> genreDetailsEntities = genreDetailsRepository.findAll(language);
        final List<Filter> filters = Filter.mapMovieGenres(genreDetailsEntities);

        if (filters.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(filters);

    }

    @Override
    public ContentType type() {
        return new ContentType("MOVIE_GENRE_FILTER_LIST");
    }

}
