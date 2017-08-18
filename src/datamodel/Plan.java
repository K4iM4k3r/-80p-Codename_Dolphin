package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

/**
 * Created by Kai on 17.08.2017.
 * part of 80p - Codename Dolphin
 */
public class Plan {
    private ArrayList<Uebung> lst = new ArrayList<>();
    private ArrayList<String> tags = new ArrayList<>();
    private int dist = 0;

    public Plan(ArrayList<Uebung> lst, ArrayList<String> tags, int dist) {
        this.lst = lst;
        this.tags = tags;
        this.dist = dist;
    }

    public ObservableList<Uebung> getPlan(){
        return FXCollections.observableArrayList(lst);
    }
}
