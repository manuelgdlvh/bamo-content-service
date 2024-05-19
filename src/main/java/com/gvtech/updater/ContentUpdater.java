package com.gvtech.updater;

import com.hazelcast.jet.cdc.ChangeRecord;

public interface ContentUpdater {

    String tableName();

    void update(final ChangeRecord changeRecord);
}
