package com.gvtech.repository.game;


import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.game.PlatformEntity;
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
public class PlatformRepository {

    @PersistenceContext
    EntityManager em;


    @SuppressWarnings("unchecked")
    @Transactional
    public List<PlatformEntity> findAll() {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, p.platform_id AS p_platform_id, p.name AS p_name FROM \"game\".platform p " +
                "JOIN \"content\".content c ON p.platform_id = c.internal_id WHERE p.enabled = true";
        final Query query = em.createNativeQuery(sql, Tuple.class);

        return mapList(query.getResultList());
    }


    private List<PlatformEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<PlatformEntity> platformEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final PlatformEntity platformEntity = map(row);
            platformEntities.add(platformEntity);
        }

        return platformEntities;
    }

    @SneakyThrows
    private PlatformEntity map(final Tuple row) {
        return new PlatformEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), (Long) row.get("p_platform_id"), (String) row.get("p_name"), null);
    }


}
