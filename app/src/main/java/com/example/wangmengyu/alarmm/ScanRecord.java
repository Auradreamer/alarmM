package com.example.wangmengyu.alarmm;

/**
 * Created by Adele on 2015-11-08.
 */
public class ScanRecord {
    public int id;
    public String timestamp;
    public String ssid;
    public String bssid;
    public int level;

    public ScanRecord(int id_, String timestamp_, String ssid_, String bssid_, int level_) {
        id = id_;
        timestamp = timestamp_;
        ssid = ssid_;
        bssid = bssid_;
        level = level_;
    }

    public String idToString() { return String.valueOf(id); }

    public String levelToString() { return String.valueOf(level); }
}
