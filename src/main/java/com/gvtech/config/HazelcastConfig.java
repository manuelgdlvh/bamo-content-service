package com.gvtech.config;


import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;


@ApplicationScoped
@Startup
public class HazelcastConfig {

    @Singleton
    HazelcastInstance createInstance() {
        return Hazelcast.bootstrappedInstance();
    }


}
