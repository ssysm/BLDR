package com.theeditorstudio.bililive.dispatcher.routes.node;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import redis.clients.jedis.Jedis;
import spark.Request;
import spark.Response;
import spark.Route;

public class NewNodeRoute implements Route {
  private final Jedis jedis = new JedisFactory().getJedis();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String nodeName = request.queryParams("node");
    jedis.publish(nodeName + ":ping", "ping");
    if(jedis.exists(nodeName + ":status")){
      long addStatus = jedis.sadd("dispatch:nodes", nodeName);
      if(addStatus == 1){
        return 1;
      }else {
        return 0;
      }
    }else{
      return -1;
    }
  }
}
