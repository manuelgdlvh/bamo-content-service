package com.gvtech.repository.recipe;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.recipe.DietEntity;
import com.gvtech.entity.recipe.TypeEntity;
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
public class TypeRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<TypeEntity> findAll(final String language) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, t.type_id AS t_type_id, t.name AS t_name " +
                "FROM \"recipe\".type t JOIN \"content\".content c ON t.type_id = c.internal_id WHERE t.language = :language AND t.enabled = true";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);

        return mapList(query.getResultList());
    }


    private List<TypeEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<TypeEntity> genreDetailsEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final TypeEntity typeEntity = map(row);
            genreDetailsEntities.add(typeEntity);
        }

        return genreDetailsEntities;
    }

    @SneakyThrows
    private TypeEntity map(final Tuple row) {
        return new TypeEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("t_type_id"), null,
                (String) row.get("t_name"), null);
    }


}
