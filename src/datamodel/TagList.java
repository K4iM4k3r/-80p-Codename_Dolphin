package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Created by Kai on 24.08.2017.
 * part of 80p - Codename Dolphin
 */
public class TagList {
    private Map<String, Integer> mapAllTag;
    private List<String> tagPlan;
    private List<String> changes;
    private int id;


    TagList(Map<String, Integer> mapAllTag, List<String> tagPlan , int id){
        this.mapAllTag = mapAllTag;
        this.id = id;
        this.tagPlan = tagPlan;
        this.changes = new ArrayList<>(tagPlan);
    }

    public ObservableList<String> getData(){
        return FXCollections.observableArrayList(mapAllTag.keySet());
    }

    public void changeTag(String tag){
        if(changes.contains(tag)){
            changes.remove(tag);
        }
        else{
            changes.add(tag);
        }
    }

    public void saveChanges(DatabaseHandler db, int newId){
        if(newId == id){
            for(String s : tagPlan){
                if(changes.contains(s)){
                    changes.remove(s);
                }
                else{
                    db.removeTagOnPlan(newId, getId(s));
                }
            }
        }
        changes.forEach(t -> db.setTagOnPlan(newId, getId(t)));
    }

    public int getId(String in){
        return mapAllTag.get(in);
    }

    public String toTagString() {
        StringBuilder builder = new StringBuilder();
        changes.forEach(s -> {
            builder.append(s);
            builder.append(", ");
        });
        return builder.length() > 1 ? builder.substring(0, builder.length()-2) : " " ;
    }

    public void clear(){
        this.tagPlan.clear();
        this.changes.clear();
        this.id = -1;
    }

    public void updateAllTag(DatabaseHandler db){
        this.mapAllTag = db.selectAllTag();
    }


}
