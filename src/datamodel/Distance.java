package datamodel;

/**
 * Created by Kai on 30.08.2017.
 * part of 80p - Codename Dolphin
 */
public enum Distance {
    LOW(" distanz <  2000"),
    SHORT(" distanz >= 2000 AND distanz < 3000"),
    MEDIUM(" distanz >= 3000 AND distanz < 4000"),
    LONG(" distanz >= 4000");

    private final String clause;

    Distance(String clause){
        this.clause = clause;
    }

    public String getClause() {
        return clause;
    }

}
