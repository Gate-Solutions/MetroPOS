package org.gate.metropos.scheduler;

import io.github.cdimascio.dotenv.Dotenv;
import org.gate.metropos.services.SyncService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SyncScheduler {
    private final SyncService syncService = new SyncService();
    private static Dotenv dotenv;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void startScheduler() {
        dotenv = Dotenv.load();
        int delay_period = 90;
        if(dotenv.get("SYNC_DELAY") != null) {
            try {
                delay_period = Integer.parseInt(dotenv.get("SYNC_DELAY"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        scheduler.scheduleAtFixedRate(
                syncService::syncWithRemote,
                0,
                delay_period,
                TimeUnit.SECONDS
        );
    }

    public void shutdownScheduler() {
        scheduler.shutdown();
    }
}
