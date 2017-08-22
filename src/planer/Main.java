package planer;

import datamodel.DatabaseHandler;
import datamodel.Plan;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private String projectname = "Project - Dolphin";
//    private TableView<Uebung> table = new TableView<>();
//    private final ObservableList<Uebung> data =
//            FXCollections.observableArrayList(
//                    new Uebung("100m", "Einschwimmen"),
//                    new Uebung("4x 50m", "Johnson")
//
//            );
    private Stage actualStage;
    private Scene main;
    private Scene planview;
    private Button exit;
    private Button createPlan;
    private Label labelDistance;
    private VBox list;
    private int actID = -1;


    @Override
    public void start(Stage stage) throws Exception {

        actualStage = stage;
        stage.setTitle(projectname);
        stage.getIcons().add(new Image("file:Logo - 64x64.png"));
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setWidth(600);
        stage.setHeight(500);

        Label errorlog = new Label("Hello World");

        Separator horsep = new Separator(Orientation.HORIZONTAL);
        horsep.setVisible(true);


        /* Layout des Planeditors
         *
         */
        BorderPane borderPane = new BorderPane();
        ScrollPane scrollPane = new ScrollPane();
        VBox footer = new VBox();
        HBox actions = new HBox();
        list = new VBox();



        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(10);
        actions.setPadding(new Insets(10,10,10,10));

        Label label = new Label("Ihr Trainingsplan");
        labelDistance = new Label("Distanz: ");
        Button savePlan = new Button("save");
        exit = new Button("exit");
        exit.setOnAction(this::switchScene);





        savePlan.setOnAction((ActionEvent e) -> {

            DatabaseHandler db = new DatabaseHandler();
            final int[] dis = {0};
            StringBuffer content = new StringBuffer();

            list.getChildren().stream().forEach(n -> {
                HBox hBox = (HBox) n;
                String unitDistance = ((TextField) hBox.getChildren().get(0)).getText();
                String unitPractice = ((TextField) hBox.getChildren().get(1)).getText();

                dis[0] += PlanUtils.calculateDistance(unitDistance);

                if(!unitPractice.isEmpty()){
                    content.append(unitDistance + " " + unitPractice + "\n");
                }


            });

            if(actID != -1){
                Alert dialogAlert = new Alert(Alert.AlertType.CONFIRMATION);
                dialogAlert.setTitle("Überschreiben?");
                dialogAlert.setHeaderText("Alten Plan überschreiben?");
                dialogAlert.setContentText("Oder als einen neuen Plan anlegen");

                ButtonType buttonNew = new ButtonType("Neuen");
                ButtonType buttonOverride = new ButtonType("Überschreiben");
                ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                dialogAlert.getButtonTypes().setAll(buttonNew, buttonOverride, buttonCancel);

                Optional<ButtonType> result = dialogAlert.showAndWait();


                if (result.get() == buttonNew){
                    actID = db.addPlan(dis[0], content.toString());
                }
                else if (result.get() == buttonOverride){
                    db.updatePlan(actID, dis[0], content.toString());

                }
            }
            else{
                actID = db.addPlan(dis[0], content.toString());
            }


            //TODO Toast ode ähnliches
        });


        list.getChildren().addAll(addEmptyLine());
        scrollPane.setContent(list);
        actions.getChildren().addAll(savePlan, exit);
        footer.getChildren().addAll(labelDistance, actions);
        borderPane.setTop(label);
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(footer);

        planview = new Scene(borderPane);





        /* Layout des Main-Fensters
         *
         */
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15,15,15,15));
//        grid.setGridLinesVisible(true);
//        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        Button open = new Button("open Plan");
        createPlan = new Button("new Plan");
        createPlan.setOnAction(this::switchScene);

        open.setOnAction((ActionEvent e) -> {
            DatabaseHandler db = new DatabaseHandler();
            Optional<Plan> planOptional = db.selectPlan(2);

            if(planOptional.isPresent()) {
                Plan plan = planOptional.get();
                actID = plan.getId();
                list.getChildren().clear();
                labelDistance.setText("Distanz: " + plan.getDistance() + "m");
                String[] lines = plan.getContent().split("\n");
                Pattern p = Pattern.compile("(((\\d+)(\\s*[x*]){0,1}\\s*(\\d*))m)(.)*");
                for (String s : lines) {
                    Matcher m = p.matcher(s);
                    if (m.matches()) {
                        String distance = m.group(1);
                        String unit = s.substring(distance.length());
                        list.getChildren().add(addLine(distance, unit));
                    }
                    else {
                        list.getChildren().add(addLine("", s));
                    }

                }

                stage.setScene(planview);
                

            }
        });

        Button btn = new Button("Click me!");
        btn.setOnAction((ActionEvent e)  ->  {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if(file != null && file.isFile() && file.canRead()){
                String res = (PlanUtils.readFromFile(file.toPath()) ? " erfolgreich geladen!" : " konnte nicht geladen werden!");
                errorlog.setText(file.getName() + res );
                System.out.println(file.getAbsolutePath() + res );

            }
        });

        grid.add(btn,0,0 );
        grid.add(horsep,0 ,1);
        grid.add(errorlog,0,2);
        grid.add(createPlan, 1,0);
        grid.add(open, 2,0);

        main = new Scene(grid);

        stage.setScene(main);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    private void switchScene(ActionEvent e){
        if(e.getSource().equals(exit)){
            actualStage.setScene(main);
        }
        else{
            actualStage.setScene(planview);
        }
    }

    private HBox addEmptyLine(){
        HBox row = new HBox();

        TextField cell1 = new TextField();
        TextField cell2 = new TextField();
        Button btnAdd = new Button();
        Button btnRemove = new Button();

        cell1.setMinWidth(100);
        cell2.setMinWidth(300);
        btnAdd.setText("new Line");
        btnRemove.setText("remove");

        cell1.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue){
                final int[] dis = {0};

                list.getChildren().stream().forEach(n -> {
                    HBox hBox = (HBox) n;
                    TextField textField = (TextField) hBox.getChildren().get(0);
                    dis[0] += PlanUtils.calculateDistance(textField.getText());

                });

                labelDistance.setText("Distanz: " + dis[0] +"m");
            }
        }));

        btnAdd.setOnAction((ActionEvent e) ->{
            int index = list.getChildren().indexOf(row);
            list.getChildren().add(index+1, addEmptyLine());

        });

        btnRemove.setOnAction((ActionEvent e) -> {
            ObservableList<Node> childs = list.getChildren();
            if(childs.size() == 1){
                cell1.setText("");
                cell2.setText("");
            }
            else {
                childs.remove(row);
            }
        });

        row.getChildren().addAll(cell1, cell2, btnAdd, btnRemove);

        HBox.setHgrow(cell2, Priority.ALWAYS);
        return row;
    }
    private HBox addLine(String distace, String unit){
        HBox row = new HBox();

        TextField cell1 = new TextField();
        TextField cell2 = new TextField();
        Button btnAdd = new Button();
        Button btnRemove = new Button();

        cell1.setMinWidth(100);
        cell2.setMinWidth(300);
        cell1.setText(distace);
        cell2.setText(unit);
        btnAdd.setText("new Line");
        btnRemove.setText("remove");

        cell1.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue){
                final int[] dis = {0};

                list.getChildren().stream().forEach(n -> {
                    HBox hBox = (HBox) n;
                    TextField textField = (TextField) hBox.getChildren().get(0);
                    dis[0] += PlanUtils.calculateDistance(textField.getText());

                });

                labelDistance.setText("Distanz: " + dis[0] +"m");
            }
        }));

        btnAdd.setOnAction((ActionEvent e) ->{
            int index = list.getChildren().indexOf(row);
            list.getChildren().add(index+1, addEmptyLine());

        });

        btnRemove.setOnAction((ActionEvent e) -> {
            ObservableList<Node> childs = list.getChildren();
            if(childs.size() == 1){
                cell1.setText("");
                cell2.setText("");
            }
            else {
                childs.remove(row);
            }
        });

        row.getChildren().addAll(cell1, cell2, btnAdd, btnRemove);

        HBox.setHgrow(cell2, Priority.ALWAYS);
        return row;
    }

}
