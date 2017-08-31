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

    private void connect() {
        try {
            String driver = "org.sqlite.JDBC";
            Class.forName(driver);
            this.connection = DriverManager.getConnection(this.url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int addPlan(int dis, String content){
        String query = "INSERT INTO Plan (Distanz, Inhalt, added)  VALUES (?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(query)){
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            stmt.setInt(1, dis);
            stmt.setString(2, content);
            stmt.setString(3, df.format(Calendar.getInstance(Locale.GERMANY).getTime()));
            int res = stmt.executeUpdate();

            if(res == 0 ){
                return -1;
            }
            ResultSet generateID = stmt.getGeneratedKeys();
            if (generateID.next()){
                int result = generateID.getInt(1);
                generateID.close();
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updatePlan(int id, int distance , String content){
        String query = "UPDATE plan SET distanz= ?, inhalt= ? WHERE id = ?; ";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, distance);
            statement.setString(2, content);
            statement.setInt(3, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public Optional<Plan> selectPlan(int id){
        String query = "SELECT * FROM  plan WHERE id = ? ;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                Optional<Plan> result = Optional.of(new Plan(rs.getInt(1), rs.getInt(3),rs.getString(2)));
                rs.close();
                return result;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void deletePlan(int id){
       String query = "DELETE FROM plan WHERE id= ?";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);

            statement.executeUpdate();
            deleteAllTagOnPlan(id);
            deleteAllBookmarksOnPlan(id);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllTagOnPlan(int id){
        String query = "DELETE FROM plan_tag WHERE id_plan=?;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteAllBookmarksOnPlan(int id){
        String query = "DELETE FROM bookmark WHERE id_plan=?;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> selectAllPlan(){
        String query = "SELECT * FROM  Plan";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            ResultSet rs =  statement.executeQuery();

            return makeListFromResult(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addUserTag(String name){
        String query = "INSERT INTO tag (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setString(1, name);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUserTag(int id, String value){
        String query = "UPDATE tag SET name=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setString(1, value);
            statement.setInt(2, id);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTag(int id){
        String query = "DELETE FROM tag WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    int setTagOnPlan(int plan, int tag){
        String query = "INSERT INTO plan_tag (id_plan, id_tag) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)){

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

    void removeTagOnPlan(int plan, int tag){
        String query = "DELETE FROM plan_tag WHERE id_plan=? AND id_tag=?;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, plan);
            statement.setInt(2, tag);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    Map<String, Integer> selectAllTag(){
        Map<String, Integer> result = new LinkedHashMap<>();
        String query = "SELECT * FROM tag";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()){
                result.put(resultSet.getString("name" ), resultSet.getInt(1));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public TagList getAllTagsOnPlan(int id){
        String query = "SELECT id_tag, id_plan, name FROM plan_tag INNER JOIN tag ON plan_tag.id_tag=tag.id WHERE id_plan=?";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            List<String> result = new ArrayList<>();
            while(rs.next()){
                result.add( rs.getString("name" ) );
            }

            rs.close();
            return new TagList(selectAllTag(), result, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getNumberOfPlan(){
        String query = "SELECT count(id) FROM plan";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                int res = rs.getInt(1);
                rs.close();
                return res;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Optional<Plan> getRandomPlan(){
        String query = "SELECT id FROM plan;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            ResultSet rs = statement.executeQuery();
            List<Integer> ids = new ArrayList<>();
            while(rs.next()){
             ids.add(rs.getInt(1));
            }
            Random random = new Random();
            rs.close();
            return selectPlan(ids.get(random.nextInt(ids.size())));

        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void addBookmark(int id, String comment){
        String query = "INSERT INTO bookmark (id_plan, comment, added) VALUES (?, ?, ?);";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, id);
            statement.setString(2, comment);
            statement.setString(3, df.format(Calendar.getInstance().getTime()));

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBookmark(String comment){
        String query = "DELETE FROM bookmark WHERE comment=?;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            statement.setString(1, comment);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<String> selectAllBookmarks(){
        ObservableList<String> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM bookmark;";
        try (PreparedStatement statement = connection.prepareStatement(query)){

            ResultSet result = statement.executeQuery();
            while(result.next()){
                list.add("Plan " + result.getString(2)+ " - " + result.getString(3));
            }
            result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<ObservableList<String>> searchPlanByUser(ObservableList<String> userInput, String distance){
        PreparedStatement statement = null;
        try {
            String prequery = "SELECT id_plan, name, inhalt, distanz, count(id_plan) AS hits FROM plan_tag INNER JOIN tag ON plan_tag.id_tag=tag.id INNER JOIN plan ON id_plan=plan.id WHERE ";
            String postquery = " GROUP BY id_plan HAVING hits=? AND " + distance;
            for(int i = 0;  i < userInput.size(); i++){
                if(i > 0){
                    prequery += " OR ";
                }
                prequery += "name=\"" + userInput.get(i) + "\"";
            }
            statement = connection.prepareStatement(prequery + postquery);
            statement.setInt(1, userInput.size());
            ResultSet rs = statement.executeQuery();

            return Optional.of(makeListFromResult(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(statement != null) try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private ObservableList<String> makeListFromResult(ResultSet rs) throws SQLException {
        ObservableList<String> lst = FXCollections.observableArrayList();
        while(rs.next()){
            int maxLength = 100;
            String summary = rs.getString("inhalt");
            summary = summary.length() > maxLength ? summary.substring(0, maxLength)+ "\n..." : summary;

            lst.add(rs.getString(1) + " - " + summary);
        }
        rs.close();
        return lst;
    }

}
