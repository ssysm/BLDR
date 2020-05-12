package com.theeditorstudio.bililive.dispatcher.routes.node;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import spark.Request;
import spark.Response;
import spark.Route;

public class DeleteNodeRoute implements Route {
  private final Jedis jedis = new JedisFactory().getJedis();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String nodeName = request.queryParams("node");
    JSONObject object = new JSONObject();
    object.put("type", "shutdown");
    jedis.publish(nodeName + ":work", object.toString());
    return jedis.srem("dispatch:nodes", nodeName);
  }
}
