package com.theeditorstudio.bililive.worker.types;

import com.theeditorstudio.bililive.worker.threads.RecorderThread;

import java.util.concurrent.Future;

public class FutureThreadWrapper {
  private final Future<?> future;
  private final RecorderThread runnable;

  public FutureThreadWrapper(Future<?> future, RecorderThread runnable) {
    this.future = future;
    this.runnable = runnable;
  }

  public Future<?> getFuture() {
    return future;
  }

  public RecorderThread getRunnable() {
    return runnable;
  }
}
