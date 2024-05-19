package com.gvtech.model.custom;


import com.gvtech.model.Content;

import java.util.Map;

public record Custom(Long id, Map<String, String> details) implements Content {

    @Override
    public String getContentType() {
        return "CUSTOM";
    }
}