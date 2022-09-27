package com.example.tourexpert;

/**
 * base abstract class for a user Purchase, each purchase
 * from a certain category will extend this base class
 */
public abstract class Purchase {

    protected long dateOfPurchase; // date of purchase
    protected int amount; // amount of the product that was purchased
    protected double price;
    protected String key; // the number of the purchase

    public Purchase() { } // default constructor

    public Purchase(long dateOfPurchase, int amount, double price, String key) {
        this.key = key;
        this.dateOfPurchase = dateOfPurchase;
        this.amount = amount;
        this.price = price;
    }

    // region GETTERS
    public String getKey() {
        return key;
    }

    public long getDateOfPurchase() {
        return dateOfPurchase;
    }
    public int getAmount() {
        return amount;
    }
    public double getPrice() {
        return price;
    }
    // endregion GETTERS

    // region SETTERS
    public void setKey(String key) {
        this.key = key;
    }
    public void setDateOfPurchase(long dateOfPurchase) {
        this.dateOfPurchase = dateOfPurchase;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    // endregion SETTERS

    @Override
    public String toString() {
        return "key='" + key + "\n" + ", dateOfPurchase=" + dateOfPurchase + "\n" + ", amount=" + amount + "\n" + ", price=" + price + "\n";
    }
}
