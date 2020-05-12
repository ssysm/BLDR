package com.theeditorstudio.bililive.dispatcher;

import com.theeditorstudio.bililive.dispatcher.factories.JedisFactory;
import com.theeditorstudio.bililive.dispatcher.routes.node.DeleteNodeRoute;
import com.theeditorstudio.bililive.dispatcher.routes.node.NewNodeRoute;
import com.theeditorstudio.bililive.dispatcher.routes.pool.GetPoolStatusRoute;
import com.theeditorstudio.bililive.dispatcher.routes.pool.ShutdownPoolRoute;
import com.theeditorstudio.bililive.dispatcher.routes.work.DeleteWorkRoute;
import com.theeditorstudio.bililive.dispatcher.routes.work.NewWorkRoute;
import com.theeditorstudio.bililive.dispatcher.routes.work.RestartWorkRoute;
import com.theeditorstudio.bililive.dispatcher.routes.work.StopWorkRoute;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import spark.Spark;

import java.io.File;

public class BiliLiveDispatcher {

  private static Config appConfig = null;
  private static final Logger logger = LoggerFactory.getLogger(BiliLiveDispatcher.class);

  public static void main(String[] args) throws InterruptedException {
    if(args.length == 1){
      logger.info("Passed In Config file path:" + args[0]);
      appConfig = ConfigFactory.parseFile(new File(args[0]));
    }else{
      logger.info("Using Default config...");
      appConfig = ConfigFactory.load("app.conf");
    }

    // set jedis
    Jedis jedis = new JedisFactory().getJedis();

    try {
      jedis.ping();
    } catch (JedisConnectionException e){
      e.printStackTrace();
      throw new RuntimeException("Fail to connect to Redis!");
    }

    Spark.port(appConfig.getInt("http.port"));
    Spark.staticFiles.location("/public");
    Spark.init();
    Spark.awaitInitialization();

    Spark.before("*",(request, response) -> {
      response.header("Content-Type", "application/json");
    });

    Spark.options("*",(request, response) -> "OK");

    Spark.path("/work", ()->{
      Spark.post("/", new NewWorkRoute());
      Spark.patch("/", new StopWorkRoute());
      Spark.delete("/", new DeleteWorkRoute());
      Spark.put("/", new RestartWorkRoute());
    });

    Spark.path("/pool", ()->{
      Spark.get("/status", new GetPoolStatusRoute());
      Spark.delete("/shutdown", new ShutdownPoolRoute());
    });

    Spark.path("/node", ()->{
      Spark.post("/", new NewNodeRoute());
      Spark.delete("/", new DeleteNodeRoute());
    });

  }

  public static Config getAppConfig() {
    return appConfig;
  }
}
