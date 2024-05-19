package com.gvtech.core;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ContentDependant {
    private final ContentId id;
    private final ContentType type;
}
