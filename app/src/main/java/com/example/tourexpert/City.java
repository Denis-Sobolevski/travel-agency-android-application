package com.example.tourexpert;

/**
 * this class is a model representation for a City with statistics,
 * cityName - name of the city
 * countryName - name of the country of the city
 * status - true: users can browse the city, false: users are unable to browse the city
 * // statistics
 * soldAttractionAmount - the amount of attraction tickets sold for the city, since creation
 * soldFlightTicketsAmount - the amount of flight tickets sold for the city, since creation
 * soldHotelRoomsAmount - the amount of hotel rooms sold for the city, since creation
 * //
 * key - unique identifier, the Node Key(primary key) upon creation
 */
public class City {

    protected String cityName;
    protected String countryName;
    protected boolean status;

    // unique identifier:
    protected String key;

    public City() {

    }

    public City(String cityName, String countryName, boolean status, String key) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.status = status;

        this.key = key;
    }

    // region GETTERS:
    public String getCityName() {
        return this.cityName;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public boolean getStatus() {
        return this.status;
    }

    public String getKey() {
        return this.key;
    }
    // endregion GETTERS:


    // region SETTERS:
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    // endregion SETTERS:

    @Override
    public String toString() {
        return "City:\n" +
                "cityName='" + cityName + "\n" +
                ", countryName='" + countryName + "\n" +
                ", status=" + status +
                ", key='" + key + "\n";
    }
}
