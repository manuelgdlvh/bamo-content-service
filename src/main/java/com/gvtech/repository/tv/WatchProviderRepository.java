package com.gvtech.repository.tv;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.entity.ContentEntity;
import com.gvtech.entity.tv.WatchProviderEntity;
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
public class WatchProviderRepository {

    final ObjectMapper mapper = new ObjectMapper();
    @PersistenceContext
    EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional
    public List<WatchProviderEntity> findAllEnabledSortByDisplayOrderAsc(final String country) {
        final String sql = "SELECT DISTINCT(c.external_code) as c_external_code, c.provider_code AS c_provider_code, wp.watch_provider_id AS wp_watch_provider_id, wp.name AS wp_name, wp.display_order AS wp_display_order " +
                "FROM \"tv\".watch_provider wp JOIN \"content\".content c ON wp.watch_provider_id = c.internal_id JOIN \"tv\".tv_watch_provider mwp ON mwp.watch_provider_id = wp.watch_provider_id" +
                " WHERE mwp.country = :country AND c.category_code = 'TV_WATCH_PROVIDER' AND wp.enabled = true ORDER BY wp.display_order ASC";
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("country", country);

        return mapList(query.getResultList());
    }


    private List<WatchProviderEntity> mapList(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        final List<WatchProviderEntity> watchProviderEntities = new ArrayList<>();
        for (Tuple row : rows) {
            final WatchProviderEntity watchProviderEntity = map(row);
            watchProviderEntities.add(watchProviderEntity);
        }

        return watchProviderEntities;
    }

    @SneakyThrows
    private WatchProviderEntity map(final Tuple row) {
        return new WatchProviderEntity(new ContentEntity((String) row.get("c_external_code"), (String) row.get("c_provider_code")), null, (Long) row.get("wp_watch_provider_id"), (String) row.get("wp_name"),
                null, null);
    }


}
