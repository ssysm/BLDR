package com.theeditorstudio.bililive.worker.types.id;

public class BiliUserID {
  String UID;

  public BiliUserID(String UID) {
    this.UID = UID;
  }

  public String getUID() {
    return UID;
  }

  public void setUID(String UID) {
    this.UID = UID;
  }

  @Override
  public String toString() {
    return UID;
  }
}
