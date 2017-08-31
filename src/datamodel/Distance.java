package datamodel;

/**
 * Created by Kai on 30.08.2017.
 * part of 80p - Codename Dolphin
 */
public enum Distance {
    EMPTY("", ""),
    LOW(" distanz <  2000", "Distanz kleiner 2km"),
    SHORT(" distanz >= 2000 AND distanz < 3000", "Distanz zwischen 2 und 3km"),
    MEDIUM(" distanz >= 3000 AND distanz < 4000", "Distanz zwischen 3 und 4km"),
    LONG(" distanz >= 4000", "Distanz über 4km ");

    private final String clause;
    private final String information;

    Distance(String clause, String information){
        this.clause = clause;
        this.information = information;
    }

    public String getClause() {
        return clause;
    }

    public String getInformation() {
        return information;
    }
}
