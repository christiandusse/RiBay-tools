package com.ribay.tools.data;

import java.util.Date;
import java.util.List;

/**
 * Created by CD on 30.04.2016.
 */
public class Tweet {

    private String user;
    private String text;
    private boolean retweet;
    private Date date;
    private List<Double> coordinates;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRetweet() {
        return retweet;
    }

    public void setRetweet(boolean retweet) {
        this.retweet = retweet;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}
