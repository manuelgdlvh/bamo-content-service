package com.gvtech.repository.recipe;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.recipe.DietEntity;
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
public class DietRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<DietEntity> findAll(final String language) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, d.diet_id AS d_diet_id, d.name AS d_name " +
                "FROM \"recipe\".diet d JOIN \"content\".content c ON d.diet_id = c.internal_id WHERE d.language = :language AND d.enabled = true";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        return mapList(query.getResultList());
    }


    private List<DietEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<DietEntity> genreDetailsEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final DietEntity dietEntity = map(row);
            genreDetailsEntities.add(dietEntity);
        }

        return genreDetailsEntities;
    }

    @SneakyThrows
    private DietEntity map(final Tuple row) {
        return new DietEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("d_diet_id"), null,
                (String) row.get("d_name"), null);
    }


}
