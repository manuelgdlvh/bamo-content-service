package com.gvtech.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.JetService;
import com.hazelcast.jet.Observable;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.cdc.postgres.PostgresCdcSources;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.config.ProcessingGuarantee;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Startup
public class ContentChangesListener {

    @Inject
    ContentChangeProcessor contentChangeProcessor;

    @Inject
    HazelcastInstance hazelcastInstance;

    @PostConstruct
    private void init() {
        final JetService jet = hazelcastInstance.getJet();
        final StreamSource<ChangeRecord> source = PostgresCdcSources.postgres("source")
                .setLogicalDecodingPlugIn("pgoutput")
                .setDatabaseAddress("host.docker.internal")
                .setDatabasePort(5432)
                .setDatabaseUser("postgres")
                .setDatabasePassword("_01Ba66mo33db_")
                .setDatabaseName("postgres")
                .setTableWhitelist("match.content_match_statistics")
                .build();

        final Observable<ChangeRecord> observable = jet.newObservable();
        observable.addObserver(contentChangeProcessor);

        final Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source)
                .withoutTimestamps()
                .peek()
                .writeTo(Sinks.observable(observable));

        final JobConfig cfg = new JobConfig().setProcessingGuarantee(ProcessingGuarantee.EXACTLY_ONCE).setName("postgres-monitor");
        jet.newJob(pipeline, cfg);
    }
}
