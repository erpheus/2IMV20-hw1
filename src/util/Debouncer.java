package util;

import java.util.concurrent.*;

public class Debouncer {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final Runnable callback;
    private final int interval;
    private ScheduledFuture<?> current_future = null;

    public Debouncer(Runnable c, int interval) {
        this.callback = c;
        this.interval = interval;
    }

    public void call() {
        if (current_future != null) {
            current_future.cancel(false);
        }
        current_future = sched.schedule(callback, interval, TimeUnit.MILLISECONDS);
    }

    public void terminate() {
        sched.shutdownNow();
    }

    public void cancel() {
        if (current_future != null) {
            current_future.cancel(false);
            current_future = null;
        }
    }
}