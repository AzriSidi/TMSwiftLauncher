package com.plamera.tmswiftlauncher.Encap;

/*
 * Created by Plamera on 19/9/2017.
 */

public class WhileList {

    String Name;
    String Package;

    public WhileList(String wlName, String wlPackage) {
        Name = wlName;
        Package = wlPackage;
    }

    public WhileList() {

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPackage() {
        return Package;
    }

    public void setPackage(String aPackage) {
        Package = aPackage;
    }
}
