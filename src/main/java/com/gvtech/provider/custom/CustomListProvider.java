package com.gvtech.provider.custom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.custom.Custom;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Startup
public class CustomListProvider implements ContentProvider<List<Custom>> {
    final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public List<Custom> get(ContentId contentId) {

        final Map<String, Object> properties = this.mapper.readValue(contentId.getId(), new TypeReference<>() {
        });
        final List<Map<String, Object>> details = (List<Map<String, Object>>) properties.get("content");


        final List<Custom> content = new ArrayList<>();
        long i = 0;
        for (Map<String, Object> detail : details) {
            final Map<String, String> result = new HashMap<>();
            for (var entry : detail.entrySet()) {
                result.put(entry.getKey(), entry.getValue().toString());
            }

            content.add(new Custom(i, result));
            i++;
        }

        return content;
    }

    @Override
    public ContentType type() {
        return new ContentType("CUSTOM_LIST");
    }
}
