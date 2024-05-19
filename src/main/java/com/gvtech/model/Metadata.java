package com.gvtech.model;


import com.gvtech.entity.ContentMetadataEntity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class Metadata {
    private Long id;
    private Long contentId;
    private Integer matches;
    private Date lastMatch;


    public static Metadata map(final ContentMetadataEntity contentMetadata) {
        return new Metadata(contentMetadata.getId(), contentMetadata.getContentId(), contentMetadata.getMatches(), contentMetadata.getLastMatch());
    }
}
