package com.gvtech.repository.game;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.game.GenreEntity;
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
public class GenreRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<GenreEntity> findAll(final String language) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, g.genre_id AS g_genre_id, g.name AS g_name " + "FROM \"game\".genre g JOIN \"content\".content c ON g.genre_id = c.internal_id WHERE g.language = :language AND g.enabled = true";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        return mapList(query.getResultList());
    }


    private List<GenreEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<GenreEntity> genreDetailsEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final GenreEntity genreDetailsEntity = map(row);
            genreDetailsEntities.add(genreDetailsEntity);
        }

        return genreDetailsEntities;
    }

    @SneakyThrows
    private GenreEntity map(final Tuple row) {
        return new GenreEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("g_genre_id"), (String) row.get("g_name"), null);
    }


}
