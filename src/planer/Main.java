package planer;

import com.sun.javafx.image.IntPixelGetter;
import datamodel.DatabaseHandler;
import datamodel.Plan;
import datamodel.TagList;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private String projectname = "Project - Dolphin";
    private Stage actualStage;
    private Scene main;
    private Scene planview;
    private Button exit;
    private Button createPlan;
    private Label labelDistance;
    private VBox list;
    private int actID = -1;
    private DatabaseHandler db = new DatabaseHandler();
    private ObservableList<String> data = FXCollections.observableArrayList();
    private TagList tagList;
    private Label tagged;


    @Override
    public void start(Stage stage) throws Exception {

        actualStage = stage;
        stage.setTitle(projectname);
        stage.getIcons().add(new Image("file:icons\\Logo - 64x64.png"));
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setWidth(600);
        stage.setHeight(500);

        Label errorlog = new Label();

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
        TitledPane tagPane = new TitledPane();
        BorderPane containerTags = new BorderPane();
        tagged = new Label();
        Button addTag = new Button("add");

        tagList = db.getAllTagsOnPlan(actID);
//        tagged.setText(tagList.toTagString());

        ComboBox<String>  tags = new ComboBox<>(tagList.getData());
        tags.getSelectionModel().selectFirst();
        addTag.setOnAction((ActionEvent e) -> {
            String selectedTag = tags.getSelectionModel().getSelectedItem();
            tagList.changeTag(selectedTag);
            tagged.setText(tagList.toTagString());

        });

        containerTags.setLeft(tagged);
        containerTags.setCenter(tags);
        containerTags.setRight(addTag);
//        containerTags.getChildren().addAll(tagged, tags, addTag);
        tagPane.setText("Tags");
        tagPane.setContent(containerTags);



        Button savePlan = new Button("save");
        exit = new Button("exit");
        exit.setOnAction(this::switchScene);

        savePlan.setOnAction(this::savePlan);



        list.getChildren().addAll(addEmptyLine());
        scrollPane.setContent(list);
        actions.getChildren().addAll(savePlan, exit);
        footer.getChildren().addAll(tagPane, labelDistance, actions);
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
        open.setOnAction(i ->openPlan(2));


        Button btn = new Button("Click me!");
        btn.setOnAction((ActionEvent e)  ->  {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TEXT", "*.txt"));
            File file = fileChooser.showOpenDialog(stage);
            if(file != null && file.isFile() && file.canRead()){
                String res = (PlanUtils.readFromFile(file.toPath()) ? " erfolgreich geladen!" : " konnte nicht geladen werden!");
                errorlog.setText(file.getName() + res );
                System.out.println(file.getAbsolutePath() + res );

            }
        });


        ListView<String> searchview = new ListView<>();
        searchview.setPrefSize(300, 400);
        searchview.setStyle("-fx-background-insets: 0 ;");

        searchview.setCellFactory(l -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu menu = new ContextMenu();
            MenuItem openItem = new MenuItem();

            openItem.textProperty().bind(Bindings.format("Open ", cell.itemProperty()));
            openItem.setOnAction((ActionEvent e ) -> {
                String input = cell.getItem();
                input = input.substring(0, input.indexOf(" -"));
                openPlan(Integer.parseInt(input));
            });

            MenuItem deleteItem = new MenuItem();

            deleteItem.textProperty().setValue("Delete");
            deleteItem.setOnAction((ActionEvent e) -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Löschen");
                alert.setContentText("Sind Sie sicher das der Plan gelöscht werden soll?");
                Optional<ButtonType> result = alert.showAndWait();

                if(result.isPresent() && result.get() == ButtonType.OK){
                    String input = cell.getItem();
                    input = input.substring(0, input.indexOf(" -"));
                    int id = Integer.parseInt(input);
                    db.deletePlan(id);
                    searchview.getItems().remove(cell.getItem());
                    errorlog.setText("Plan mit der ID("+id+") gelöscht!");
                }
            });



            menu.getItems().setAll(openItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());

            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(menu);
                }
            });
            cell.setOnMousePressed(c -> {
                if(c.isPrimaryButtonDown()){
                    String input = searchview.getSelectionModel().getSelectedItem();
                    input = input.substring(0, input.indexOf(" -"));
                    openPlan(Integer.parseInt(input));
                }
            });

            return cell;
        });

        CheckBox checkBox = new CheckBox("alle Pläne anzeigen");
        checkBox.setOnAction(a ->{
            if( checkBox.isSelected()){
                data = db.selectAllPlan();
                searchview.setItems(data);
            }
            else{
                searchview.getItems().clear();
            }
        });

        grid.add(btn,0,0 );
        grid.add(horsep,0 ,1,3,1);
        grid.add(createPlan, 1,0);
        grid.add(open, 2,0);
        grid.add(searchview, 0,3,3,2);
        grid.add(checkBox, 0,5);
        grid.add(errorlog,1,5);

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
            list.getChildren().clear();
            list.getChildren().add(addEmptyLine());
            labelDistance.setText("Distanz: ");
            actualStage.setScene(planview);
        }
    }

    private void openPlan(int id){
        Optional<Plan> planOptional = db.selectPlan(id);

        if(planOptional.isPresent()) {
            Plan plan = planOptional.get();
            actID = plan.getId();
            list.getChildren().clear();
            labelDistance.setText("Distanz: " + plan.getDistance() + "m");
            String[] lines = plan.getContent().split("\n");
            Pattern p = Pattern.compile("(((\\d+)(\\s*[x*])?\\s*(\\d*))m)(.)*");
            for (String s : lines) {
                Matcher m = p.matcher(s);
                if (m.matches()) {
                    String distance = m.group(1);
                    String unit = s.substring(distance.length()+1);
                    list.getChildren().add(addLine(distance, unit));
                }
                else {
                    list.getChildren().add(addLine("", s));
                }

            }
            tagList = db.getAllTagsOnPlan(actID);
            tagged.setText(tagList.toTagString());

            actualStage.setScene(planview);
        }
    }

    private void savePlan(ActionEvent e){
        final int[] dis = {0};
        StringBuffer content = new StringBuffer();
        boolean update = false;

        list.getChildren().forEach(n -> {
            HBox hBox = (HBox) n;
            String unitDistance = ((TextField) hBox.getChildren().get(0)).getText();
            String unitPractice = ((TextField) hBox.getChildren().get(1)).getText();

            dis[0] += PlanUtils.calculateDistance(unitDistance);

            if(!unitPractice.isEmpty()){
                content.append(unitDistance).append(" ").append(unitPractice).append("\n");
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

            if(result.isPresent()){
                if (result.get() == buttonNew){
                    actID = db.addPlan(dis[0], content.toString());
                }
                else if (result.get() == buttonOverride){
                    db.updatePlan(actID, dis[0], content.toString());
                }
            }
        }
        else{
            actID = db.addPlan(dis[0], content.toString());
        }

        tagList.saveChanges(db, actID);
        tagList = db.getAllTagsOnPlan(actID);


        //TODO Toast ode ähnliches
    }

    private HBox addEmptyLine(){
        return addLine("", "");
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
//        btnAdd.setGraphic(new ImageView(new Image("file:icons\\addLine.png")));
        btnRemove.setText("remove");
//        btnRemove.setGraphic(new ImageView(new Image("file:icons\\deleteLine.png").));


        cell1.focusedProperty().addListener(((observable, oldValue, newValue) -> {
            if(!newValue){
                final int[] dis = {0};

                list.getChildren().forEach(n -> {
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
