package com.gvtech.updater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.core.ContentHandler;
import com.gvtech.core.update.ContentUpdate;
import com.gvtech.monitoring.MetricService;
import com.hazelcast.jet.cdc.ChangeRecord;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;

public abstract class AbstractContentUpdater implements ContentUpdater {

    @Inject
    ContentHandler contentHandler;
    @Inject
    MetricService metricsService;

    final ObjectMapper mapper = new ObjectMapper();


    public abstract ContentUpdate build(final ChangeRecord changeRecord, final ObjectMapper mapper) throws JsonProcessingException;


    private ContentUpdate safeBuild(final ChangeRecord changeRecord, final ObjectMapper mapper) {

        try {
            return build(changeRecord, mapper);
        } catch (Exception e) {
            Log.error(String.format("error building update for content with '%s' associated table caused by: %s", tableName(), e.getMessage()));
        }
        return null;
    }

    @Override
    public void update(final ChangeRecord changeRecord) {
        final ContentUpdate contentUpdate = safeBuild(changeRecord, this.mapper);
        if (contentUpdate == null) {
            return;
        }

        contentHandler.enqueueUpdate(contentUpdate);
        metricsService.incrementDatabaseUpdate();
    }
}
