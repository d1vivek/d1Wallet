package com.d1vivek.wallet.presentation.common;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

/**
 * Runs a ViewModel's blocking work on one background thread and hands the result back on the main
 * thread. Only the most recently started task can deliver a result, and nothing is delivered after
 * {@link #shutdown()}, so a slow old request can't overwrite newer state.
 *
 * <p>Each ViewModel owns one instance and must call {@link #shutdown()} from {@code onCleared()}.
 * All methods must be called on the main thread.
 */
public final class BackgroundTasks {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int latestTaskId;
    private boolean shutDown;

    /**
     * @param work runs on the background thread
     * @param fallback delivered instead of a result if {@code work} throws
     * @param onResult called on the main thread with the result
     */
    public <R> void run(Callable<R> work, R fallback, Consumer<R> onResult) {
        if (shutDown) {
            return;
        }
        int taskId = ++latestTaskId;
        try {
            executor.execute(() -> {
                R result;
                try {
                    result = work.call();
                } catch (Exception e) {
                    result = fallback;
                }
                R delivered = result;
                mainHandler.post(() -> {
                    if (!shutDown && taskId == latestTaskId) {
                        onResult.accept(delivered);
                    }
                });
            });
        } catch (RejectedExecutionException e) {
            onResult.accept(fallback);
        }
    }

    /** Stops delivery of any pending result without stopping future work. */
    public void cancelPending() {
        latestTaskId++;
    }

    public void shutdown() {
        shutDown = true;
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }
}
