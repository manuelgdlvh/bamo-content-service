package com.gvtech.model.movie;

import com.gvtech.entity.movie.*;
import com.gvtech.model.Content;

import java.io.Serializable;
import java.util.List;

public record Movie(Long id, String title, String release_date, List<String> genres, List<String> watch_providers,
                    String url_path, String poster_path, Short runtime, String description, Float rate,
                    List<CreditsMemberResponse> credits) implements Content {


    public record CreditsMemberResponse(String name, String type) implements Serializable {
    }


    public static Movie map(final MovieEntity movieEntity, final String country) {
        final MovieDetailsEntity movieDetails = movieEntity.getDetails().getFirst();
        final List<WatchProviderEntity> watchProviderEntities = movieEntity.getWatchProviders().stream()
                .filter(movieWatchProviderEntity -> movieWatchProviderEntity.getCountry().equals(country)).map(MovieWatchProviderEntity::getWatchProviderEntity).toList();

        final List<String> genres = movieEntity.getGenres().stream().map(GenreDetailsEntity::getName).toList();
        final List<String> watchProviders = watchProviderEntities.stream().map(WatchProviderEntity::getName).toList();
        final List<CreditsMemberResponse> creditsMemberResponses = movieEntity.getCredits().stream().map(creditsMember -> new CreditsMemberResponse(creditsMember.getName(), creditsMember.getType())).toList();

        final String urlPath = String.format("%s%s", "https://www.themoviedb.org", movieEntity.getUrlPath());
        final String posterPath = String.format("%s%s", "https://image.tmdb.org/t/p/w600_and_h900_bestv2", movieDetails.getPosterPath());
        return new Movie(movieEntity.getMovieId(), movieDetails.getTitle(), movieEntity.getReleaseDate().toString(), genres, watchProviders, urlPath, posterPath, movieEntity.getRuntime(), movieDetails.getOverview(),
                movieEntity.getRate(), creditsMemberResponses);
    }

    public static List<Movie> mapList(final List<MovieEntity> movieEntities, final String country) {
        return movieEntities.stream().map(movieEntity -> map(movieEntity, country)).toList();
    }


    @Override
    public String getContentType() {
        return "MOVIE";
    }
}

