package com.inhwa.nan.activity;

import java.io.Serializable;

/**
 * Created by Inhwa_ on 2017-06-19.
 */

public class Performance implements Serializable {
    private int PID;
    private String title;
    private String content;
    private String region;
    private String genre;
    private String pdate;
    private String ptime;
    private String image;
    private int like_state;
    private String location;
    private int artist_no;
    private int price;
    private int count;
    private boolean userLiked;

    public Performance() {
    }

    public Performance(int PID, String title, String content, String region, String genre, String pdate, String ptime, String image, String location) {
        this.PID = PID;
        this.title = title;
        this.content = content;
        this.region = region;
        this.genre = genre;
        this.pdate = pdate;
        this.ptime = ptime;
        this.image = image;
        this.location = location;
    }

    public Performance(int PID, String title, String content, String region, String genre, String pdate, String ptime, String image, int like_state, String location, int artist_no, int price) {
        this.PID = PID;
        this.title = title;
        this.content = content;
        this.region = region;
        this.genre = genre;
        this.pdate = pdate;
        this.ptime = ptime;
        this.image = image;
        this.like_state = like_state;
        this.artist_no = artist_no;
        this.price = price;
    }

    public int getArtist_no() {return artist_no;}

    public void setArtist_no(int artist_no) {this.artist_no = artist_no;}

    public int getPrice() {return price;}

    public void setPrice(int price) {this.price = price;}

    public int getPID() { return PID; }

    public void setPID(int PID) { this.PID = PID; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public String getPtime() {
        return ptime;
    }

    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }


    public int getLike_state() { return like_state; }

    public void setLike_state(int like_state) { this.like_state = like_state; }

    public String getLocation() {return location;}

    public void setLocation(String location) { this.location = location;}

}