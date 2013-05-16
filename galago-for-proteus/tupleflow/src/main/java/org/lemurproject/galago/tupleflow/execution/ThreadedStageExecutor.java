// BSD License (http://lemurproject.org/galago-license)
package org.lemurproject.galago.tupleflow.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.lemurproject.galago.tupleflow.ExNihiloSource;

/**
 *
 * @author trevor
 */
public class ThreadedStageExecutor implements StageExecutor {

  public static class InstanceRunnable implements Runnable {

    StageInstanceDescription description;
    Exception exception;
    boolean isRunning;
    boolean isQueued;
    NetworkedCounterManager counterManager;
    CountDownLatch latch;

    public InstanceRunnable(StageInstanceDescription description,
            NetworkedCounterManager manager,
            CountDownLatch latch) {
      this.isRunning = false;
      this.isQueued = true;
      this.description = description;
      this.exception = null;
      this.counterManager = manager;
      this.latch = latch;
    }

    public synchronized Exception getException() {
      return exception;
    }

    public synchronized boolean isQueued() {
      return isQueued;
    }

    public synchronized boolean isRunning() {
      return isRunning;
    }

    public synchronized boolean isDone() {
      return !isQueued && !isRunning;
    }

    synchronized void setException(Exception e) {
      this.exception = e;
    }

    synchronized void setIsRunning(boolean isRunning) {
      this.isRunning = isRunning;
    }

    synchronized void setIsQueued(boolean isQueued) {
      this.isQueued = isQueued;
    }

    public void run() {
      try {
        setIsQueued(false);
        setIsRunning(true);
        StageInstanceFactory factory = new StageInstanceFactory(counterManager);
        ExNihiloSource source = factory.instantiate(description);
        source.run();
      } catch (Exception e) {
        setException(e);
      } finally {
        latch.countDown();
        setIsRunning(false);
      }
    }
  }

  public class ThreadedStageContext implements StageExecutionStatus, Runnable {

    StageGroupDescription stage;
    String temporaryDirectory;
    boolean done = false;
    ArrayList<InstanceRunnable> runnables = new ArrayList();
    List<StageInstanceDescription> instances;
    CountDownLatch latch;
    NetworkedCounterManager counterManager;

    ThreadedStageContext(StageGroupDescription stage, String temporaryDirectory) {
      this.stage = stage;
      this.counterManager = new NetworkedCounterManager();
      this.temporaryDirectory = temporaryDirectory;
      this.instances = stage.getInstances();
      this.latch = new CountDownLatch(instances.size());
      counterManager.start();

      for (StageInstanceDescription instance : instances) {
        InstanceRunnable runnable = new InstanceRunnable(instance, counterManager, latch);
        runnables.add(runnable);
      }
    }

    public synchronized boolean isDone() {
      return done;
    }

    public void run() {
      for (InstanceRunnable instance : runnables) {
        threadPool.execute(instance);
      }

      while (latch.getCount() > 0) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          // do nothing
        }
      }

      synchronized (this) {
        counterManager.stop();
        done = true;
      }
    }

    public String getName() {
      return stage.getName();
    }

    public int getBlockedInstances() {
      return 0;
    }

    public synchronized int getQueuedInstances() {
      int queuedInstances = 0;

      for (InstanceRunnable instance : runnables) {
        if (instance.isQueued()) {
          queuedInstances++;
        }
      }

      return queuedInstances;
    }

    public int getRunningInstances() {
      int runningInstances = 0;

      for (InstanceRunnable instance : runnables) {
        if (instance.isRunning()) {
          runningInstances++;
        }
      }

      return runningInstances;
    }

    public int getCompletedInstances() {
      int completedInstances = 0;

      for (InstanceRunnable instance : runnables) {
        if (instance.isDone()) {
          completedInstances++;
        }
      }

      return completedInstances;
    }

    public synchronized List<Double> getRunTimes() {
      ArrayList<Double> times = new ArrayList();
      // do something
      return times;
    }

    public synchronized List<Exception> getExceptions() {
      ArrayList<Exception> exceptions = new ArrayList();

      for (InstanceRunnable instance : runnables) {
        Exception e = instance.getException();
        if (e != null) {
          exceptions.add(e);
        }
      }

      return exceptions;
    }
  }

  public ThreadedStageExecutor() {
    threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  public ThreadedStageContext execute(StageGroupDescription stage, String temporary) {
    ThreadedStageContext result = new ThreadedStageContext(stage, temporary);
    new Thread(result).start();
    return result;
  }

  public void shutdown() {
    threadPool.shutdown();
  }
  ExecutorService threadPool;
}
