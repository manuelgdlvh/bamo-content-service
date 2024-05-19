package com.gvtech.provider.game;

import com.gvtech.core.AbstractCacheableContentProvider;
import com.gvtech.core.ContentBuild;
import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import com.gvtech.model.ContentWithMetadata;
import com.gvtech.model.Metadata;
import com.gvtech.model.game.Game;
import com.gvtech.provider.content.MetadataProvider;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


@ApplicationScoped
@Startup
public class GameMetadataProvider extends AbstractCacheableContentProvider<ContentWithMetadata> {

    // PATTERN LANGUAGE/ID
    @Inject
    MetadataProvider metadataProvider;
    @Inject
    GameProvider gameProvider;

    public GameMetadataProvider() {
        super((long) (1000 * 60 * 60 * 24));
    }

    @Override
    public boolean equals(ContentWithMetadata o1, ContentWithMetadata o2) {
        return o1.equals(o2);
    }

    @Override
    public ContentBuild<ContentWithMetadata> build(ContentId contentId) {
        final String id = contentId.getId();
        final String[] parts = id.split("/");
        if (parts.length < 2) {
            return null;
        }

        final Game game = gameProvider.get(contentId);
        if (game == null) {
            return null;
        }

        final ContentBuild<ContentWithMetadata> contentBuild = new ContentBuild<>();
        final ContentId metadataContentId = new ContentId(String.format("%s/%s", "GAME", game.id()));
        Metadata metadata = metadataProvider.get(metadataContentId);

        contentBuild.setContent(ContentWithMetadata.buildGame(game, metadata));

        contentBuild.addDependency(metadataContentId, metadataProvider.type());
        contentBuild.addDependency(contentId, gameProvider.type());

        return contentBuild;
    }


    @Override
    public ContentType type() {
        return new ContentType("GAME_METADATA");
    }
}
