package com.gvtech.repository.movie;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.entity.movie.*;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.util.*;

@ApplicationScoped
@Startup
public class MovieRepository {

    @PersistenceContext
    EntityManager em;

    final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Transactional
    public MovieEntity findBy(final String language, final Long movieId) {
        final String sql = " SELECT m.movie_id AS m_movie_id, m.release_date AS m_release_date, m.year AS m_year,m.runtime AS m_runtime,m.rate AS m_rate,m.credits AS m_credits,m.url_path AS m_url_path," +
                " md.id AS md_id, md.movie_id AS md_movie_id, md.language AS md_language, md.title AS md_title, md.overview AS md_overview, md.poster_path AS md_poster_path, mgd.id AS mgd_id," +
                " mgd.genre_id AS mgd_genre_id,mgd.language AS mgd_language, mgd.name AS mgd_name, mwp.country AS mwp_country, wp.id AS wp_id, wp.watch_provider_id AS wp_watch_provider_id," +
                " wp.name AS wp_name, wp.enabled AS wp_enabled, wp.display_order AS wp_display_order FROM \"movie\".movie m JOIN \"movie\".movie_details md ON m.movie_id = md.movie_id" +
                " JOIN \"movie\".movie_genre mg ON m.movie_id = mg.movie_id JOIN \"movie\".genre_details mgd ON mg.genre_id = mgd.genre_id LEFT JOIN \"movie\".movie_watch_provider mwp ON m.movie_id = mwp.movie_id" +
                " LEFT JOIN \"movie\".watch_provider wp ON wp.watch_provider_id = mwp.watch_provider_id WHERE m.movie_id = :movieId AND mgd.language = :language AND md.language = :language";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("movieId", movieId);

        return map(query.getResultList());
    }


    // Exclusive Country para mejorar performance, si hay plataformas seleccionadas no traer plataformas que no se quieran
    @Transactional
    public List<MovieEntity> findByIdsAndLanguage(final String language, final String country, final boolean exclusiveCountry, final List<Long> movieIds) {
        final String sqlBase = " SELECT m.movie_id AS m_movie_id, m.release_date AS m_release_date, m.year AS m_year,m.runtime AS m_runtime,m.rate AS m_rate,m.credits AS m_credits,m.url_path AS m_url_path," +
                " md.id AS md_id, md.movie_id AS md_movie_id, md.language AS md_language, md.title AS md_title, md.overview AS md_overview, md.poster_path AS md_poster_path, mgd.id AS mgd_id," +
                " mgd.genre_id AS mgd_genre_id,mgd.language AS mgd_language, mgd.name AS mgd_name, mwp.country AS mwp_country, wp.id AS wp_id, wp.watch_provider_id AS wp_watch_provider_id," +
                " wp.name AS wp_name, wp.enabled AS wp_enabled, wp.display_order AS wp_display_order FROM \"movie\".movie m JOIN \"movie\".movie_details md ON m.movie_id = md.movie_id" +
                " JOIN \"movie\".movie_genre mg ON m.movie_id = mg.movie_id JOIN \"movie\".genre_details mgd ON mg.genre_id = mgd.genre_id LEFT JOIN \"movie\".movie_watch_provider mwp ON m.movie_id = mwp.movie_id" +
                " LEFT JOIN \"movie\".watch_provider wp ON wp.watch_provider_id = mwp.watch_provider_id WHERE m.movie_id IN (:movieIds) AND mgd.language = :language AND md.language = :language %s";

        String countryWhereClause = "";
        if (exclusiveCountry) {
            countryWhereClause = "AND mwp.country = :country ";
        }

        final String sql = String.format(sqlBase, countryWhereClause);
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("movieIds", movieIds);
        if (exclusiveCountry) {
            query.setParameter("country", country);
        }

        return mapList(query.getResultList());
    }


    // MINIMO UNA PLATAFORMA ASOCIADA Y ACTIVADA, SI NO HAY PLATAFORMAS SELECCIONADAS COGE CUALQUIAR TIPO DE CONTENIDO CON CUALQUIER PLATAFORMA ACTIVA EN CUALQUIER PAIS
    @SuppressWarnings("unchecked")
    @Transactional
    public Set<Long> findBy(final String language, final String country, final boolean exclusiveCountry, final List<Long> platforms, final List<Long> genres, final List<Integer> years) {
        final String sqlBase = " SELECT DISTINCT(m.movie_id) AS m_movie_id FROM \"movie\".movie m JOIN \"movie\".movie_details md ON m.movie_id = md.movie_id" +
                " JOIN \"movie\".movie_genre mg ON m.movie_id = mg.movie_id JOIN \"movie\".genre_details mgd ON mg.genre_id = mgd.genre_id JOIN \"movie\".movie_watch_provider mwp ON m.movie_id = mwp.movie_id" +
                " JOIN \"movie\".watch_provider wp ON wp.watch_provider_id = mwp.watch_provider_id WHERE mgd.language = :language AND md.language = :language" +
                " AND wp.enabled = true %s %s %s %s";

        String platformsWhereClause = "";
        String genresWhereClause = "";
        String yearsWhereClause = "";
        String countryWhereClause = "";
        if (exclusiveCountry) {
            countryWhereClause = "AND mwp.country = :country ";
        }
        if (!platforms.isEmpty()) {
            platformsWhereClause = "AND mwp.watch_provider_id IN (:platforms) ";
        }
        if (!genres.isEmpty()) {
            genresWhereClause = "AND mg.genre_id IN (:genres) ";
        }
        if (!years.isEmpty()) {
            yearsWhereClause = "AND m.year IN (:years)";
        }


        final String sql = String.format(sqlBase, countryWhereClause, platformsWhereClause, genresWhereClause, yearsWhereClause);

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        if (exclusiveCountry) {
            query.setParameter("country", country);
        }
        if (!platforms.isEmpty()) {
            query.setParameter("platforms", platforms);
        }
        if (!genres.isEmpty()) {
            query.setParameter("genres", genres);
        }
        if (!years.isEmpty()) {
            query.setParameter("years", years);
        }

        return mapIds(query.getResultList());
    }


    private Set<Long> mapIds(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        final Set<Long> ids = new HashSet<>();
        for (Tuple row : rows) {
            ids.add((Long) row.get("m_movie_id"));
        }

        return ids;
    }


    private List<MovieEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<MovieEntity> movieEntities = new ArrayList<>();

        long currentId = (Long) rows.getFirst().get("m_movie_id");
        final List<Tuple> toProcess = new ArrayList<>();
        for (Tuple row : rows) {
            if (currentId != (long) row.get("m_movie_id")) {
                currentId = (long) row.get("m_movie_id");
                movieEntities.add(map(toProcess));
                toProcess.clear();
            }
            toProcess.add(row);
        }

        movieEntities.add(map(toProcess));
        return movieEntities;
    }

    @SneakyThrows
    private MovieEntity map(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        boolean entityProcessed = false;
        final Set<String> watchProvidersProcessed = new HashSet<>();
        final Set<Long> genresProcessed = new HashSet<>();
        final Set<Long> movieDetailsProcessed = new HashSet<>();

        MovieEntity movieEntity = null;
        for (Tuple row : rows) {
            if (!entityProcessed) {
                final List<MovieEntity.CreditsMember> creditsMembers = this.mapper.readValue((String) row.get("m_credits"), new TypeReference<>() {
                });
                movieEntity = new MovieEntity((Long) row.get("m_movie_id"), (Date) row.get("m_release_date"), (Short) row.get("m_year"), (Short) row.get("m_runtime"), (Float) row.get("m_rate"), creditsMembers, (String) row.get("m_url_path"),
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                entityProcessed = true;
            }
            final Long movieDetailsId = (Long) row.get("md_id");
            if (movieDetailsProcessed.add(movieDetailsId)) {
                movieEntity.getDetails().add(new MovieDetailsEntity((Long) row.get("md_id"), (Long) row.get("md_movie_id"), (String) row.get("md_language"), (String) row.get("md_title"),
                        (String) row.get("md_overview"), (String) row.get("md_poster_path")));
            }

            final Long genreId = (Long) row.get("mgd_id");
            if (genresProcessed.add(genreId)) {
                movieEntity.getGenres().add(new GenreDetailsEntity(null, (Long) row.get("mgd_id"), (Long) row.get("mgd_genre_id"), (String) row.get("mgd_language"), (String) row.get("mgd_name")));

            }

            if (row.get("wp_id") != null) {
                final String watchProviderId = String.format("%s/%s", row.get("wp_id"), row.get("mwp_country"));
                if (watchProvidersProcessed.add(watchProviderId)) {
                    movieEntity.getWatchProviders().add(new MovieWatchProviderEntity((String) row.get("mwp_country"), new WatchProviderEntity(null, (Long) row.get("wp_id"), (Long) row.get("wp_watch_provider_id"),
                            (String) row.get("wp_name"), (Boolean) row.get("wp_enabled"), (Integer) row.get("wp_display_order"))));
                }
            }

        }


        return movieEntity;
    }


}
