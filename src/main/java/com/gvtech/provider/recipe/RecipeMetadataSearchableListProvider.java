package com.gvtech.provider.recipe;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import com.gvtech.provider.content.ContentSearchableIdsProvider;
import com.gvtech.support.AbstractContentListProvider;
import com.gvtech.support.ContentBucket;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;


@ApplicationScoped
@Startup
public class RecipeMetadataSearchableListProvider extends AbstractContentListProvider implements ContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/KEYWORD/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    ContentSearchableIdsProvider contentSearchableIdsProvider;
    @Inject
    RecipeMetadataProvider recipeMetadataProvider;


    @Override
    public ContentWithMetadataList get(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 3) {
            return null;
        }

        final String language = parts[0];
        final String keyword = parts[1];
        final int pageNumber = Integer.parseInt(parts[2]);
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s", language, "RECIPE", keyword));
        final List<Long> contentIds = this.contentSearchableIdsProvider.get(metadataIdsContentId);
        if (contentIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= contentIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > contentIds.size()) {
            toIndex = contentIds.size();
        }

        final List<Long> recipeMetadataIdsPaged = new ArrayList<>(contentIds).subList(fromIndex, toIndex);

        final ContentBucket<ContentWithMetadata> recipeBucket = this.emptyBucket();
        for (Long recipeId : recipeMetadataIdsPaged) {
            recipeBucket.add(() -> findContentMetadata(recipeId, language));
        }

        final List<ContentWithMetadata> recipesWithMetadata = recipeBucket.get();
        if (recipesWithMetadata.isEmpty()) {
            return null;
        }

        return new ContentWithMetadataList(recipesWithMetadata);
    }

    private ContentWithMetadata findContentMetadata(final Long id, final String language) {
        final ContentId recipeMetadataContentId = new ContentId(String.format("%s/%s", language, id));
        final ContentWithMetadata withMetadata = recipeMetadataProvider.get(recipeMetadataContentId);
        if (withMetadata == null) {
            return null;
        }
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("RECIPE_METADATA_SEARCHABLE_LIST");
    }
}
