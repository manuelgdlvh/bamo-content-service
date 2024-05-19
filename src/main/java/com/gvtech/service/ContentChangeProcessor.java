package com.gvtech.service;

import com.gvtech.updater.ContentUpdater;
import com.hazelcast.jet.cdc.ChangeRecord;
import com.hazelcast.jet.function.Observer;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import java.util.*;

@ApplicationScoped
@Startup
public class ContentChangeProcessor implements Observer<ChangeRecord> {


    final Map<String, List<ContentUpdater>> updaters;

    public ContentChangeProcessor(Instance<ContentUpdater> updaters) {
        final Map<String, List<ContentUpdater>> updaterMap = new HashMap<>();

        for (ContentUpdater updater : updaters) {
            if (!updaterMap.containsKey(updater.tableName())) {
                updaterMap.put(updater.tableName(), new ArrayList<>());
            }
            updaterMap.get(updater.tableName()).add(updater);
        }

        this.updaters = Collections.unmodifiableMap(updaterMap);
    }


    @Override
    public void onNext(ChangeRecord changeRecord) {
        final List<ContentUpdater> updaterList = this.updaters.get(String.format("%s.%s", changeRecord.schema(), changeRecord.table()));
        if (updaterList == null) {
            return;
        }
        for (ContentUpdater contentUpdater : updaterList) {
            contentUpdater.update(changeRecord);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        Observer.super.onError(throwable);
    }

    @Override
    public void onComplete() {
        Observer.super.onComplete();
    }


}
