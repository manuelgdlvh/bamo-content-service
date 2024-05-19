package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.repository.tv.TvRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
@Startup
public class TvIdsProvider extends AbstractCacheableContentProvider<Set<Long>> {

    // PATTERN LANGUAGE/COUNTRY/COUNTRY-EXCLUSIVE/PLATFORMS/GENRES/YEARS/
    @Inject
    TvRepository tvRepository;

    // TODO
    public TvIdsProvider() {
        super((long) (1000 * 90));
    }

    @Override
    public boolean equals(Set<Long> o1, Set<Long> o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Set<Long>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 6) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final boolean exclusiveCountry = Boolean.parseBoolean(parts[2]);

        final List<Long> platforms = new ArrayList<>();
        if (!Objects.equals(parts[3], "NONE")) {
            for (String platform : parts[3].split(",")) {
                platforms.add(Long.valueOf(platform));
            }
        }

        final List<Long> genres = new ArrayList<>();
        if (!Objects.equals(parts[4], "NONE")) {
            for (String genre : parts[4].split(",")) {
                genres.add(Long.valueOf(genre));
            }
        }

        final Set<Integer> years = new HashSet<>();
        if (!Objects.equals(parts[5], "NONE")) {
            for (String yearRange : parts[5].split(",")) {
                final String[] yearRangeSplit = yearRange.split("-");
                int lowBound = Integer.parseInt(yearRangeSplit[0]);
                final int highBound = Integer.parseInt(yearRangeSplit[1]);

                do {
                    years.add(lowBound);
                    lowBound += 1;
                } while (lowBound < highBound);
            }
        }

        final Set<Long> ids = tvRepository.findBy(language, country, exclusiveCountry, platforms, genres, years.stream().toList());
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(ids);
    }


    @Override
    public ContentType type() {
        return new ContentType("TV_IDS");
    }
}
