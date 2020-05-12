package com.theeditorstudio.bililive.dispatcher.routes.work;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import spark.Request;
import spark.Response;
import spark.Route;

public class NewWorkRoute implements Route {
  private final Jedis jedis = new JedisFactory().getJedis();
  @Override
  public Object handle(Request request, Response response) throws Exception {
    String nodeName = request.queryParams("node"),
            idType = request.queryParams("id_type"),
            id = request.queryParams("id");
    JSONObject object = new JSONObject();
    object.put("type", "new");
    object.put("id_type", idType);
    object.put("node", nodeName);
    object.put("id", id);
    jedis.publish(nodeName + ":work", object.toString());
    return object;
  }
}
