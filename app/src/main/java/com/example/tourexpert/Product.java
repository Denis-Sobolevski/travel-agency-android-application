package com.example.tourexpert;

/**
 * this is the base class for the Product, each product category will
 * extend this base class
 */
public class Product {

    protected String key;
    protected int price;
    protected int availableAmount;

    public Product () { }

    public Product(String key, int price, int availableAmount) {
        this.key = key;
        this.price = price;
        this.availableAmount = availableAmount;
    }

    // region GETTERS
    public String getKey() {
        return key;
    }
    public int getPrice() {
        return price;
    }
    public int getAvailableAmount() {
        return availableAmount;
    }
    // endregion GETTERS

    // region SETTERS
    public void setKey(String key) {
        this.key = key;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public void setAvailableAmount(int availableAmount) {
        this.availableAmount = availableAmount;
    }
    // endregion SETTERS

    @Override
    public String toString() {
        return "Product{" + "\n" +
                "key='" + key + "\n" +
                ", price=" + price + "\n" +
                ", availableAmount=" + availableAmount +"\n";
    }
}
