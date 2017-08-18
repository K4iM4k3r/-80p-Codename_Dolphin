package datamodel;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * Created by Kai on 18.08.2017.
 * part of 80p - Codename Dolphin
 */
public class DatabaseHandler {

    private final String driver = "org.sqlite.JDBC";
    private final String url = "jdbc:sqlite:."+ File.separator+"database"+File.separator+"database.sqlite";
    private Connection connection;

    public DatabaseHandler(){
        connect();
    }

    public void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (Exception e) {
            }
        }
    }

    public void connect() {
        try {
            Class.forName(this.driver);
            this.connection = DriverManager.getConnection(this.url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
