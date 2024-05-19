package com.gvtech.provider.tv;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.tv.Tv;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class TvListByIdsProvider implements ContentProvider<List<Tv>> {

    // PATTERN LANGUAGE/COUNTRY/IDS
    @Inject
    TvProvider tvProvider;


    @Override
    public List<Tv> get(ContentId contentId) {
        final String[] parts = contentId.getId().split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final List<Long> ids = new ArrayList<>();

        for (String id : parts[2].split(",")) {
            ids.add(Long.valueOf(id));
        }

        final List<Tv> tvs;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<Tv>> subtasks =
                    ids.stream().map(tvId -> scope.fork(() -> tvProvider.get(new ContentId(String.format("%s/%s/%s", language, country, tvId))))).toList();
            scope.join();
            tvs = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        return tvs;
    }

    @Override
    public ContentType type() {
        return new ContentType("TV_LIST_BY_IDS");
    }

}
