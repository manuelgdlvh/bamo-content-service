package com.gvtech.repository.game;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.game.GameModeEntity;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Startup
public class GameModeRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<GameModeEntity> findAll(final String language) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, gm.name AS gm_name, gm.game_mode_id AS gm_game_mode_id FROM \"game\".game_mode gm " +
                "JOIN \"content\".content c ON gm.game_mode_id = c.internal_id WHERE gm.language = :language AND gm.enabled = true";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        return mapList(query.getResultList());
    }


    private List<GameModeEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<GameModeEntity> gameModeEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final GameModeEntity gameModeEntity = map(row);
            gameModeEntities.add(gameModeEntity);
        }

        return gameModeEntities;
    }

    @SneakyThrows
    private GameModeEntity map(final Tuple row) {
        return new GameModeEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("gm_game_mode_id"), (String) row.get("gm_name"), null);
    }


}
