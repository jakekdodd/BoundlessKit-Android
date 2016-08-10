package com.usedopamine.dopaminekit.Synchronization;

/**
 * Created by cuddergambino on 8/4/16.
 */

public class SyncCoordinator {

    private static SyncCoordinator sharedInstance = new SyncCoordinator();

    private SyncCoordinator() {}

    public static SyncCoordinator getInstance() { return sharedInstance; }

    static void sync() {

    }
}
