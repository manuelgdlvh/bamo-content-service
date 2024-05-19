package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.tv.Tv;
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
public class TvRandomizedListProvider extends AbstractCacheableContentProvider<List<Tv>> {

    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/SIZE
    @Inject
    TvIdsProvider tvIdsProvider;
    @Inject
    TvListByIdsProvider tvListByIdsProvider;

    public TvRandomizedListProvider() {
        super((long) 1000 * 60);
    }

    @Override
    public boolean equals(List<Tv> o1, List<Tv> o2) {
        return true;
    }

    @Override
    public ContentBuild<List<Tv>> build(ContentId contentId) {
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

        final ContentId tvIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, exclusiveCountry, platforms, genres, years));
        final Set<Long> tvIdsSet = this.tvIdsProvider.get(tvIdsContentId);
        if (tvIdsSet == null) {
            return null;
        }
        List<Long> tvIds = new ArrayList<>(tvIdsSet);

        Collections.shuffle(tvIds);
        if (size < tvIds.size()) {
            tvIds = tvIds.subList(0, size);
        }

        final String tvIdsAsStr = tvIds.stream().map(Object::toString).collect(Collectors.joining(","));
        final ContentId tvByIdsContentId = new ContentId(String.format("%s/%s/%s", language, country, tvIdsAsStr));
        return new ContentBuild<>(tvListByIdsProvider.get(tvByIdsContentId));
    }

    @Override
    public ContentType type() {
        return new ContentType("TV_RANDOMIZED_LIST");
    }

}
