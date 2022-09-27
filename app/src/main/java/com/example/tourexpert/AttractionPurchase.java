package com.example.tourexpert;

/**
 * Flight purchase for a user, will sit under the
 * history collection, user id, and attraction purchase history collection
 */
public class AttractionPurchase extends Purchase {

    protected long dateOfAttraction;
    protected String type;
    protected String description;

    public AttractionPurchase() { super(); }

    public AttractionPurchase(long dateOfPurchase, int amount, double price, String key, long dateOfAttraction, String type, String description) {
        super(dateOfPurchase,amount,price,key);
        this.dateOfAttraction = dateOfAttraction;
        this.type = type;
        this.description = description;
    }

    // region GETTERS
    public long getDateOfAttraction() { return dateOfAttraction; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    // endregion GETTERS

    // region SETTERS
    public void setDateOfAttraction(long dateOfAttraction) { this.dateOfAttraction = dateOfAttraction; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    // endregion SETTERS

    @Override
    public String toString() {
        return "AttractionPurchase: " + super.toString() +" date of attraction: " + this.dateOfAttraction +"\n" +
                "type: " + this.type +"\n"+ "description:\n"+this.description+"\n";
    }

}
