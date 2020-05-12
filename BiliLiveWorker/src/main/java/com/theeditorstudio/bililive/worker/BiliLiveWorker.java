package com.theeditorstudio.bililive.worker;

import com.theeditorstudio.bililive.worker.factories.JedisFactory;
import com.theeditorstudio.bililive.worker.threads.*;
import com.theeditorstudio.bililive.worker.types.FutureThreadWrapper;
import com.theeditorstudio.bililive.worker.types.id.BiliLiveRoomID;
import com.theeditorstudio.bililive.worker.types.id.BiliUserID;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class BiliLiveWorker {

  private static final Logger logger = LoggerFactory.getLogger(BiliLiveWorker.class);

  private static Config appConfig = ConfigFactory.defaultApplication();
  private static Jedis jedis = new Jedis();
  private static int threadPoolSize = 0;
  private static ThreadPoolExecutor threadPoolExecutor;
  private static String nodeName = "BiliLiveRecorder420";

  private static final ConcurrentHashMap<Long, FutureThreadWrapper> threadConcurrentHashMap = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    // Load Config
    if(args.length == 1){
      logger.info("Passed In Config file path:" + args[0]);
      appConfig = ConfigFactory.parseFile(new File(args[0])).resolve();
    } else {
      appConfig = ConfigFactory.load("app.conf");
    }

    // Load Config to class
    threadPoolSize = appConfig.getInt("worker.maxThread");
    jedis = new JedisFactory().getJedis();
    nodeName = BiliLiveWorker.getAppConfig().getString("worker.name");

    // Try to connect to Redis
    try {
      jedis.ping();
    } catch (JedisConnectionException e){
      e.printStackTrace();
      throw new RuntimeException("Fail to connect to Redis!");
    }

    // Define Thread Pool
    threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize,
            Executors.defaultThreadFactory());

    // Load Previous Work
    loadPreviousWorks();

    // Spawn Supporting Thread
    new Thread(new PoolStatusThread(threadPoolExecutor)).start();
    new Thread(new PoolSubscriberThread()).start();
    new Thread(new PoolPingThread()).start();

    // Set Shutdown hook to prevent ffmpeg hanging in background !DO NOT SIGTERM!
    Runtime.getRuntime().addShutdownHook(new ShutdownHookThread());
  }

  private static void loadPreviousWorks(){
    String works = jedis.get(getAppConfig().getString("worker.name") + ":works");
    // No work
    if(works == null){
      logger.debug("No Previous work found");
      return;
    }
    if(works.isEmpty()){
      logger.debug("No Previous work found");
      return;
    }
    // Remap String
    JSONArray jsonArray = new JSONArray(works);
    ArrayList<String> arrayList = new ArrayList<>();
    for(int i = 0; i < jsonArray.length(); i++){
      arrayList.add(jsonArray.getString(i));
    }
    // Restore
    logger.info("Restoring Previous Work");
    for(String str: arrayList) {
      createThread(new BiliLiveRoomID(str));
    }
    logger.debug("Work Restored: " + arrayList.size());
  }

  public static Config getAppConfig() {
    return appConfig;
  }

  public static FutureThreadWrapper getThread(long threadID) {
    return threadConcurrentHashMap.get(threadID);
  }

  public static ConcurrentHashMap<Long, FutureThreadWrapper> getThreadHMap(){
    return threadConcurrentHashMap;
  }

  public static void saveWork(){
    JSONArray works = new JSONArray();
    for (Map.Entry<Long, FutureThreadWrapper> future: threadConcurrentHashMap.entrySet()) {
      works.put(future.getValue().getRunnable().getRoomID());
    }
    jedis.set(BiliLiveWorker.getNodeName() + ":works", works.toString());
  }


  public static void createThread(BiliUserID userID) {
    RecorderThread recorderThread = new RecorderThread(userID);
    submitThread(recorderThread);
  }

  public static void createThread(BiliLiveRoomID roomID) {
    RecorderThread recorderThread = new RecorderThread(roomID);
    submitThread(recorderThread);
  }

  public static String getNodeName(){
    return nodeName;
  }

  public static int getThreadPoolSize() {
    return threadPoolSize;
  }

  public static synchronized void shutdownPool(){
    for (Map.Entry<Long, FutureThreadWrapper> future: threadConcurrentHashMap.entrySet()) {
      future.getValue().getFuture().cancel(true);
    }
    threadConcurrentHashMap.clear();
  }

  private static void submitThread(RecorderThread recorderThread) {
    try {
      Future<?> future = threadPoolExecutor.submit(recorderThread);
      FutureThreadWrapper threadWrapper = new FutureThreadWrapper(future, recorderThread);
      threadConcurrentHashMap.put(recorderThread.getThreadId(), threadWrapper);
      logger.debug("New Thread ID:" + recorderThread.getThreadId());
    }catch (RejectedExecutionException e){
      e.printStackTrace();
    }
  }

}
