package com.gvtech.support;


@FunctionalInterface
public interface GetContentTask<V> {

    V compute() throws Exception;
}
