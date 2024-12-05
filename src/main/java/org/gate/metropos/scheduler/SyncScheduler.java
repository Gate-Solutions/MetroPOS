package org.gate.metropos.scheduler;

import org.gate.metropos.services.SyncService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SyncScheduler {
    private final SyncService syncService = new SyncService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startScheduler() {
        scheduler.scheduleAtFixedRate(
                syncService::syncWithRemote,
                0,
                30,
                TimeUnit.SECONDS
        );
    }

    public void shutdownScheduler() {
        scheduler.shutdown();
    }
}
