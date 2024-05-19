package com.gvtech.provider.game;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentProvider;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.ContentWithMetadataList;
import com.gvtech.provider.content.ContentSearchableIdsProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.StructuredTaskScope;


@ApplicationScoped
@Startup
public class GameMetadataSearchableListProvider implements ContentProvider<ContentWithMetadataList> {
    // PATTERN LANGUAGE/KEYWORD/PAGE

    private static final int PAGE_SIZE = 15;
    @Inject
    ContentSearchableIdsProvider contentSearchableIdsProvider;
    @Inject
    GameMetadataProvider gameMetadataProvider;


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
        final ContentId metadataIdsContentId = new ContentId(String.format("%s/%s/%s", language, "GAME", keyword));
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

        final List<Long> gameMetadataIdsPaged = new ArrayList<>(contentIds).subList(fromIndex, toIndex);
        final List<ContentWithMetadata> gamesWithMetadata;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            final List<StructuredTaskScope.Subtask<ContentWithMetadata>> subtasks =
                    gameMetadataIdsPaged.stream().map(gameId -> scope.fork(() -> findContentMetadata(gameId, language))).toList();
            scope.join();
            gamesWithMetadata = subtasks.stream().map(StructuredTaskScope.Subtask::get).filter(Objects::nonNull).toList();

        } catch (InterruptedException e) {
            return null;
        }

        if (gamesWithMetadata.isEmpty()) {
            return null;
        }

        return new ContentWithMetadataList(gamesWithMetadata);
    }

    private ContentWithMetadata findContentMetadata(final Long id, final String language) {
        final ContentId gameMetadataContentId = new ContentId(String.format("%s/%s", language, id));
        final ContentWithMetadata withMetadata = gameMetadataProvider.get(gameMetadataContentId);
        if (withMetadata == null) {
            return null;
        }
        return withMetadata;
    }


    @Override
    public ContentType type() {
        return new ContentType("GAME_METADATA_SEARCHABLE_LIST");
    }
}
