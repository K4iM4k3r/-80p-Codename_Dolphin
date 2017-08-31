package datamodel;

/**
 * Created by Kai on 17.08.2017.
 * part of 80p - Codename Dolphin
 */
public class Uebung {
    private String distance;
    private String practice;

    public Uebung(String distance, String practice){
        this.distance = distance;
        this.practice  = practice;
    }

    public String getDistance() {
        return distance;
    }

    public String getPractice() {
        return practice;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setPractice(String practice) {
        this.practice = practice;
    }
}
