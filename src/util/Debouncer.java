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

    public void call() {call(interval);}

    public void call(int override_interval) {
        if (current_future != null) {
            current_future.cancel(true);
        }
        current_future = sched.schedule(callback, override_interval, TimeUnit.MILLISECONDS);
    }

    public void terminate() {
        sched.shutdownNow();
    }

    public void cancel() {
        if (current_future != null) {
            current_future.cancel(true);
            current_future = null;
        }
    }
}