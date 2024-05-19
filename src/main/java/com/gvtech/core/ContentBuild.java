package com.gvtech.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
public class ContentBuild<T> {

    @Setter
    private T content;
    private Set<ContentDependency> dependencies;

    public ContentBuild(final T content) {
        this.content = content;
        this.dependencies = new HashSet<>();
    }

    public ContentBuild() {
        this.dependencies = new HashSet<>();
    }

    public void addDependency(final ContentId id, final ContentType type) {
        dependencies.add(new ContentDependency(id, type));
    }
}
