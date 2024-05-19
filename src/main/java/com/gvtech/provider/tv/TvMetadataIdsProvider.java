package com.gvtech.provider.tv;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.provider.content.MetadataIdsProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.*;


@ApplicationScoped
@Startup
public class TvMetadataIdsProvider extends AbstractCacheableContentProvider<Set<Long>> {

    // PATTERN LANGUAGE/COUNTRY/PLATFORMS/GENRES/YEARS/FILTER_BY
    @Inject
    TvIdsProvider tvIdsProvider;
    @Inject
    MetadataIdsProvider metadataIdsProvider;


    // TODO
    public TvMetadataIdsProvider() {
        super((long) (1000 * 90));
    }

    @Override
    public boolean equals(Set<Long> o1, Set<Long> o2) {
        if (o1.size() != o2.size()) {
            return false;
        }

        final Iterator<Long> o1Iterator = o1.iterator();
        final Iterator<Long> o2Iterator = o2.iterator();
        while (o1Iterator.hasNext()) {
            if (!o1Iterator.next().equals(o2Iterator.next())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ContentBuild<Set<Long>> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 6) {
            return null;
        }

        final String platforms = parts[2];

        String exclusiveCountry = "TRUE";
        if (platforms.equals("NONE")) {
            exclusiveCountry = "FALSE";
        }

        final ContentId tvIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", parts[0], parts[1], exclusiveCountry, platforms, parts[3], parts[4]));
        final Set<Long> tvIds = tvIdsProvider.get(tvIdsContentId);
        if (tvIds == null) {
            return null;
        }

        Set<MetadataIdsProvider.MetadataId> metadataIds = new HashSet<>();
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s", "TV", parts[5]));
        if (!Objects.equals(parts[5], "NONE")) {
            metadataIds = metadataIdsProvider.get(metadataIdsContentId);
            if (metadataIds == null) {
                metadataIds = new HashSet<>();
            }
        }

        final ContentBuild<Set<Long>> contentBuild = new ContentBuild<>();
        final Set<Long> result = new LinkedHashSet<>();
        for (MetadataIdsProvider.MetadataId metadataId : metadataIds) {
            if (tvIds.contains(metadataId.getContentId())) {
                result.add(metadataId.getContentId());
                contentBuild.addDependency(metadataIdsContentId, metadataIdsProvider.type());
            }
        }
        result.addAll(tvIds);

        contentBuild.setContent(result);
        contentBuild.addDependency(tvIdsContentId, tvIdsProvider.type());
        return contentBuild;
    }


    @Override
    public ContentType type() {
        return new ContentType("TV_METADATA_IDS");
    }
}
