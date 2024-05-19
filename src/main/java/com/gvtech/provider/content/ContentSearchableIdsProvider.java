package com.gvtech.provider.content;


import com.gvtech.client.ContentSearchClient;
import com.gvtech.client.request.Request;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
@Startup
public class ContentSearchableIdsProvider implements ContentProvider<List<Long>> {


    // PATTERN LANGUAGE/CONTENT_TYPE/KEYWORDS

    @RestClient
    ContentSearchClient client;

    @Override
    public List<Long> get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String type = parts[1];
        final String keyword = parts[2];

        final List<Long> result = client.search(new Request(type, keyword), language);

        return result.isEmpty() ? null : result;
    }

    @Override
    public ContentType type() {
        return new ContentType("CONTENT_SEARCHABLE_BY_IDS");
    }
}
