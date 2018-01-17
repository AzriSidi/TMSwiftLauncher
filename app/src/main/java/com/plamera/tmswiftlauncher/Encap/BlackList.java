package com.plamera.tmswiftlauncher.Encap;

public class BlackList {
    String ID;
    String BlackListNumber;

    public BlackList(String id, String blackListNumber) {
        ID = id;
        BlackListNumber = blackListNumber;
    }

    public BlackList(String blackListNumber) {
        BlackListNumber = blackListNumber;
    }

    // Empty constructor
    public BlackList(){

    }

    public String getId() {
        return ID;
    }

    public void setId(String id) {
        this.ID = id;
    }

    public String getBlackListNumber() {
        return BlackListNumber;
    }

    public void setBlackListNumber(String blackListNumber) {
        this.BlackListNumber = blackListNumber;
    }
}
