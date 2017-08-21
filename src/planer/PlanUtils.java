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
            Pattern p = Pattern.compile("(((\\d+)(\\s*[x*]){0,1}\\s*(\\d*))m)(.)*");
            String line = reader.readLine();
            String content = "";
            int distance = 0;

            while (line != null){
                Matcher m = p.matcher(line);

                if(m.matches()){
                    int pre = Integer.parseInt(m.group(3));
                    distance += m.group(4) == null ? pre : pre * Integer.parseInt(m.group(5));
                }
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




}
