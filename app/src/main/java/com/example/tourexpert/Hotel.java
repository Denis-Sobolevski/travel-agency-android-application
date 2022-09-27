package com.example.tourexpert;

import android.net.Uri;

/**
 * this class is a model representation of a Hotel room collection
 */
public class Hotel extends Product {

    protected String name; // hotel name
    protected String type; // type of the hotel room
    protected String description; // short paragraph description about the hotel room
    protected String imageKey; // the path to the image associated with this hotel
    protected int stars; // the star rating of the hotel, 1 - 5

    public Hotel() {}

    public Hotel(String key, int price, int availableAmount, String name, String type, String description, String imageKey, int stars) {
        super(key, price, availableAmount);

        this.name = name;
        this.type = type;
        this.description = description;
        this.imageKey = imageKey;
        this.stars = stars;
    }

    // region GETTERS
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getImageKey() {return imageKey;}
    public int getStars() { return stars; }

    // endregion GETTERS

    // region SETTERS
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }
    public void setStars(int stars) { this.stars = stars; }
    // endregion SETTERS

    @Override
    public String toString() {
        return super.toString() +"hotel name: " + this.name+"\n"+
                                 "room type: " + this.type +"\n"+
                                 "image key: " + this.imageKey +"\n"+
                                 "Stars: " + this.stars + "\n" +
                                 "description:\n"+this.description+"\n";
    }

}
