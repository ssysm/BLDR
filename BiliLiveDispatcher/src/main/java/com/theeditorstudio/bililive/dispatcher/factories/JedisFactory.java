package com.theeditorstudio.bililive.dispatcher.factories;

import com.theeditorstudio.bililive.dispatcher.BiliLiveDispatcher;
import redis.clients.jedis.Jedis;

public class JedisFactory {
  private Jedis jedis;

  public JedisFactory() {
    String host = BiliLiveDispatcher.getAppConfig().getString("redis.host");
    int port = BiliLiveDispatcher.getAppConfig().getInt("redis.port");
    String password = BiliLiveDispatcher.getAppConfig().getString("redis.password");

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
