package ru.euphoriadev.vk.util;

import java.util.concurrent.Executor;

/**
 * Created by Igor on 10.07.15.
 * Simple executor
 */

public class ThreadExecutor {
    /** Number of processor cores available */
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /** Thread executor fpr execute in new thread */
//    private static final Executor EXECUTOR = Executors.newFixedThreadPool(CPU_COUNT * 2 + 1);

    /**
     * Execute runnable with {@link Executor}
     *
     * @param command is the code you need to execute in a background
     */
    public static void execute(Runnable command) {
        new Thread(command).start();
//        EXECUTOR.execute(command);
    }
}
