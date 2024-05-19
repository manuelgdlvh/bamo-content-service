package com.gvtech.core.remove;

import com.gvtech.core.ContentId;
import com.gvtech.core.ContentType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ContentRemove {
    private final ContentId id;
    private final ContentType type;
}
