package com.gvtech.provider.movie;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;


@ApplicationScoped
@Startup
public class HasMoviesRandomizedListProvider implements ContentProvider<Boolean> {

    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS
    @Inject
    MovieIdsProvider movieIdsProvider;


    @Override
    public Boolean get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 5) {
            return false;
        }

        final String language = parts[0];
        final String country = parts[1];
        final String platforms = parts[2];
        final String genres = parts[3];
        final String years = parts[4];

        String exclusiveCountry = "TRUE";
        if (platforms.equals("NONE")) {
            exclusiveCountry = "FALSE";
        }

        final ContentId movieIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, exclusiveCountry, platforms, genres, years));

        final Set<Long> ids = movieIdsProvider.get(movieIdsContentId);
        return ids != null && !ids.isEmpty();

    }

    @Override
    public ContentType type() {
        return new ContentType("HAS_MOVIE_RANDOMIZED_LIST");
    }

}
