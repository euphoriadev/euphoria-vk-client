package ru.euphoriadev.vk.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Igor on 10.07.15.
 * Simple executor
 */

public class ThreadExecutor {
    /**
     * Number of processor cores available
     */
    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    /** Thread executor for execute on background */
    private static final Executor singleExecutor = Executors.newSingleThreadExecutor();

    /**
     * Execute runnable with {@link Executor} on {@link LowThread}
     *
     * @param command is the code you need to execute in a background
     */
    public static void execute(Runnable command) {
        new LowThread(command).start();
//        EXECUTOR.execute(command);
    }

    /**
     * Execute runnable with {@link Executor} on single thread
     *
     * @param command the code to execute in a background
     */
    public static void executeOnSingle(Runnable command) {
        singleExecutor.execute(command);
    }
}
