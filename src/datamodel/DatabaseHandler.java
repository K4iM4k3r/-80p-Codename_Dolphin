package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Kai on 18.08.2017.
 * part of 80p - Codename Dolphin
 */
public class DatabaseHandler {

    private final String url = "jdbc:sqlite:."+ File.separator+"database"+File.separator+"database.sqlite";
    private Connection connection;

    public DatabaseHandler(){
        connect();
    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void connect() {
        try {
            String driver = "org.sqlite.JDBC";
            Class.forName(driver);
            this.connection = DriverManager.getConnection(this.url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int addPlan(int dis, String content){
        String query = "INSERT INTO Plan (Distanz, Inhalt)  VALUES (?, ?);";
        try{
            PreparedStatement stmt = connection.prepareStatement(query);

            stmt.setInt(1, dis);
            stmt.setString(2, content);
            int res = stmt.executeUpdate();

            if(res == 0 ){
                return -1;
            }
            ResultSet generateID = stmt.getGeneratedKeys();
            if (generateID.next()){
                return generateID.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int updatePlan(int id, int distance , String content){
        String query = "UPDATE plan SET distanz= ?, inhalt= ? WHERE id = ?; ";
        try{
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, distance);
            statement.setString(2, content);
            statement.setInt(3, id);
            int res = statement.executeUpdate();
            
            if(res == 0 ){
                return -1;
            }
            return 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public Optional<Plan> selectPlan(int id){
        try {
            String query = "SELECT * FROM  plan WHERE id = ? ;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                return Optional.of(new Plan(rs.getInt(1), rs.getInt(3),rs.getString(2)));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int deletePlan(int id){
        try {
            String query = "DELETE FROM plan WHERE id= ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int res = statement.executeUpdate();

            if(res == 0){
                return -1;
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ObservableList<String> selectAllPlan(){
        ObservableList<String> data = FXCollections.observableArrayList();

        try {
            String query = "SELECT * FROM  Plan";
            Statement stmt = connection.createStatement();
            ResultSet rs =  stmt.executeQuery(query);

            while(rs.next()){
                int maxLength = 100;
                String summary = rs.getString(2);
                summary = summary.length() > maxLength ? summary.substring(0, maxLength)+ "\n..." : summary;

                data.add(rs.getString(1) + " - " + summary);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public int addUserTag(String name){
        try {
            String query = "INSERT INTO tag (name) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int updateUserTag(int id, String value){
        try {
            String query = "UPDATE tag SET name=? WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, value);
            statement.setInt(2, id);
            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int deleteTag(int id){
        try{
            String query = "DELETE FROM tag WHERE id=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    int setTagOnPlan(int plan, int tag){
        try {
            String query = "INSERT INTO plan_tag (id_plan, id_tag) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, plan);
            statement.setInt(2, tag);

            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    int removeTagOnPlan(int plan, int tag){
        try{
            String query = "DELETE FROM plan_tag WHERE id_plan=? AND id_tag=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, plan);
            statement.setInt(2, tag);

            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Map<String, Integer> selectAllTag(){
        Map<String, Integer> result = new LinkedHashMap<>();
        try {
            String query = "SELECT * FROM tag";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                result.put(resultSet.getString("name" ), resultSet.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public TagList getAllTagsOnPlan(int id){
        try{
            String querry = "SELECT id_tag, id_plan, name FROM plan_tag INNER JOIN tag ON plan_tag.id_tag=tag.id WHERE id_plan=?";
            PreparedStatement statement = connection.prepareStatement(querry);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            List<String> result = new ArrayList<>();
            while(rs.next()){
                result.add( rs.getString("name" ) );
            }

            return new TagList(selectAllTag(), result, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNumberOfPlan(){
        try {
            String query = "SELECT count(id) FROM plan";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Optional<Plan> getRandomPlan(){
        try{
            String query = "SELECT id FROM plan;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            List<Integer> ids = new ArrayList<>();
            while(rs.next()){
             ids.add(rs.getInt(1));
            }
            Random random = new Random();
            return selectPlan(ids.get(random.nextInt(ids.size())));

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public int addBookmark(int id, String comment){
        try {
            String query = "INSERT INTO bookmark (id_plan, comment, added) VALUES (?, ?, ?);";
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, id);
            statement.setString(2, comment);
            statement.setString(3, df.format(Calendar.getInstance().getTime()));

            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int removeBookmark(String comment){
        try {
            String query = "DELETE FROM bookmark WHERE comment=?;";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, comment);

            int res = statement.executeUpdate();
            if(res == 0){
                return -1;
            }
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public ObservableList<String> selectAllBookmarks(){
        ObservableList<String> list = FXCollections.observableArrayList();
        try{
            String query = "SELECT * FROM bookmark;";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet result = statement.executeQuery();
            while(result.next()){
                list.add("Plan " + result.getString(2)+ " - " + result.getString(3));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


}
