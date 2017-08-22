package planer;

import datamodel.DatabaseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kai on 21.08.2017.
 * part of 80p - Codename Dolphin
 */
public class PlanUtils {

    public static boolean readFromFile(Path path) {
        try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            String content = "";
            int distance = 0;

            while (line != null){
                distance += calculateDistance(line);
                content += line + "\n";
                line = reader.readLine();
            }
            DatabaseHandler db = new DatabaseHandler();
            return db.addPlan(distance, content);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int calculateDistance(String in){
        int result = 0;
        Pattern p = Pattern.compile("(((\\d+)(\\s*[x*]){0,1}\\s*(\\d*))m)(.)*");
        Matcher m = p.matcher(in);

        if(m.matches()){
            int pre = Integer.parseInt(m.group(3));
            result = m.group(4) == null ? pre : pre * Integer.parseInt(m.group(5));
        }
        return result;
    }




}
