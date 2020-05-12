package com.theeditorstudio.bililive.worker.threads;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.handlers.JedisWorkSubscriber;
import com.theeditorstudio.bililive.worker.factories.JedisFactory;
import redis.clients.jedis.Jedis;

public class PoolSubscriberThread implements Runnable {

  Jedis jedis = new JedisFactory().getJedis();

  @Override
  public void run() {
    jedis.subscribe(new JedisWorkSubscriber(), BiliLiveWorker.getNodeName() + ":work");
  }

}
