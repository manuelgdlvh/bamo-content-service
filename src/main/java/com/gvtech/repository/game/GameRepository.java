package com.gvtech.repository.game;


import com.gvtech.entity.game.*;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Startup
public class GameRepository {

    @PersistenceContext
    EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional
    public GameEntity findBy(final String language, final Long gameId) {
        final String sql = "SELECT g.game_id AS g_game_id, g.cover AS g_cover, g.name AS g_name, g.rating AS g_rating, g.rating_count AS g_rating_count, g.url AS g_url, g.release_year AS g_release_year," +
                " gd.summary AS gd_summary, ge.genre_id AS ge_genre_id, ge.name AS ge_name, gm.game_mode_id AS gm_game_mode_id, gm.name AS gm_name, p.platform_id AS p_platform_id, p.name AS p_name" +
                " FROM \"game\".game g JOIN \"game\".game_details gd ON g.game_id = gd.game_id JOIN \"game\".game_genre gg ON g.game_id = gg.game_id JOIN \"game\".genre ge ON ge.genre_id = gg.genre_id " +
                " JOIN \"game\".game_platform gp ON g.game_id = gp.game_id JOIN \"game\".platform p ON p.platform_id = gp.platform_id JOIN \"game\".game_game_mode ggm ON ggm.game_id = g.game_id JOIN \"game\".game_mode gm " +
                " ON gm.game_mode_id = ggm.game_mode_id  WHERE gd.language = :language AND ge.language = :language AND gm.language = :language AND g.game_id = :gameId";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("gameId", gameId);

        return map(query.getResultList());
    }

    @Transactional
    public List<GameEntity> findBy(final String language, final List<Long> gameIds) {
        final String sql = "SELECT g.game_id AS g_game_id, g.cover AS g_cover, g.name AS g_name, g.rating AS g_rating, g.rating_count AS g_rating_count, g.url AS g_url, g.release_year AS g_release_year," +
                " gd.summary AS gd_summary, ge.genre_id AS ge_genre_id, ge.name AS ge_name, gm.game_mode_id AS gm_game_mode_id, gm.name AS gm_name, p.platform_id AS p_platform_id, p.name AS p_name" +
                " FROM \"game\".game g JOIN \"game\".game_details gd ON g.game_id = gd.game_id JOIN \"game\".game_genre gg ON g.game_id = gg.game_id JOIN \"game\".genre ge ON ge.genre_id = gg.genre_id " +
                " JOIN \"game\".game_platform gp ON g.game_id = gp.game_id JOIN \"game\".platform p ON p.platform_id = gp.platform_id JOIN \"game\".game_game_mode ggm ON ggm.game_id = g.game_id JOIN \"game\".game_mode gm " +
                " ON gm.game_mode_id = ggm.game_mode_id  WHERE gd.language = :language AND ge.language = :language AND gm.language = :language AND g.game_id IN (:gameIds)";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("gameIds", gameIds);

        return mapList(query.getResultList());
    }


    @SuppressWarnings("unchecked")
    @Transactional
    public Set<Long> findBy(final String language, final List<Long> platforms, final List<Long> genres, final List<Integer> years, final List<Long> gameModes) {
        final String sqlBase = "SELECT DISTINCT(g.game_id) AS g_game_id FROM \"game\".game g JOIN \"game\".game_details gd ON g.game_id = gd.game_id" +
                " JOIN \"game\".game_genre gg ON g.game_id = gg.game_id JOIN \"game\".genre ge ON ge.genre_id = gg.genre_id JOIN \"game\".game_platform gp ON g.game_id = gp.game_id" +
                " JOIN \"game\".platform p ON p.platform_id = gp.platform_id JOIN \"game\".game_game_mode ggm ON ggm.game_id = g.game_id JOIN \"game\".game_mode gm " +
                " ON gm.game_mode_id = ggm.game_mode_id WHERE gd.language = :language AND ge.language = :language AND gm.language = :language AND p.enabled = true %s %s %s %s";

        String platformsWhereClause = "";
        String genresWhereClause = "";
        String gameModesWhereClause = "";
        String yearsWhereClause = "";

        if (!years.isEmpty()) {
            yearsWhereClause = "AND g.release_year IN (:years) ";
        }
        if (!platforms.isEmpty()) {
            platformsWhereClause = "AND gp.platform_id IN (:platforms) ";
        }
        if (!genres.isEmpty()) {
            genresWhereClause = "AND gg.genre_id IN (:genres) ";
        }
        if (!gameModes.isEmpty()) {
            gameModesWhereClause = "AND ggm.game_mode_id IN (:gameModes)";
        }


        final String sql = String.format(sqlBase, platformsWhereClause, genresWhereClause, gameModesWhereClause, yearsWhereClause);

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        if (!years.isEmpty()) {
            query.setParameter("years", years);
        }
        if (!platforms.isEmpty()) {
            query.setParameter("platforms", platforms);
        }
        if (!genres.isEmpty()) {
            query.setParameter("genres", genres);
        }
        if (!gameModes.isEmpty()) {
            query.setParameter("gameModes", gameModes);
        }

        return mapIds(query.getResultList());
    }


    private Set<Long> mapIds(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        final Set<Long> ids = new HashSet<>();
        for (Tuple row : rows) {
            ids.add((Long) row.get("g_game_id"));
        }

        return ids;
    }


    private List<GameEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<GameEntity> gameEntities = new ArrayList<>();

        long currentId = (Long) rows.getFirst().get("g_game_id");
        final List<Tuple> toProcess = new ArrayList<>();
        for (Tuple row : rows) {
            if (currentId != (long) row.get("g_game_id")) {
                currentId = (long) row.get("g_game_id");
                gameEntities.add(map(toProcess));
                toProcess.clear();
            }
            toProcess.add(row);
        }

        gameEntities.add(map(toProcess));
        return gameEntities;
    }

    @SneakyThrows
    private GameEntity map(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }

        boolean entityProcessed = false;
        boolean gameDetailsProcessed = false;
        final Set<Long> platformsProcessed = new HashSet<>();
        final Set<Long> genresProcessed = new HashSet<>();
        final Set<Long> gameModesProcessed = new HashSet<>();

        GameEntity gameEntity = null;
        for (Tuple row : rows) {
            if (!entityProcessed) {
                gameEntity = new GameEntity((Long) row.get("g_game_id"), (String) row.get("g_cover"), (String) row.get("g_name"), (Double) row.get("g_rating"), (Integer) row.get("g_rating_count"), (String) row.get("g_url"),
                        (Integer) row.get("g_release_year"), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                entityProcessed = true;
            }

            if (!gameDetailsProcessed) {
                gameEntity.getDetails().add(new GameDetailsEntity(null, null, null, (String) row.get("gd_summary")));
                gameDetailsProcessed = true;
            }

            final Long genreId = (Long) row.get("ge_genre_id");
            if (genresProcessed.add(genreId)) {
                gameEntity.getGenres().add(new GenreEntity(null, null, null, (String) row.get("ge_name"), null));
            }
            final Long platformId = (Long) row.get("p_platform_id");
            if (platformsProcessed.add(platformId)) {
                gameEntity.getPlatformEntities().add(new PlatformEntity(null, null, (String) row.get("p_name"), null));
            }
            final Long gameModeId = (Long) row.get("gm_game_mode_id");
            if (gameModesProcessed.add(gameModeId)) {
                gameEntity.getGameModeEntities().add(new GameModeEntity(null, null, null, (String) row.get("gm_name"), null));
            }
        }

        return gameEntity;
    }


}
