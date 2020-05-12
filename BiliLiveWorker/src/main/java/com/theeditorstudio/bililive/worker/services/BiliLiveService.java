package com.theeditorstudio.bililive.worker.services;

import com.theeditorstudio.bililive.worker.types.id.BiliLiveRoomID;
import com.theeditorstudio.bililive.worker.types.id.BiliUserID;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This is the Bilibili Live Request Service
 */
public class BiliLiveService {

  /**
   * Request the original room ID
   *
   * @param roomID the RoomID
   * @return Http Response
   */
  public static HttpResponse<String> getRoomInfo(BiliLiveRoomID roomID) {
    try {
      HttpRequest request = HttpRequest.newBuilder(
              URI.create("https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id=" + roomID.getRoomID())
      )
              .header("Accept", "application/json")
              .build();
      return HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Request Current Bilibili Live Status and Room ID
   *
   * @param userID Bilibili UID
   * @return HttpResponse
   */
  public static HttpResponse<String> getLiveInfo(BiliUserID userID) {
    try {
      HttpRequest request = HttpRequest.newBuilder(
              URI.create("https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + userID.getUID())
      )
              .header("Accept", "application/json")
              .build();
      return HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Request Current Streaming URL
   *
   * @param roomID Live room ID
   * @return HttpResponse
   */
  public static HttpResponse<String> getVideoInfo(String roomID) {
    try {
      HttpRequest request = HttpRequest.newBuilder(
              URI.create("https://api.live.bilibili.com/room/v1/Room/playUrl?cid=" + roomID
                         + "&qn=10000&platform=web")
      )
              .header("Accept", "application/json")
              .build();

      return HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
