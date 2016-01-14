package ru.euphoriadev.vk.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Igor on 10.07.15.
 * Simple executor
 */

public class ThreadExecutor {
    /** Thread pool Executor with 2 fixed threads */
    private static final Executor EXECUTOR = Executors.newFixedThreadPool(2);

    /**
     * Execute runnable with {@link Executor}
     *
     * @param command is the code you need to execute in a background
     */
    public static void execute(Runnable command) {
//        new Thread(command).start();
        EXECUTOR.execute(command);
    }
}
