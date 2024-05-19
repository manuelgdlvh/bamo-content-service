package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.repository.game.GameRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
@Startup
public class GameIdsProvider extends AbstractCacheableContentProvider<Set<Long>> {

    // PATTERN LANGUAGE/PLATFORMS/GENRES/YEARS/GAME_MODES
    @Inject
    GameRepository gameRepository;

    public GameIdsProvider() {
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
        if (parts.length < 5) {
            return null;
        }

        final String language = parts[0];

        final List<Long> platforms = new ArrayList<>();
        if (!Objects.equals(parts[1], "NONE")) {
            for (String platform : parts[1].split(",")) {
                platforms.add(Long.valueOf(platform));
            }
        }

        final List<Long> genres = new ArrayList<>();
        if (!Objects.equals(parts[2], "NONE")) {
            for (String genre : parts[2].split(",")) {
                genres.add(Long.valueOf(genre));
            }
        }


        final Set<Integer> years = new HashSet<>();
        if (!Objects.equals(parts[3], "NONE")) {
            for (String yearRange : parts[3].split(",")) {
                final String[] yearRangeSplit = yearRange.split("-");
                int lowBound = Integer.parseInt(yearRangeSplit[0]);
                final int highBound = Integer.parseInt(yearRangeSplit[1]);

                do {
                    years.add(lowBound);
                    lowBound += 1;
                } while (lowBound < highBound);
            }
        }

        final List<Long> gameModes = new ArrayList<>();
        if (!Objects.equals(parts[4], "NONE")) {
            for (String gameMode : parts[4].split(",")) {
                gameModes.add(Long.valueOf(gameMode));
            }
        }


        final Set<Long> ids = gameRepository.findBy(language, platforms, genres, years.stream().toList(), gameModes);
        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return new ContentBuild<>(ids);
    }


    @Override
    public ContentType type() {
        return new ContentType("GAME_IDS");
    }
}
