package com.gvtech.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.Content;
import com.gvtech.model.ContentWithMetadataList;
import com.gvtech.model.Filter;
import com.gvtech.model.FilterList;
import com.gvtech.service.ContentService;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Path("/content")
public class AdapterResource {

    final ObjectMapper mapper = new ObjectMapper();
    @Inject
    ContentService contentService;

    @SuppressWarnings("unchecked")
    @Path("/byFilter")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public List<Content> findByFilter(final Request request, @RestHeader("Language") String language, @RestHeader("Country") String country) throws JsonProcessingException {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }
        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/SIZE
        final List<Content> result;
        switch (request.getContentType()) {
            case "MOVIE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), 30));
                result = (List<Content>) contentService.get(new ContentType("MOVIE_RANDOMIZED_LIST"), contentId);
            }
            case "TV" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), 30));
                result = (List<Content>) contentService.get(new ContentType("TV_RANDOMIZED_LIST"), contentId);
            }
            case "GAME" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), request.getGameModesAsString(), 30));
                result = (List<Content>) contentService.get(new ContentType("GAME_RANDOMIZED_LIST"), contentId);
            }
            case "RECIPE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, request.getCuisinesAsString(), request.getDietsAsString(), request.getTypesAsString(), 30));
                result = (List<Content>) contentService.get(new ContentType("RECIPE_RANDOMIZED_LIST"), contentId);
            }
            case "CUSTOM" -> {
                final ContentId contentId = new ContentId(String.format("%s", mapper.writeValueAsString(request.getContentFilter())));
                result = (List<Content>) contentService.get(new ContentType("CUSTOM_LIST"), contentId);
            }
            default -> result = null;
        }

        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }


    @Path("/explore")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public ContentWithMetadataList explore(final ExploreRequest request, @RestHeader("Language") String language, @RestHeader("Country") String country) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }
        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        if (request.getFilterBy() == null) {
            return new ContentWithMetadataList(new ArrayList<>());
        }

        final ContentWithMetadataList result;
        switch (request.getContentType()) {
            case "MOVIE" -> {
                if (request.getKeyword() == null) {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(),
                            request.getFilterBy(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("MOVIE_METADATA_LIST"), contentId);
                } else {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s", language, country, request.getKeyword(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("MOVIE_METADATA_SEARCHABLE_LIST"), contentId);
                }
            }
            case "TV" -> {
                if (request.getKeyword() == null) {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(),
                            request.getFilterBy(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("TV_METADATA_LIST"), contentId);
                } else {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s", language, country, request.getKeyword(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("TV_METADATA_SEARCHABLE_LIST"), contentId);
                }

            }
            case "GAME" -> {

                if (request.getKeyword() == null) {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s/%s", language, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), request.getGameModesAsString(),
                            request.getFilterBy(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("GAME_METADATA_LIST"), contentId);
                } else {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s", language, request.getKeyword(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("GAME_METADATA_SEARCHABLE_LIST"), contentId);
                }

            }
            case "RECIPE" -> {
                if (request.getKeyword() == null) {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, request.getCuisinesAsString(), request.getDietsAsString(), request.getTypesAsString(),
                            request.getFilterBy(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("RECIPE_METADATA_LIST"), contentId);
                } else {
                    final ContentId contentId = new ContentId(String.format("%s/%s/%s", language, request.getKeyword(), request.getPage()));
                    result = (ContentWithMetadataList) contentService.get(new ContentType("RECIPE_METADATA_SEARCHABLE_LIST"), contentId);
                }


            }
            default -> result = null;
        }

        if (result == null) {
            return new ContentWithMetadataList(new ArrayList<>());
        }

        return result;
    }


    @Path("/random")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Object findRandom(final Request request, @RestHeader("Language") String language, @RestHeader("Country") String country) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }
        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/SIZE
        switch (request.getContentType()) {
            case "MOVIE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString()));
                return contentService.get(new ContentType("MOVIE_RANDOMIZED"), contentId);
            }
            case "TV" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString()));
                return contentService.get(new ContentType("TV_RANDOMIZED"), contentId);
            }
            case "GAME" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), request.getGameModesAsString()));
                return contentService.get(new ContentType("GAME_RANDOMIZED"), contentId);
            }
            case "RECIPE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s", language, request.getCuisinesAsString(), request.getDietsAsString(), request.getTypesAsString()));
                return contentService.get(new ContentType("RECIPE_RANDOMIZED"), contentId);
            }
            default -> throw new RuntimeException("CONTENT_NOT_FOUND");
        }
    }


    @SuppressWarnings("unchecked")
    @Path("/byIds")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public List<Content> findByIds(final RequestByIds request, @RestHeader("Language") String language, @RestHeader("Country") String country) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }
        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        // PATTERN LANGUAGE/COUNTRY/IDS
        final List<Content> result;
        switch (request.getContentType()) {
            case "MOVIE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s", language, country, request.getIds().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))));
                result = (List<Content>) contentService.get(new ContentType("MOVIE_LIST_BY_IDS"), contentId);
            }
            case "TV" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s", language, country, request.getIds().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))));
                result = (List<Content>) contentService.get(new ContentType("TV_LIST_BY_IDS"), contentId);
            }
            case "GAME" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s", language, request.getIds().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))));
                result = (List<Content>) contentService.get(new ContentType("GAME_LIST_BY_IDS"), contentId);
            }
            case "RECIPE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s", language, request.getIds().stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))));
                result = (List<Content>) contentService.get(new ContentType("RECIPE_LIST_BY_IDS"), contentId);
            }
            default -> result = null;
        }

        if (result == null) {
            return new ArrayList<>();
        }

        return result;
    }


    @Path("/hasResults")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public HasResultsResponse hasResults(final Request request, @RestHeader("Language") String language, @RestHeader("Country") String country) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }
        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }
        // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/SIZE
        switch (request.getContentType()) {
            case "MOVIE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString()));
                return new HasResultsResponse(new HasResultsResponse.Response((Boolean) this.contentService.get(new ContentType("HAS_MOVIE_RANDOMIZED_LIST"), contentId)));
            }
            case "TV" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, country, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString()));
                return new HasResultsResponse(new HasResultsResponse.Response((Boolean) this.contentService.get(new ContentType("HAS_TV_RANDOMIZED_LIST"), contentId)));
            }
            case "GAME" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, request.getPlatformsAsString(), request.getGenresAsString(), request.getYearsAsString(), request.getGameModesAsString()));
                return new HasResultsResponse(new HasResultsResponse.Response((Boolean) this.contentService.get(new ContentType("HAS_GAME_RANDOMIZED_LIST"), contentId)));
            }
            case "RECIPE" -> {
                final ContentId contentId = new ContentId(String.format("%s/%s/%s/%s", language, request.getCuisinesAsString(), request.getDietsAsString(), request.getTypesAsString()));
                return new HasResultsResponse(new HasResultsResponse.Response((Boolean) this.contentService.get(new ContentType("HAS_RECIPE_RANDOMIZED_LIST"), contentId)));
            }
            default -> throw new RuntimeException("CONTENT_NOT_FOUND");
        }
    }

    @SuppressWarnings("unchecked")
    @Path("/movie/genres")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public FilterList findAllGenres(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("MOVIE_GENRE_FILTER_LIST"), contentId));
    }

    @Path("/movie/watchProviders")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllWatchProviders(@RestHeader("Country") String country) {

        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        final ContentId contentId = new ContentId(country);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("MOVIE_WATCH_PROVIDER_FILTER_LIST"), contentId));
    }

    @Path("/tv/genres")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllTvGenres(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("TV_GENRE_FILTER_LIST"), contentId));
    }

    @Path("/tv/watchProviders")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllTvWatchProviders(@RestHeader("Country") String country) {

        if (country == null || country.isBlank()) {
            Log.warn("country not provided, using default...");
            country = "US";
        }

        final ContentId contentId = new ContentId(country);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("TV_WATCH_PROVIDER_FILTER_LIST"), contentId));
    }


    @Path("/game/genres")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllGameGenres(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);

        return new FilterList((List<Filter>) this.contentService.get(new ContentType("GAME_GENRE_FILTER_LIST"), contentId));
    }

    @Path("/game/platforms")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllPlatforms() {

        final ContentId contentId = new ContentId("");
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("GAME_PLATFORM_FILTER_LIST"), contentId));
    }


    @Path("/game/gameModes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllGameModes(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("GAME_MODE_FILTER_LIST"), contentId));
    }

    @Path("/recipe/cuisines")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllRecipeCuisines(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("RECIPE_CUISINE_FILTER_LIST"), contentId));
    }

    @Path("/recipe/diets")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllRecipeDiets(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("RECIPE_DIET_FILTER_LIST"), contentId));
    }

    @Path("/recipe/types")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    @SuppressWarnings("unchecked")
    public FilterList findAllRecipeTypes(@RestHeader("Language") String language) {
        if (language == null || language.isBlank()) {
            Log.warn("language not provided, using default...");
            language = "EN";
        }

        final ContentId contentId = new ContentId(language);
        return new FilterList((List<Filter>) this.contentService.get(new ContentType("RECIPE_TYPE_FILTER_LIST"), contentId));
    }


}
