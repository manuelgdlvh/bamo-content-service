package com.gvtech.entity.tv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TvWatchProviderEntity {
    private String country;
    private WatchProviderEntity watchProviderEntity;
}
