package com.theeditorstudio.bililive.worker.threads;

import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.factories.JedisFactory;
import com.theeditorstudio.bililive.worker.types.FutureThreadWrapper;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class PoolStatusThread implements Runnable {

  private final ThreadPoolExecutor executor;
  private final int delay = BiliLiveWorker.getAppConfig().getInt("worker.reportTime");
  private final Jedis jedis = new JedisFactory().getJedis();

  public PoolStatusThread(ThreadPoolExecutor executor){
    this.executor = executor;
  }

  @Override
  public void run() {
    while (true) {
      JSONObject status = new JSONObject();
      status.put("current_size", this.executor.getPoolSize());
      status.put("pool_size", this.executor.getCorePoolSize());
      status.put("task_count", this.executor.getTaskCount());
      status.put("active_count", this.executor.getActiveCount());
      JSONArray jobStat = new JSONArray();
      for (Map.Entry<Long, FutureThreadWrapper> future: BiliLiveWorker.getThreadHMap().entrySet()) {
        JSONObject stat = new JSONObject();
        stat.put("thread_id", String.valueOf(future.getKey()));
        stat.put("room_id", future.getValue().getRunnable().getRoomID());
        stat.put("status", Character.valueOf(future.getValue().getRunnable().getStatus()));
        stat.put("thread_stopped",future.getValue().getFuture().isCancelled());
        jobStat.put(stat);
      }
      status.put("job_stat",jobStat);
      status.put("last_update", Instant.now().getEpochSecond());
      status.put("name", String.valueOf(BiliLiveWorker.getNodeName()));
      jedis.set(BiliLiveWorker.getNodeName() + ":status", status.toString());
      jedis.publish(BiliLiveWorker.getNodeName() + ":status", status.toString());
      try{
        Thread.sleep(delay);
      }catch (IllegalArgumentException|InterruptedException e){
        e.printStackTrace();
      }
    }
  }

}
