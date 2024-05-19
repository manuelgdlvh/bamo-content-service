package com.gvtech.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@AllArgsConstructor
@Getter
public class ContentWithMetadataList {
    private List<ContentWithMetadata> data;
}
