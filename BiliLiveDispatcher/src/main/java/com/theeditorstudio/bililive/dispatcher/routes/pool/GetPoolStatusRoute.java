package com.theeditorstudio.bililive.dispatcher.routes.pool;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Set;

public class GetPoolStatusRoute implements Route {
  private final Jedis jedis = new JedisFactory().getJedis();

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Set<String> sets = jedis.smembers("dispatch:nodes");
    JSONArray statusArray = new JSONArray();
    for(String str : sets){
      statusArray.put(new JSONObject(jedis.get(str + ":status")));
    }
    return statusArray;
  }
}
