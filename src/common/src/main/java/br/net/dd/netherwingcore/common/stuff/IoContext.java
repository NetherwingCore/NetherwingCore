package br.net.dd.netherwingcore.common.stuff;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public class IoContext {

    private final ExecutorService executor;

    public IoContext() {
        this.executor = Executors.newCachedThreadPool();
    }

    public IoContext(int concurrencyHint) {
        this.executor = Executors.newFixedThreadPool(concurrencyHint);
    }

    public void stop() {
        executor.shutdownNow();
    }

    public boolean isStopped() {
        return executor.isShutdown();
    }

    public void restart() {
        if (!executor.isShutdown()) {
            stop();
        }
    }

    public <T> Future<T> post(Callable<T> task) {
        return executor.submit(task);
    }

    public Future<?> post(Runnable task) {
        return executor.submit(task);
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public static <T> void bindExecutor(ExecutorService executor, Runnable task) {
        executor.submit(task);
    }

    public static ExecutorService getIoContext(IoContext ioContext) {
        return ioContext.getExecutor();
    }

}
