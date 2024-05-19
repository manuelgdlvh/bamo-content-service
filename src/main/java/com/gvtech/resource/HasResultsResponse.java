package com.gvtech.resource;

public record HasResultsResponse(Response data) {
    public record Response(boolean status) {
    }
}
