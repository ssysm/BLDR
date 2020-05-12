package com.theeditorstudio.bililive.worker.threads;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.factories.JedisFactory;
import com.theeditorstudio.bililive.worker.handlers.JedisPingSubscriber;
import redis.clients.jedis.Jedis;

public class PoolPingThread implements Runnable{
  Jedis jedis = new JedisFactory().getJedis();

  @Override
  public void run() {
    jedis.subscribe(new JedisPingSubscriber(), BiliLiveWorker.getNodeName() + ":ping");
  }
}
