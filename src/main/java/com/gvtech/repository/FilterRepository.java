package com.gvtech.repository;


import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
@Startup
public class FilterRepository {

    @PersistenceContext
    EntityManager em;
}
