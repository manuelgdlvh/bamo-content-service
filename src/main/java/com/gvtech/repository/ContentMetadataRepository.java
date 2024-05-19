package com.gvtech.repository;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.entity.ContentMetadataEntity;
import com.gvtech.provider.content.MetadataIdsProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Startup
public class ContentMetadataRepository {

    @PersistenceContext
    EntityManager em;

    final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @Transactional
    public ContentMetadataEntity findBy(final String language, final String contentType, final Long contentId) {
        final String sql = "SELECT * FROM \"match\".content_match_statistics WHERE language = :language AND content_type = :contentType AND content_id = :contentId";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("language", language);
        query.setParameter("contentType", contentType);
        query.setParameter("contentId", contentId);

        return map(query.getResultList());
    }

    @Transactional
    public ContentMetadataEntity findBy(final String contentType, final Long contentId) {
        final String sql = "SELECT * FROM \"match\".content_match_statistics WHERE content_type = :contentType AND content_id = :contentId";

        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("contentType", contentType);
        query.setParameter("contentId", contentId);


        return map(query.getResultList());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public Set<MetadataIdsProvider.MetadataId> findAllIdsBy(final String contentType, final String filterBy) {
        String sql = "SELECT %s FROM \"match\".content_match_statistics WHERE content_type = :contentType %s";
        final String sqlSelect;
        final String sqlOrderBy;

        switch (filterBy) {
            case "NUM_MATCHES_ASC":
                sqlSelect = "DISTINCT(id), matches, content_id";
                sqlOrderBy = "ORDER BY matches ASC";
                break;
            case "NUM_MATCHES_DESC":
                sqlSelect = "DISTINCT(id), matches, content_id";
                sqlOrderBy = "ORDER BY matches DESC";
                break;
            case "LAST_MATCH_ASC":
                sqlSelect = "DISTINCT(id), last_match, content_id";
                sqlOrderBy = "ORDER BY last_match ASC";
                break;
            case "LAST_MATCH_DESC":
                sqlSelect = "DISTINCT(id), last_match, content_id";
                sqlOrderBy = "ORDER BY last_match DESC";
                break;
            default:
                return null;
        }

        sql = String.format(sql, sqlSelect, sqlOrderBy);
        final Query query = em.createNativeQuery(sql, Tuple.class);
        query.setParameter("contentType", contentType);

        return mapIds(query.getResultList());
    }


    @SneakyThrows
    private Set<MetadataIdsProvider.MetadataId> mapIds(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        final Set<MetadataIdsProvider.MetadataId> ids = new LinkedHashSet<>();
        for (Tuple row : rows) {
            ids.add(new MetadataIdsProvider.MetadataId((long) row.get("id"), (long) row.get("content_id")));
        }
        return ids;
    }

    @SneakyThrows
    private ContentMetadataEntity map(final List<Tuple> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        final Tuple row = rows.getFirst();
        return new ContentMetadataEntity((Long) row.get("id"), (Long) row.get("content_id"), (String) row.get("content_type"), (Integer) row.get("matches"), (Date) row.get("last_match"));
    }


}
