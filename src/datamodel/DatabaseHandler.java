package datamodel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;
import java.util.Observable;
import java.util.Optional;

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
            Statement stmt =  stmt = connection.createStatement();
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

}
