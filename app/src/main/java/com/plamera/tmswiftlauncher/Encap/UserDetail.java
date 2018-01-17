package com.plamera.tmswiftlauncher.Encap;

public class UserDetail {

    String _staffId;
    String _token;

    // constructor
    public UserDetail(String _staffId, String _token){
        this._staffId = _staffId;
        this._token = _token;
    }

    public UserDetail() {

    }

    public String get_staffId() {
        return _staffId;
    }

    public void set_staffId(String _userName) {
        this._staffId = _userName;
    }

    public String get_token() {
        return _token;
    }

    public void set_token(String _token) {
        this._token = _token;
    }
}