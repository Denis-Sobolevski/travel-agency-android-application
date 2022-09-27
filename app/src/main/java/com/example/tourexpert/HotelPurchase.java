package com.example.tourexpert;

/**
 * this class is a model representation of a purchase history object
 * which a user has made from the hotel category
 */
public class HotelPurchase extends Purchase {

    protected int days; // the amount of days of rent
    protected String hotelName;
    protected String type; // room type
    protected String description;
    protected String arrivalDate;
    protected String departureDate;

    public HotelPurchase() { }

    public HotelPurchase(long datOfPurchase, int amount, double price, String key, int days,String hotelName, String type, String description
    ,String arrivalDate, String departureDate) {
        super(datOfPurchase, amount, price, key);

        this.days = days;
        this.hotelName = hotelName;
        this.type = type;
        this.description = description;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

    // region GETTERS
    public int getDays() { return days; }
    public String getHotelName() { return hotelName; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getArrivalDate() { return arrivalDate; }
    public String getDepartureDate() { return departureDate; }

    // endregion GETTERS

    // region SETTERS
    public void setDays(int days) { this.days = days; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setArrivalDate(String arrivalDate) { this.arrivalDate = arrivalDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }

    // endregion SETTERS

    @Override
    public String toString() {
        return "HotelPurchase: " + super.toString() +"days of rent: " + this.days +"\n"+
                "hotel name: " + this.hotelName+"\n"+
                "type of room: " + this.type+"\n"+
                "description:\n"+this.description+"\n"+
                "arrival date: " + this.arrivalDate +"\n" +
                "deparute date: " + this.departureDate+"\n";
    }
}
