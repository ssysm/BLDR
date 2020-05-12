package com.theeditorstudio.bililive.worker.threads;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.theeditorstudio.bililive.worker.BiliLiveWorker;
import com.theeditorstudio.bililive.worker.services.BiliLiveService;
import com.theeditorstudio.bililive.worker.types.id.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

public class RecorderThread extends Thread implements Runnable {

  private JSONObject liveInfo;
  private final FFmpeg ffmpeg = FFmpeg.atPath(Paths.get(BiliLiveWorker.getAppConfig().getString("ffmpeg.path")));
  private final Logger logger = LoggerFactory.getLogger(BiliLiveWorker.class);
  private File distLocation = new File(BiliLiveWorker.getAppConfig().getString("ffmpeg.output"));
  private char status = 'V'; // V: Vacant, S: Waiting Stream to Open, E: Exception, R: Running, W: Warning, Q:Stopped
  private BiliLiveRoomID roomID;
  private BiliUserID userID;
  private final AtomicBoolean stopped = new AtomicBoolean(false);

  public RecorderThread(BiliLiveRoomID roomID) {
    this.roomID = roomID;
  }

  public RecorderThread(BiliUserID UID) {
    this.userID = UID;
  }

  public RecorderThread(BiliLiveRoomID roomID, File distLocation) {
    this.roomID = roomID;
    this.distLocation = distLocation;
  }

  public RecorderThread(BiliUserID UID, File distLocation) {
    this.userID = UID;
    this.distLocation = distLocation;
  }

  /**
   * Setup Destination Folder and check folder available space
   */
  private void setupDist() {
    this.distLocation = new File(
            this.distLocation.getAbsolutePath() + '/' + roomID.getRoomID());
    if (this.distLocation.exists()) {
      logger.debug("Directory Exist:" + this.distLocation.getAbsolutePath());
      return;
    }
    try {
      boolean mkdirResult = this.distLocation.mkdirs();
      if (!mkdirResult) {
        this.status = 'E'; // Set State to Error
        logger.error("Fail to Create Folder: " + this.distLocation.getAbsolutePath());
        throw new SecurityException("Fail to Create Folder");
      } else {
        logger.info("Created Directory: " + this.distLocation.getAbsolutePath());
        logger.debug(String.format("Dist Free Space: %.2fGB",(distLocation.getFreeSpace() / Math.pow(1024.0, 3))));
        if (distLocation.getFreeSpace() < 1 * (Math.pow(1024.0, 3))) {
          this.status = 'W'; // Set State to Warining
          logger.warn("Disk Space Low!(Free Space < 1GB)");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      this.status = 'E';
      logger.error("Fail to create folder:" + this.distLocation.getAbsolutePath());
    }
  }

  @Override
  public void run() {
    this.requestLiveInfo();
    Thread.currentThread().setName(Thread.currentThread().getName() + ',' + roomID.getRoomID());
    while (!this.stopped.get() && this.status != 'E'){ // If Stopped is not requested and not in error state
      // Set Thread Name
      this.requestLiveInfo();
      if(this.isLive()){
          String url = this.getStreamURL();
          if(url == null){
              return;
          }
          this.setupDist();
          this.downloadLive(url);
          this.status = 'Q';
      }

      // Sleep Loop
      try {
        logger.info("Record Thread State:" + this.status);
        Thread.sleep(BiliLiveWorker.getAppConfig().getInt("worker.waitTime"));
      } catch (InterruptedException|IllegalArgumentException e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * Request Live Info base on constrcutor Value
   */
  private void requestLiveInfo(){
    if(roomID == null){
      this.requestUserLiveInfo();
    }else if(userID == null){
      this.requestRoomLiveInfo();
    }else{
      logger.error("Invalid Arg provided in constructor");
    }
  }

  /**
   * Request Room Live Info by Room ID
   */
  private void requestRoomLiveInfo(){
    HttpResponse<String> infoRes = BiliLiveService.getRoomInfo(roomID);
    if (infoRes == null) {
      this.status = 'E'; // Set State to Error
      logger.error("Cannot get Room ID for:" + roomID.getRoomID());
      return;
    }
    liveInfo = new JSONObject(infoRes.body()).getJSONObject("data").getJSONObject("room_info");
  }

  /**
   * Request Room Live Info by User ID
   */
  private void requestUserLiveInfo(){
    HttpResponse<String> infoRes = BiliLiveService.getLiveInfo(userID);
    if (infoRes == null) {
      this.status = 'E'; // Set State to Error
      logger.error("Cannot get Live Info for User ID:" + userID.getUID());
      return;
    }
    liveInfo = new JSONObject(infoRes.body()).getJSONObject("data");
    liveInfo = liveInfo.put("room_id", liveInfo.get("roomid"));
    this.roomID = new BiliLiveRoomID((String) liveInfo.get("room_id"));
  }

  /**
   * Check if the room is live or not
   * @return room live state
   */
  private boolean isLive(){
    int liveStatus = (int) liveInfo.get("live_status");
    String roomName = (String) liveInfo.get("title");
    if (liveStatus != 1) {
      this.status = 'S'; // Set State to Waiting Stream
      logger.info(roomName + '(' + roomID.getRoomID() + ')' + "Not Yet open to stream");
      return false;
    }else{
      return true;
    }
  }

  /**
   * Get the highest quality stream
   * @return Stream URL
   */
  private String getStreamURL(){
    HttpResponse<String> videoInfoRes = BiliLiveService.getVideoInfo(roomID.getRoomID());
    if (videoInfoRes == null) {
      this.status = 'E'; // Set State to Error
      logger.error("Cannot get Stream URL");
      return null;
    }
    JSONObject videoInfo = new JSONObject(videoInfoRes.body());
    return (String) videoInfo.getJSONObject("data").getJSONArray("durl").getJSONObject(0).get("url");
  }

  /**
   * Download Live
   * @param videoURL Stream Video URL
   * @throws RuntimeException If Runnable are being called to stop
   */
  private void downloadLive(String videoURL){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
    LocalDateTime now = LocalDateTime.now();
    // Build FFMPEG Job
    FFmpeg builder = this.ffmpeg
            .setLogLevel(LogLevel.INFO)
            .addInput(UrlInput.fromUrl(videoURL)
                    .setUserAgent("Chrome/81.0.4044.129")
            )
            .setOverwriteOutput(true)
            .addOutput(UrlOutput.toPath(Path.of(this.distLocation.getAbsolutePath() + '/'
                                                + dtf.format(now) + ".flv"))
                    .copyAllCodecs()
            );
    // Set Progress Info
    this.ffmpeg.setProgressListener(progress -> {
      this.status = 'R'; // Set State to Recording
    });
    logger.info("Starting FFMPEG Job...");
    builder.execute();
  }

  public char getStatus() {
    return this.status;
  }

  public BiliLiveRoomID getRoomID() {
    return this.roomID;
  }

  public long getThreadId(){
    return this.getId();
  }

}
