package com.theeditorstudio.bililive.dispatcher.routes.work;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import spark.Request;
import spark.Response;
import spark.Route;

public class RestartWorkRoute implements Route {
  private final Jedis jedis = new JedisFactory().getJedis();
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String nodeName = request.queryParams("node"),
            id = request.queryParams("thread_id");
    JSONObject object = new JSONObject();
    object.put("thread_id", id);
    object.put("type", "restart");
    object.put("node", nodeName);
    jedis.publish(nodeName + ":work", object.toString());
    return object;
  }
}
