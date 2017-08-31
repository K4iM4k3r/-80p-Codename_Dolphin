package planer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import datamodel.DatabaseHandler;
import datamodel.Uebung;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kai on 21.08.2017.
 * part of 80p - Codename Dolphin
 */
class PlanUtils {

    static boolean readFromFile(Path path) {
        try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            StringBuilder content = new StringBuilder();
            int distance = 0;

            while (line != null){
                distance += calculateDistance(line);
                content.append(line).append("\n");
                line = reader.readLine();
            }
            DatabaseHandler db = new DatabaseHandler();
            return db.addPlan(distance, content.toString()) > 0;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    static int calculateDistance(String in){
        int result = 0;
        Pattern p = Pattern.compile("(((\\d+)(\\s*[x*]){0,1}\\s*(\\d*))m)(.)*");
        Matcher m = p.matcher(in);

        if(m.matches()){
            int pre = Integer.parseInt(m.group(3));
            result = m.group(4) == null ? pre : pre * Integer.parseInt(m.group(5));
        }
        return result;
    }

    static void exportAsTxt(File path, String output){
        try (BufferedWriter writer = Files.newBufferedWriter(path.toPath(), StandardCharsets.UTF_8)) {

            writer.write(output);
            writer.flush();
            System.out.println("Plan als txt exportiert: "+ path.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void exportAsPdf(File path, List<Uebung> plan, String distance){
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));

            document.open();

            Paragraph p = new Paragraph();
            p.setFont(new Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD));
            p.add("Trainingsplan \n");
            document.add(p);

            TabSettings rowsSettings = new TabSettings(65f);
            Font rowsFont = new Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL);

            for(Uebung ueb : plan){
                p = new Paragraph();
                p.setTabSettings(rowsSettings);
                p.setFont(rowsFont);

                p.add(new Chunk(ueb.getDistance()));
                p.add(Chunk.TABBING);
                p.add(new Chunk(ueb.getPractice()));

                if(plan.lastIndexOf(ueb) != plan.size()-1)    p.add("\n\n");

                document.add(p);
            }

            p = new Paragraph();
            p.setTabSettings(rowsSettings);
            p.setFont(rowsFont);
            p.add("__________\n");
            p.add(distance);
            document.add(p);

            p = new Paragraph();
            document.add(p);
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        }

        document.close();
    }
}
