package com.example.tourexpert;

/**
 * each time a user purchases something
 * we will save this class to the statistics collection
 * under the city which it was purchased from
 */
public class Statistic {

    protected String category;
    protected long date;
    protected int amount;
    protected double price;

    public Statistic() { } // default constructor

    public Statistic(String category, long date, int amount, double price) {
        this.category = category;
        this.date = date;
        this.amount = amount;
        this.price = price;
    }

    // region GETTERS
    public String getCategory() {
        return category;
    }
    public long getDate() {
        return date;
    }
    public int getAmount() {
        return amount;
    }
    public double getPrice() {
        return price;
    }
    // endregion GETTERS

    // region SETTERS
    public void setCategory(String category) {
        this.category = category;
    }
    public void setDate(long date) {
        this.date = date;
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
        return "Statistic:\n" + "category='" + category + "\n" + ", date=" + date + "\n" + ", amount=" + amount + "\n" + ", price=" + price + "\n";
    }
}
