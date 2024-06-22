package com.gvtech.repository.tv;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.entity.tv.*;
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
public class TvRepository {

    final ObjectMapper mapper = new ObjectMapper();
    @PersistenceContext
    EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional
    public TvEntity findBy(final String language, final Long tvId) {
        final String sql = " SELECT t.tv_id AS t_tv_id, t.release_date AS t_release_date, t.year AS t_year,t.number_of_episodes AS t_number_of_episodes,t.number_of_seasons AS t_number_of_seasons ,t.rate AS t_rate,t.credits AS t_credits,t.url_path AS t_url_path," +
                " td.id AS td_id, td.tv_id AS td_tv_id, td.language AS td_language, td.title AS td_title, td.overview AS td_overview, td.poster_path AS td_poster_path, tgd.id AS tgd_id," +
                " tgd.genre_id AS tgd_genre_id,tgd.language AS tgd_language, tgd.name AS tgd_name, twp.country AS twp_country, wp.id AS wp_id, wp.watch_provider_id AS wp_watch_provider_id," +
                " wp.name AS wp_name, wp.enabled AS wp_enabled, wp.display_order AS wp_display_order FROM \"tv\".tv t JOIN \"tv\".tv_details td ON t.tv_id = td.tv_id" +
                " JOIN \"tv\".tv_genre tg ON t.tv_id = tg.tv_id JOIN \"tv\".genre_details tgd ON tg.genre_id = tgd.genre_id LEFT JOIN \"tv\".tv_watch_provider twp ON t.tv_id = twp.tv_id" +
                " LEFT JOIN \"tv\".watch_provider wp ON wp.watch_provider_id = twp.watch_provider_id WHERE t.tv_id = :tvId AND tgd.language = :language AND td.language = :language";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("tvId", tvId);

        return map(query.getResultList());
    }


    // Exclusive Country para mejorar performance, si hay plataformas seleccionadas no traer plataformas que no se quieran
    @Transactional
    public List<TvEntity> findByIdsAndLanguage(final String language, final String country, final boolean exclusiveCountry, final List<Long> tvIds) {
        final String sqlBase = " SELECT t.tv_id AS t_tv_id, t.release_date AS t_release_date, t.year AS t_year,t.number_of_episodes AS t_number_of_episodes,t.number_of_seasons AS t_number_of_seasons, t.rate AS t_rate,t.credits AS t_credits,t.url_path AS t_url_path," +
                " td.id AS td_id, td.tv_id AS td_tv_id, td.language AS td_language, td.title AS td_title, td.overview AS td_overview, td.poster_path AS td_poster_path, tgd.id AS tgd_id," +
                " tgd.genre_id AS tgd_genre_id,tgd.language AS tgd_language, tgd.name AS tgd_name, twp.country AS twp_country, wp.id AS wp_id, wp.watch_provider_id AS wp_watch_provider_id," +
                " wp.name AS wp_name, wp.enabled AS wp_enabled, wp.display_order AS wp_display_order FROM \"tv\".tv t JOIN \"tv\".tv_details td ON t.tv_id = td.tv_id" +
                " JOIN \"tv\".tv_genre tg ON t.tv_id = tg.tv_id JOIN \"tv\".genre_details tgd ON tg.genre_id = tgd.genre_id LEFT JOIN \"tv\".tv_watch_provider twp ON t.tv_id = twp.tv_id" +
                " LEFT JOIN \"tv\".watch_provider wp ON wp.watch_provider_id = twp.watch_provider_id WHERE t.tv_id IN (:tvIds) AND tgd.language = :language AND td.language = :language %s";

        String countryWhereClause = "";
        if (exclusiveCountry) {
            countryWhereClause = "AND twp.country = :country ";
        }

        final String sql = String.format(sqlBase, countryWhereClause);
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("tvIds", tvIds);
        if (exclusiveCountry) {
            query.setParameter("country", country);
        }

        return mapList(query.getResultList());
    }


    // MINIMO UNA PLATAFORMA ASOCIADA Y ACTIVADA, SI NO HAY PLATAFORMAS SELECCIONADAS COGE CUALQUIAR TIPO DE CONTENIDO CON CUALQUIER PLATAFORMA ACTIVA EN CUALQUIER PAIS
    @SuppressWarnings("unchecked")
    @Transactional
    public Set<Long> findBy(final String language, final String country, final boolean exclusiveCountry, final List<Long> platforms, final List<Long> genres, final List<Integer> years) {
        final String sqlBase = " SELECT DISTINCT(t.tv_id) AS t_tv_id FROM \"tv\".tv t JOIN \"tv\".tv_details td ON t.tv_id = td.tv_id" +
                " JOIN \"tv\".tv_genre tg ON t.tv_id = tg.tv_id JOIN \"tv\".genre_details tgd ON tg.genre_id = tgd.genre_id JOIN \"tv\".tv_watch_provider twp ON t.tv_id = twp.tv_id" +
                " JOIN \"tv\".watch_provider wp ON wp.watch_provider_id = twp.watch_provider_id WHERE tgd.language = :language AND td.language = :language" +
                " AND wp.enabled = true %s %s %s %s";

        String platformsWhereClause = "";
        String genresWhereClause = "";
        String yearsWhereClause = "";
        String countryWhereClause = "";
        if (exclusiveCountry) {
            countryWhereClause = "AND twp.country = :country ";
        }
        if (!platforms.isEmpty()) {
            platformsWhereClause = "AND twp.watch_provider_id IN (:platforms) ";
        }
        if (!genres.isEmpty()) {
            genresWhereClause = "AND tg.genre_id IN (:genres) ";
        }
        if (!years.isEmpty()) {
            yearsWhereClause = "AND t.year IN (:years)";
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
            ids.add((Long) row.get("t_tv_id"));
        }

        return ids;
    }


    private List<TvEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<TvEntity> tvEntities = new ArrayList<>();

        long currentId = (Long) rows.getFirst().get("t_tv_id");
        final List<Tuple> toProcess = new ArrayList<>();
        for (Tuple row : rows) {
            if (currentId != (long) row.get("t_tv_id")) {
                currentId = (long) row.get("t_tv_id");
                tvEntities.add(map(toProcess));
                toProcess.clear();
            }
            toProcess.add(row);
        }

        tvEntities.add(map(toProcess));
        return tvEntities;
    }

    @SneakyThrows
    private TvEntity map(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        boolean entityProcessed = false;
        final Set<String> watchProvidersProcessed = new HashSet<>();
        final Set<Long> genresProcessed = new HashSet<>();
        final Set<Long> tvDetailsProcessed = new HashSet<>();

        TvEntity tvEntity = null;
        for (Tuple row : rows) {
            if (!entityProcessed) {
                final List<TvEntity.CreditsMember> creditsMembers = this.mapper.readValue((String) row.get("t_credits"), new TypeReference<>() {
                });
                tvEntity = new TvEntity((Long) row.get("t_tv_id"), (Date) row.get("t_release_date"), (Short) row.get("t_year"), (Short) row.get("t_number_of_episodes"), (Short) row.get("t_number_of_seasons"), (Float) row.get("t_rate"), creditsMembers, (String) row.get("t_url_path"),
                        new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                entityProcessed = true;
            }
            final Long tvDetailsId = (Long) row.get("td_id");
            if (tvDetailsProcessed.add(tvDetailsId)) {
                tvEntity.getDetails().add(new TvDetailsEntity((Long) row.get("td_id"), (Long) row.get("td_tv_id"), (String) row.get("td_language"), (String) row.get("td_title"),
                        (String) row.get("td_overview"), (String) row.get("td_poster_path")));
            }

            final Long genreId = (Long) row.get("tgd_id");
            if (genresProcessed.add(genreId)) {
                tvEntity.getGenres().add(new GenreDetailsEntity(null, (Long) row.get("tgd_id"), (Long) row.get("tgd_genre_id"), (String) row.get("tgd_language"), (String) row.get("tgd_name")));

            }

            if (row.get("wp_id") != null) {
                final String watchProviderId = String.format("%s/%s", row.get("wp_id"), row.get("twp_country"));
                if (watchProvidersProcessed.add(watchProviderId)) {
                    tvEntity.getWatchProviders().add(new TvWatchProviderEntity((String) row.get("twp_country"), new WatchProviderEntity(null, (Long) row.get("wp_id"), (Long) row.get("wp_watch_provider_id"),
                            (String) row.get("wp_name"), (Boolean) row.get("wp_enabled"), (Integer) row.get("wp_display_order"))));
                }
            }

        }


        return tvEntity;
    }


}
