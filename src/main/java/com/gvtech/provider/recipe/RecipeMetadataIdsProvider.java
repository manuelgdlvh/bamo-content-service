package com.gvtech.provider.recipe;

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
public class RecipeMetadataIdsProvider extends AbstractCacheableContentProvider<Set<Long>> {

    // PATTERN LANGUAGE/CUISINES/DIETS/TYPES/FILTER_BY
    @Inject
    RecipeIdsProvider recipeIdsProvider;
    @Inject
    MetadataIdsProvider metadataIdsProvider;


    public RecipeMetadataIdsProvider() {
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
        if (parts.length < 5) {
            return null;
        }

        final ContentId recipeIdsContentId = new ContentId(String.format("%s/%s/%s/%s", parts[0], parts[1], parts[2], parts[3]));
        final Set<Long> movieIds = recipeIdsProvider.get(recipeIdsContentId);
        if (movieIds == null) {
            return null;
        }

        Set<MetadataIdsProvider.MetadataId> metadataIds = new HashSet<>();
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s", "RECIPE", parts[4]));
        if (!Objects.equals(parts[4], "NONE")) {
            metadataIds = metadataIdsProvider.get(metadataIdsContentId);
            if (metadataIds == null) {
                metadataIds = new HashSet<>();
            }
        }

        final ContentBuild<Set<Long>> contentBuild = new ContentBuild<>();
        final Set<Long> result = new LinkedHashSet<>();
        for (MetadataIdsProvider.MetadataId metadataId : metadataIds) {
            if (movieIds.contains(metadataId.getContentId())) {
                result.add(metadataId.getContentId());
                contentBuild.addDependency(metadataIdsContentId, metadataIdsProvider.type());
            }
        }
        result.addAll(movieIds);

        contentBuild.setContent(result);
        contentBuild.addDependency(recipeIdsContentId, recipeIdsProvider.type());
        return contentBuild;
    }


    @Override
    public ContentType type() {
        return new ContentType("RECIPE_METADATA_IDS");
    }
}
