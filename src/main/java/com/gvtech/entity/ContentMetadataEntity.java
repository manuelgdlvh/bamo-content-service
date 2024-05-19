package com.gvtech.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;


@AllArgsConstructor
@Getter
public class ContentMetadataEntity {

    private Long id;
    private Long contentId;
    private String contentType;
    private Integer matches;
    private Date lastMatch;
}
