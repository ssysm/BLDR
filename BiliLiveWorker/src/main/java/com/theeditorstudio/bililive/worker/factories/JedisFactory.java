package com.theeditorstudio.bililive.worker.factories;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import redis.clients.jedis.Jedis;

public class JedisFactory {
  private Jedis jedis;

  public JedisFactory() {
    String host = BiliLiveWorker.getAppConfig().getString("redis.host");
    int port = BiliLiveWorker.getAppConfig().getInt("redis.port");
    String password = BiliLiveWorker.getAppConfig().getString("redis.password");

    if(!host.isEmpty() && port == 0 && password.isEmpty()) {
      jedis = new Jedis(host);
    }
    if(!host.isEmpty() && port != 0) {
      jedis = new Jedis(host, port);
      if(!password.isEmpty() ){
        jedis.auth(password);
      }
    }
  }

  public Jedis getJedis() {
    return this.jedis;
  }
}
