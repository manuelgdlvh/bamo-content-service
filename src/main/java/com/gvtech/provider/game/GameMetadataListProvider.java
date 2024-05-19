package com.gvtech.provider.game;

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
import java.util.function.Predicate;


@ApplicationScoped
@Startup
public class GameMetadataListProvider extends AbstractCacheableContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/PLATFORMS/GENRES/YEARS/GAME_MODES/FILTER_BY/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    GameMetadataIdsProvider gameMetadataIdsProvider;
    @Inject
    GameMetadataProvider gameMetadataProvider;

    public GameMetadataListProvider() {
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
        if (parts.length < 7) {
            return null;
        }

        final String language = parts[0];
        final String platforms = parts[1];
        final String genres = parts[2];
        final String years = parts[3];
        final String gameModes = parts[4];
        final String filterBy = parts[5];
        final int pageNumber = Integer.parseInt(parts[6]);

        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s/%s/%s/%s", language, platforms, genres, years, gameModes, filterBy));
        final Set<Long> gameMetadataIds = this.gameMetadataIdsProvider.get(metadataIdsContentId);
        if (gameMetadataIds == null) {
            return null;
        }

        // PAGINATION
        int fromIndex = pageNumber * PAGE_SIZE;
        if (fromIndex >= gameMetadataIds.size()) {
            return null;
        }

        int toIndex = fromIndex + PAGE_SIZE;
        if (toIndex > gameMetadataIds.size()) {
            toIndex = gameMetadataIds.size();
        }

        final ContentBuild<ContentWithMetadataList> contentBuild = new ContentBuild<>();
        final List<Long> metadataIdsPaged = new ArrayList<>(gameMetadataIds).subList(fromIndex, toIndex);

        final List<ContentWithMetadata> gamesWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    metadataIdsPaged.stream().map(gameId -> scope.fork(() -> findContentMetadataAndRegisterDependency(gameId, language, contentBuild))).toList();
            scope.join();
            gamesWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (gamesWithMetadata.isEmpty()) {
            return null;
        }

        contentBuild.addDependency(metadataIdsContentId, gameMetadataIdsProvider.type());
        contentBuild.setContent(new ContentWithMetadataList(gamesWithMetadata));

        return contentBuild;
    }

    private ContentWithMetadata findContentMetadataAndRegisterDependency(final Long gameId, final String language,
                                                                         final ContentBuild<ContentWithMetadataList> contentBuild) {
        final ContentId gameMetadataContentId = new ContentId(String.format("%s/%s", language, gameId));
        final ContentWithMetadata withMetadata = gameMetadataProvider.get(gameMetadataContentId);
        if (withMetadata == null) {
            return null;
        }

        contentBuild.addDependency(gameMetadataContentId, gameMetadataProvider.type());
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("GAME_METADATA_LIST");
    }
}
