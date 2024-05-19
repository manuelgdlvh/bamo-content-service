package com.gvtech.repository.movie;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.movie.GenreDetailsEntity;
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
public class GenreDetailsRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<GenreDetailsEntity> findAll(final String language) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, gd.genre_id AS gd_genre_id, gd.name AS gd_name " +
                "FROM \"movie\".genre_details gd JOIN \"content\".content c ON gd.genre_id = c.internal_id WHERE gd.language = :language ";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        return mapList(query.getResultList());
    }


    private List<GenreDetailsEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<GenreDetailsEntity> genreDetailsEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final GenreDetailsEntity genreDetailsEntity = map(row);
            genreDetailsEntities.add(genreDetailsEntity);
        }

        return genreDetailsEntities;
    }

    @SneakyThrows
    private GenreDetailsEntity map(final Tuple row) {
        return new GenreDetailsEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("gd_genre_id"), null,
                (String) row.get("gd_name"));
    }


}
