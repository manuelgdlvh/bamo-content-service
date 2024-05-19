package com.gvtech.provider.recipe;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class RecipeMetadataListProvider extends AbstractCacheableContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/CUSINES/DIETS/TYPES/FILTER_BY/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    RecipeMetadataIdsProvider recipeMetadataIdsProvider;
    @Inject
    RecipeMetadataProvider recipeMetadataProvider;

    public RecipeMetadataListProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(ContentWithMetadataList o1, ContentWithMetadataList o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<ContentWithMetadataList> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 6) {
            return null;
        }

        final String language = parts[0];
        final String cuisines = parts[1];
        final String diets = parts[2];
        final String types = parts[3];
        final String filterBy = parts[4];
        final int pageNumber = Integer.parseInt(parts[5]);

        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s", language, cuisines, diets, types, filterBy));
        final Set<Long> recipeMetadataIds = this.recipeMetadataIdsProvider.get(metadataIdsContentId);
        if (recipeMetadataIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= recipeMetadataIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > recipeMetadataIds.size()) {
            toIndex = recipeMetadataIds.size();
        }

        final ContentBuild<ContentWithMetadataList> contentBuild = new ContentBuild<>();
        final List<Long> metadataIdsPaged = new ArrayList<>(recipeMetadataIds).subList(fromIndex, toIndex);

        final List<ContentWithMetadata> recipesWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    metadataIdsPaged.stream().map(recipeId -> scope.fork(() -> findContentMetadataAndRegisterDependency(recipeId, language, contentBuild))).toList();
            scope.join();
            recipesWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (recipesWithMetadata.isEmpty()) {
            return null;
        }

        contentBuild.addDependency(metadataIdsContentId, recipeMetadataIdsProvider.type());
        contentBuild.setContent(new ContentWithMetadataList(recipesWithMetadata));

        return contentBuild;
    }

    private ContentWithMetadata findContentMetadataAndRegisterDependency(final Long recipeId, final String language,
                                                                         final ContentBuild<ContentWithMetadataList> contentBuild) {
        final ContentId recipeMetadataContentId = new ContentId(String.format("%s/%s", language, recipeId));
        final ContentWithMetadata withMetadata = recipeMetadataProvider.get(recipeMetadataContentId);
        if (withMetadata == null) {
            return null;
        }

        contentBuild.addDependency(recipeMetadataContentId, recipeMetadataProvider.type());
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("RECIPE_METADATA_LIST");
    }
}
