package datamodel;

/**
 * Created by Kai on 17.08.2017.
 * part of 80p - Codename Dolphin
 */
public class Plan {
    private int id;
    private int distance;
    private String content;

    public Plan(int id, int distance, String content) {
        this.id = id;
        this.distance = distance;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
