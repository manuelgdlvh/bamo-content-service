package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.entity.tv.TvEntity;
import com.gvtech.model.tv.Tv;
import com.gvtech.repository.tv.TvRepository;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class TvProvider extends AbstractCacheableContentProvider<Tv> {

    // PATTERN LANGUAGE/COUNTRY/ID
    @Inject
    TvRepository tvRepository;

    public TvProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(Tv o1, Tv o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<Tv> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String country = parts[1];
        final Long tvId = Long.valueOf(parts[2]);

        final TvEntity tvEntity = tvRepository.findBy(language, tvId);

        if (tvEntity == null) {
            return null;
        }

        return new ContentBuild<>(Tv.map(tvEntity, country));
    }


    @Override
    public ContentType type() {
        return new ContentType("TV");
    }
}
