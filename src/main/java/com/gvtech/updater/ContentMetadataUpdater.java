package com.gvtech.updater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.core.update.ContentUpdate;
import com.hazelcast.jet.cdc.ChangeRecord;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@ApplicationScoped
@Startup
public class ContentMetadataUpdater extends AbstractContentUpdater {


    @Override
    public String tableName() {
        return "match.content_match_statistics";
    }

    @Override
    public ContentUpdate build(ChangeRecord changeRecord, ObjectMapper mapper) throws JsonProcessingException {
        final Key key = mapper.readValue(Objects.requireNonNull(changeRecord.value()).toJson(), Key.class);
        return new ContentUpdate(new ContentId(String.format("%s/%s", key.getContent_type(), key.getContent_id())), new ContentType("METADATA"));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    private static class Key {
        private Long id;
        private Long content_id;
        private String content_type;
        private Integer matches;
        private Date last_match;
    }
}
