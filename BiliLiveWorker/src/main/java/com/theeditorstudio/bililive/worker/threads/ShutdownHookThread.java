package com.theeditorstudio.bililive.worker.threads;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownHookThread extends Thread {
  private final Logger logger = LoggerFactory.getLogger(BiliLiveWorker.class);

  @Override
  public synchronized void start() {
    logger.warn("Shutdown Hook Detected, shutting down pool...");
    BiliLiveWorker.shutdownPool();
  }
}
