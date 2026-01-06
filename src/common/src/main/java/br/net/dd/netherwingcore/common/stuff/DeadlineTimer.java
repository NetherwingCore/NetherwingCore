package br.net.dd.netherwingcore.common.stuff;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DeadlineTimer {

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;

    public DeadlineTimer() {
        // Create a ScheduledExecutorService with a single thread
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Schedules a task to be executed after a specified delay.
     *
     * @param delay  the timeout in milliseconds.
     * @param task   the task to be executed.
     */
    public void schedule(long delay, Runnable task) {
        // Cancel any previously scheduled task
        if (scheduledTask != null && !scheduledTask.isDone()) {
            scheduledTask.cancel(false);
        }

        // Schedule the new task with the specified delay
        scheduledTask = scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancels the timer.
     */
    public void cancel() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }

    /**
     * Shuts down the executor and releases resources.
     */
    public void shutdown() {
        scheduler.shutdown();
    }

}
