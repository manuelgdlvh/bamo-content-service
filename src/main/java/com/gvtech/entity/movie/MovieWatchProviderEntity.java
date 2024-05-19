package com.gvtech.entity.movie;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MovieWatchProviderEntity {
    private String country;
    private WatchProviderEntity watchProviderEntity;
}
