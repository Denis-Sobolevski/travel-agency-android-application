package com.example.tourexpert;

/**
 * this class is a model representation for an Attraction,
 * ticketPrice - price for a ticket
 * availableAmount - the available amount of tickets
 * type - may be football game, comedy show, disney park..
 * description - short description containing details about the attraction itself
 * key - unique key
 */
public class Attraction extends Product {

    protected String type; // like comedy, show, football game
    protected String description; // short paragraph description about the attraction
    protected long date;
    protected String imageKey;

    public Attraction() {
        super();
    }

    public Attraction(String key, int price, int availableAmount, String type, long date, String description, String imageKey) {
        super(key, price, availableAmount);
        this.type = type;
        this.date = date;
        this.description = description;
        this.imageKey = imageKey;
    }

    // region GETTERS
    public String getType() {
        return type;
    }
    public long getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }
    public String getImageKey() {return this.imageKey; }
    // endregion GETTERS

    // region SETTERS
    public void setType(String type) {
        this.type = type;
    }
    public void setDate(long date) {
        this.date = date;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }
    // endregion SETTERS

    public String toString() {
        return super.toString() + "type: " + this.type + "\n" +
                                  "date: " + this.date + "\n" +
                                  "imageKey: " + this.imageKey +"\n" +
                                  "descriptions:\n" + this.description + "\n";
    }
}
