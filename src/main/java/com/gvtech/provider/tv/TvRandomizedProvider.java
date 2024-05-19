package com.gvtech.provider.tv;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.tv.Tv;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Random;


@ApplicationScoped
@Startup
public class TvRandomizedProvider implements ContentProvider<Tv> {

    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS
    @Inject
    TvIdsProvider tvIdsProvider;
    @Inject
    TvProvider tvProvider;

    final Random random = new Random();


    @Override
    public Tv get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 5) {
            return null;
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

        final ContentId tvContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, exclusiveCountry, platforms, genres, years));
        final List<Long> tvIds = this.tvIdsProvider.get(tvContentId).stream().toList();
        // MAYBE THROW EXCEPTION TO AVOID NULL CHECK IN EVERY POINT???
        if (tvIds == null) {
            return null;
        }

        final long id = tvIds.get(random.nextInt(tvIds.size()));

        return this.tvProvider.get(new ContentId(String.format("%s/%s/%s", language, country, id)));
    }

    @Override
    public ContentType type() {
        return new ContentType("TV_RANDOMIZED");
    }

}
