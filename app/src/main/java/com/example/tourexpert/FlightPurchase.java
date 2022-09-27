package com.example.tourexpert;

/**
 * Flight purchase for a user, will sit under the
 * history collection, user id, and flights purchase history collection
 */
public class FlightPurchase extends Purchase  {

    protected long dateOfFlight;
    protected String source;
    protected String destination;
    protected String flightClass;

    public FlightPurchase() {
        super();
    }

    public FlightPurchase(long dateOfPurchase, int amount, double price, String key, long dateOfFlight, String source, String destination, String flightClass) {
        super(dateOfPurchase, amount, price, key);
        this.dateOfFlight = dateOfFlight;
        this.source = source;
        this.destination = destination;
        this.flightClass = flightClass;
    }

    // region GETTERS
    public long getDateOfFlight() { return dateOfFlight; }
    public String getSource() {
        return source;
    }
    public String getDestination() {
        return destination;
    }
    public String getFlightClass() {
        return flightClass;
    }
    // endregion GETTERS

    // region SETTERS
    public void setDateOfFlight(long dateOfFlight) {
        this.dateOfFlight = dateOfFlight;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public void setFlightClass(String flightClass) {
        this.flightClass = flightClass;
    }
    // endregion SETTERS

    @Override
    public String toString() {
        return "FlightPurchase:" + super.toString() + "\n" + "dateOfFlight=" + dateOfFlight + "\n" + ", source='" + source + "\n" + ", destination='" + destination + "\n" + ", flightClass='" + flightClass + "\n";
    }
}