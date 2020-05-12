package com.theeditorstudio.bililive.worker.handlers;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.factories.JedisFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class JedisPingSubscriber extends JedisPubSub {

  private final Jedis jedis = new JedisFactory().getJedis();
  private static final Logger logger = LoggerFactory.getLogger(BiliLiveWorker.class);

  @Override
  public void onMessage(String channel, String message) {
    logger.debug("Got Ping Request from redis.");
    jedis.publish(BiliLiveWorker.getNodeName()+":pong", "pong");
  }
}
