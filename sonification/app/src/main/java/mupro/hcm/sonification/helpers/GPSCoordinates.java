package mupro.hcm.sonification.helpers;

// consider returning Location instead of this dummy wrapper class
public class GPSCoordinates {
    public double longitude;
    public double latitude;

    public GPSCoordinates(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String toString() {
        return "Coordinates: " + this.longitude + " | " + this.latitude;
    }
}