package com.gvtech.model.tv;


import com.gvtech.entity.tv.*;
import com.gvtech.model.Content;

import java.io.Serializable;
import java.util.List;

public record Tv(Long id, String title, String release_date,
                 List<String> genres,
                 List<String> watch_providers, String url_path,
                 String poster_path,
                 Short number_of_episodes, Short number_of_seasons, String description
        , Float rate, List<CreditsMemberResponse> credits) implements Content {


    @Override
    public String getContentType() {
        return "TV";
    }


    public record CreditsMemberResponse(String name, String type) implements Serializable {
    }

    public static Tv map(final TvEntity tvEntity, final String country) {
        final TvDetailsEntity tvDetails = tvEntity.getDetails().getFirst();
        final List<WatchProviderEntity> watchProviderEntities = tvEntity.getWatchProviders().stream().filter(tvWatchProviderEntity -> tvWatchProviderEntity.getCountry().equals(country)).map(TvWatchProviderEntity::getWatchProviderEntity).toList();

        final List<String> genres = tvEntity.getGenres().stream().map(GenreDetailsEntity::getName).toList();
        final List<String> watchProviders = watchProviderEntities.stream().map(WatchProviderEntity::getName).toList();
        final List<Tv.CreditsMemberResponse> creditsMemberResponses = tvEntity.getCredits().stream().map(creditsMember -> new Tv.CreditsMemberResponse(creditsMember.getName(), creditsMember.getType())).toList();

        final String urlPath = String.format("%s%s", "https://www.themoviedb.org", tvEntity.getUrlPath());
        final String posterPath = String.format("%s%s", "https://image.tmdb.org/t/p/w600_and_h900_bestv2", tvDetails.getPosterPath());
        return new Tv(tvEntity.getTvId(), tvDetails.getTitle(), tvEntity.getReleaseDate().toString(), genres, watchProviders, urlPath, posterPath, tvEntity.getNumberOfEpisodes(), tvEntity.getNumberOfSeasons(), tvDetails.getOverview(),
                tvEntity.getRate(), creditsMemberResponses);
    }

    public static List<Tv> mapList(final List<TvEntity> tvEntities, final String country) {
        return tvEntities.stream().map(tvEntity -> map(tvEntity, country)).toList();
    }


}

