package com.example.tourexpert;

/**
 * this class is a model representation for a Flight,
 * source - from where the flight will take off, preferably contains city and airport
 * destination - destination of the flight, preferably contains city and airport
 * dateOfFlight -  date of flight in long, preferably formatted when used to display user as follows: "yyyy.mm.dd hh:mm"
 * ticketPrice - the price for a ticket of this Flight, will be in dollars
 * availableAmount - the amount of tickets available for this Flight
 * key - the primary key / node key **** UNIQUE IDENTIFIER ****
 */
public class Flight extends Product {

    protected String source;
    protected String destination;
    protected long dateOfFlight;
    protected String flightClass;

    public Flight() {
        super();
    }

    public Flight( String key, int price, int availableAmount, String source, String destination, long dateOfFlight, String flightClass) {
        super(key, price, availableAmount);
        this.source = source;
        this.destination = destination;
        this.dateOfFlight = dateOfFlight;
        this.flightClass = flightClass;
    }

    // region GETTERS:
    public String getSource() {
        return source;
    }
    public String getDestination() {
        return destination;
    }
    public long getDateOfFlight() {
        return dateOfFlight;
    }
    public String getFlightClass() { return flightClass; }
    // endregion GETTERS

    // region SETTERS:
    public void setSource(String source) {
        this.source = source;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public void setDateOfFlight(long dateOfFlight) {
        this.dateOfFlight = dateOfFlight;
    }
    public void setFlightClass(String flightClass) { this.flightClass = flightClass; }
    // endregion SETTERS

    @Override
    public String toString() {
        return super.toString() + "source: " + this.source +"\n" +
                                  "destination: " + this.destination +"\n" +
                                  "date of flight: " + this.dateOfFlight +"\n"+
                                  "flight class: " + this.flightClass +"\n";
    }
}
