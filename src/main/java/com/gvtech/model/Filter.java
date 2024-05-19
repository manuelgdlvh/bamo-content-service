package com.gvtech.model;

import com.gvtech.entity.game.GameModeEntity;
import com.gvtech.entity.game.GenreEntity;
import com.gvtech.entity.game.PlatformEntity;
import com.gvtech.entity.movie.GenreDetailsEntity;
import com.gvtech.entity.movie.WatchProviderEntity;
import com.gvtech.entity.recipe.CuisineEntity;
import com.gvtech.entity.recipe.DietEntity;
import com.gvtech.entity.recipe.TypeEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Filter {
    private long id;
    private String external_code;
    private String provider_code;
    private String name;


    public static List<Filter> mapMovieWatchProviders(final List<WatchProviderEntity> watchProviderEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (WatchProviderEntity watchProviderEntity : watchProviderEntities) {
            filters.add(new Filter(watchProviderEntity.getWatchProviderId(), watchProviderEntity.getContent().getExternalCode(), watchProviderEntity.getContent().getProviderCode(), watchProviderEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapMovieGenres(final List<GenreDetailsEntity> genreDetailsEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (GenreDetailsEntity genreDetailsEntity : genreDetailsEntities) {
            filters.add(new Filter(genreDetailsEntity.getGenreId(), genreDetailsEntity.getContent().getExternalCode(), genreDetailsEntity.getContent().getProviderCode(), genreDetailsEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapGameGenres(final List<GenreEntity> genreEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (GenreEntity genreEntity : genreEntities) {
            filters.add(new Filter(genreEntity.getGenreId(), genreEntity.getContent().getExternalCode(), genreEntity.getContent().getProviderCode(), genreEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapGamePlatforms(final List<PlatformEntity> platformEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (PlatformEntity platformEntity : platformEntities) {
            filters.add(new Filter(platformEntity.getPlatformId(), platformEntity.getContent().getExternalCode(), platformEntity.getContent().getProviderCode(), platformEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapGameModes(final List<GameModeEntity> gameModeEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (GameModeEntity gameModeEntity : gameModeEntities) {
            filters.add(new Filter(gameModeEntity.getGameModeId(), gameModeEntity.getContent().getExternalCode(), gameModeEntity.getContent().getProviderCode(), gameModeEntity.getName()));
        }
        return filters;
    }


    public static List<Filter> mapTvGenres(final List<com.gvtech.entity.tv.GenreDetailsEntity> genreDetailsEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (com.gvtech.entity.tv.GenreDetailsEntity genreDetailsEntity : genreDetailsEntities) {
            filters.add(new Filter(genreDetailsEntity.getGenreId(), genreDetailsEntity.getContent().getExternalCode(), genreDetailsEntity.getContent().getProviderCode(), genreDetailsEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapTvWatchProviders(final List<com.gvtech.entity.tv.WatchProviderEntity> watchProviderEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (com.gvtech.entity.tv.WatchProviderEntity watchProviderEntity : watchProviderEntities) {
            filters.add(new Filter(watchProviderEntity.getWatchProviderId(), watchProviderEntity.getContent().getExternalCode(), watchProviderEntity.getContent().getProviderCode(), watchProviderEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapRecipeCuisines(final List<CuisineEntity> cuisineEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (CuisineEntity cuisineEntity : cuisineEntities) {
            filters.add(new Filter(cuisineEntity.getCuisineId(), cuisineEntity.getContent().getExternalCode(), cuisineEntity.getContent().getProviderCode(), cuisineEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapRecipeDiets(final List<DietEntity> dietEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (DietEntity dietEntity : dietEntities) {
            filters.add(new Filter(dietEntity.getDietId(), dietEntity.getContent().getExternalCode(), dietEntity.getContent().getProviderCode(), dietEntity.getName()));
        }
        return filters;
    }

    public static List<Filter> mapRecipeTypes(final List<TypeEntity> typeEntities) {
        final List<Filter> filters = new ArrayList<>();
        for (TypeEntity typeEntity : typeEntities) {
            filters.add(new Filter(typeEntity.getTypeId(), typeEntity.getContent().getExternalCode(), typeEntity.getContent().getProviderCode(), typeEntity.getName()));
        }
        return filters;
    }


}
