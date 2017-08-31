package planer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import datamodel.DatabaseHandler;
import datamodel.Plan;

import java.io.*;
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
            return db.addPlan(distance, content) > 0;

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

    public static void exportAsTxt(File path, String output){
        try (BufferedWriter writer = Files.newBufferedWriter(path.toPath(), StandardCharsets.UTF_8)) {

            writer.write(output);
            writer.flush();
            System.out.println("Plan als txt exportiert: "+ path.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportAsPdf(File path, Plan plan){
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));

            document.open();

            Paragraph p = new Paragraph();
            p.setFont(new Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD));
//            p.add(input.getText().toString()+"\n");
            document.add(p);


            //Add paragraph to the document
            String[] content = plan.getContent().split("\n");

            for(int i = 0; i < content.length; i+=2){
                p = new Paragraph();
                p.setTabSettings(new TabSettings(65f));
                p.setFont(new Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL));
                p.add(new Chunk(content[i]));
//                p.add(Chunk.TABBING);
//                p.add(new Chunk(content.get(i+1)));
//                if(i != content.size()-1){
//                    p.add("\n\n");
//                }
                document.add(p);


            }
            p = new Paragraph();
            p.setTabSettings(new TabSettings(65f));
            p.setFont(new Font(Font.FontFamily.HELVETICA, 14f, Font.NORMAL));
            p.add("__________\n");
            p.add(Integer.toString(plan.getDistance()) + "m");
            document.add(p);

            p = new Paragraph();
//            p.add("\n\n\n\nSwimplan "+getActivity().getResources().getString(R.string.app_version) +" Plan ("+ id +")\n© Kai Schäfer");
            document.add(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Close document
        document.close();
    }
}
