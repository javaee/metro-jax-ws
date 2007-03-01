package com.sun.xml.ws.api.pipe;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.xml.ws.api.message.Packet;

/**
 * Collection of {@link Fiber}s.
 * Owns an {@link Executor} to run them.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class Engine {
    private volatile Executor threadPool;
    public final String id;

    public Engine(String id, Executor threadPool) {
        this(id);
        this.threadPool = threadPool;
    }

    public Engine(String id) {
        this.id = id;
    }

    public void setExecutor(Executor threadPool) {
        this.threadPool = threadPool;
    }

    void addRunnable(Fiber fiber) {
        if(threadPool==null) {
            synchronized(this) {
                threadPool = Executors.newFixedThreadPool(5, new DaemonThreadFactory());
            }
        }
        threadPool.execute(fiber);
    }

    /**
     * Creates a new fiber in a suspended state.
     *
     * <p>
     * To start the returned fiber, call {@link Fiber#start(Tube,Packet,Fiber.CompletionCallback)}.
     * It will start executing the given {@link Tube} with the given {@link Packet}.
     *
     * @return new Fiber
     */
    public Fiber createFiber() {
        return new Fiber(this);
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread daemonThread = new Thread(r, "JAXWS-Engine-"+threadNumber.getAndIncrement());
            daemonThread.setDaemon(Boolean.TRUE);
            return daemonThread;
        }
    }
}
