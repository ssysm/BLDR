package com.theeditorstudio.bililive.worker.handlers;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.threads.RecorderThread;
import com.theeditorstudio.bililive.worker.types.id.BiliLiveRoomID;
import com.theeditorstudio.bililive.worker.types.id.BiliUserID;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;

public class JedisWorkSubscriber extends JedisPubSub {

  private static final Logger logger = LoggerFactory.getLogger(BiliLiveWorker.class);

  @Override
  public void onMessage(String channel, String message) {
    logger.info("Get Work Message: " + message);
    try {
      JSONObject msg = new JSONObject(message);
      String type = msg.getString("type");
      switch (type) {
        case "new":
          if (BiliLiveWorker.getThreadHMap().size() + 1 > BiliLiveWorker.getThreadPoolSize()) {
            logger.error("Thread Pool Full!");
            return;
          }
          String idType = msg.getString("id_type");
          String id = msg.getString("id");
          if (idType.equals("room_id")) {
            logger.info("Create new thread base on room_id: " + id);
            BiliLiveWorker.createThread(new BiliLiveRoomID(id));
          } else if (idType.equals("user_id")) {
            logger.info("Create new thread base on user_id: " + id);
            BiliLiveWorker.createThread(new BiliUserID(id));
          }
          break;
        case "stop": {
          long threadID = msg.getLong("thread_id");
          logger.info("Stopping a thread, id:" + threadID);
          BiliLiveWorker.getThread(threadID).getFuture().cancel(true);
          break;
        }
        case "delete": {
          long threadID = msg.getLong("thread_id");
          logger.info("Deleting  a thread, id:" + threadID);
          BiliLiveWorker.getThreadHMap()
                  .remove(threadID)
                  .getFuture().cancel(true);
          break;
        }
        case "shutdown": {
          logger.info("Shutting down pool.");
          BiliLiveWorker.shutdownPool();
          break;
        }
        case "restart": {
          long threadID = msg.getLong("thread_id");
          logger.info("Resarting Thread: " + threadID);
          RecorderThread stoppedThread = BiliLiveWorker.getThreadHMap().remove(threadID).getRunnable();
          BiliLiveWorker.createThread(stoppedThread.getRoomID());
          break;
        }
      }

      BiliLiveWorker.saveWork();
    }catch (Exception e){
      e.printStackTrace();
    }
  }
}
