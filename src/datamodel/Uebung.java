package datamodel;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Kai on 17.08.2017.
 * part of 80p - Codename Dolphin
 */
public class Uebung {
    private SimpleStringProperty distanz;
    private SimpleStringProperty uebung;

    public Uebung(String distanz, String uebung){
        this.distanz = new SimpleStringProperty(distanz);
        this.uebung  = new SimpleStringProperty(uebung);
    }

    public String getDistanz() {
        return distanz.get();
    }

    public String getUebung() {
        return uebung.get();
    }

    public void setDistanz(String distanz) {
        this.distanz.set(distanz);
    }

    public void setUebung(String uebung) {
        this.uebung.set(uebung);
    }
}
