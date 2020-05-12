package com.theeditorstudio.bililive.worker.types.id;

public class BiliLiveRoomID {

  String roomID;

  public BiliLiveRoomID(String roomID) {
    this.roomID = roomID;
  }

  public String getRoomID() {
    return roomID;
  }

  public void setRoomID(String roomID) {
    this.roomID = roomID;
  }

  @Override
  public String toString() {
    return roomID;
  }
}
