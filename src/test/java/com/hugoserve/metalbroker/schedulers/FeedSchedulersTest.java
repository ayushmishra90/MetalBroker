package com.hugoserve.metalbroker.schedulers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test class is used to manually trigger the full data synchronization once.
 * It loads the entire Spring application context to allow autowiring of services.
 */
@SpringBootTest // <-- IMPORTANT: Loads the full Spring context
class FeedSchedulersTest {

    // Autowire the class containing the @Scheduled method
    @Autowired
    private FeedSchedulers feedSchedulers;

    @Test
    void DailyHistorySync() {
        System.out.println("--- Starting manual data sync from JUnit Test ---");

        // Call the exact method you want to run
        feedSchedulers.dailyHistorySync();

        System.out.println("--- Manual sync complete ---");
    }

    @Test
    void cleanupIntraday(){
        System.out.println("--- checking cleaing of 7days data--");

        feedSchedulers.cleanupIntraday();

        System.out.println("--checking complete----");
    }

    @Test
    void syncEvery5Minutes(){
        System.out.println("--- checking syncEvery5Minutes--");

        feedSchedulers.syncEvery5Minutes();

        System.out.println("--checking complete----");
    }
}
