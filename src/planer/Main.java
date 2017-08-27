package planer;

import datamodel.DatabaseHandler;
import datamodel.Plan;
import datamodel.TagList;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private String projectname = "Project - Dolphin";
    private Stage actualStage;
    private Scene main;
    private Scene planview;
    private Scene tagEditor;
    private Button exit;
    private Button createPlan;
    private Label labelDistance;
    private VBox list;
    private int actID = -1;
    private int stateListView = 0;
    private DatabaseHandler db = new DatabaseHandler();
    private ObservableList<String> data = FXCollections.observableArrayList();
    private TagList tagList;
    private Label tagged;
    private Label errorLog;
    private ComboBox<String> tags;
    private ListView<String> searchview;


    @Override
    public void start(Stage stage) throws Exception {

        actualStage = stage;
        stage.setTitle(projectname);
        stage.getIcons().add(new Image("file:icons\\Logo - 64x64.png"));
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        stage.setWidth(600);
        stage.setHeight(500);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
        HBox tagControll = new HBox();
        tagged = new Label();
        Button addTag = new Button("add");

        tagList = db.getAllTagsOnPlan(actID);
        tagControll.setSpacing(10);
        tagControll.setAlignment(Pos.CENTER_RIGHT);
//        tagged.setText(tagList.toTagString());

        tags = new ComboBox<>(tagList.getData());
        tags.getSelectionModel().selectFirst();
        addTag.setOnAction((ActionEvent e) -> {
            String selectedTag = tags.getSelectionModel().getSelectedItem();
            tagList.changeTag(selectedTag);
            tagged.setText(tagList.toTagString());

        });

        tagControll.getChildren().addAll(tags, addTag);
        containerTags.setLeft(tagged);
        containerTags.setRight(tagControll);
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

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        /*
         *   Layout des Tageditors
         */
        HBox tagEditorBox = new HBox();
        VBox tagControllVertical = new VBox();
        HBox tagControlHorizontal = new HBox();
        ListView<String> allActTag = new ListView<>(tagList.getData());
        TextField inputField = new TextField();
        Label labelFeedback = new Label("Info:");
        Button changeTag = new Button("add");
        Button deleteTag = new Button("delete");
        Button exitTagScene = new Button("exit");

        deleteTag.setVisible(false);
        allActTag.setCellFactory((ListView<String> lv) -> {
            ListCell<String> listCell = new ListCell<>();
            SelectionModel<String> selectionModel = allActTag.getSelectionModel();
            listCell.textProperty().bind(listCell.itemProperty());
            listCell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                allActTag.requestFocus();
                if (!listCell.isEmpty()) {
                    int idx;
                    idx = listCell.getIndex();
                    if (selectionModel.getSelectedIndex() == idx) {
                        selectionModel.clearSelection();
                        inputField.setText("");
                        deleteTag.setVisible(false);
                        changeTag.setText("Add");
                    } else {
                        selectionModel.select(idx);
                        inputField.setText(selectionModel.getSelectedItem());
                        deleteTag.setVisible(true);
                        changeTag.setText("Change");
                    }
                    event.consume();
                }
            });
            return listCell;
        });
        changeTag.setOnAction(i -> {
            String userInput = inputField.getText();
            if(userInput.isEmpty()){
                labelFeedback.setText("Info: Ihre Eingabe ist leer");
            }
            else{
                if(allActTag.getSelectionModel().isEmpty()){
                    if(allActTag.getItems().contains(userInput)){
                        labelFeedback.setText("Info: Ihre Eingabe -" + userInput + "- existiert schon" );
                    }
                    else {
                        db.addUserTag(userInput);
                        labelFeedback.setText("Info: Sie haben den Tag "+ userInput +" erstellt");
                    }
                }
                else{
                    String selectedTag = allActTag.getSelectionModel().getSelectedItem();
                    db.updateUserTag(tagList.getId(selectedTag), userInput);
                    labelFeedback.setText("Info: Sie haben "+ selectedTag + " zu " + userInput + " geändert");
                    inputField.setText("");
                    deleteTag.setVisible(false);
                    changeTag.setText("Add");
                }
            }

            tagList.updateAllTag(db);
            allActTag.setItems(tagList.getData());
        });

        deleteTag.setOnAction(i -> {
            if(!allActTag.getSelectionModel().isEmpty()){
                db.deleteTag(tagList.getId(allActTag.getSelectionModel().getSelectedItem()));
                tagList.updateAllTag(db);
                allActTag.setItems(tagList.getData());
                inputField.setText("");
                deleteTag.setVisible(false);
                changeTag.setText("Add");

            }
        });

        exitTagScene.setOnAction(this::switchScene);
        inputField.setPromptText("your tag");
        tagControlHorizontal.getChildren().addAll(inputField, changeTag, deleteTag, exitTagScene);
        tagControllVertical.getChildren().addAll(tagControlHorizontal, labelFeedback);
        tagEditorBox.getChildren().addAll(allActTag, tagControllVertical);

        tagEditor = new Scene(tagEditorBox);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /* Layout des Main-Fensters
         *
         */
        BorderPane pane = new BorderPane();
        VBox verticalBox = new VBox();
        HBox shortCut = new HBox();
        HBox toggleItems = new HBox();
        verticalBox.setPadding(new Insets(15,15,15,15));
        verticalBox.setSpacing(15);
        shortCut.setSpacing(20);
        errorLog = new Label();
        Separator horsep = new Separator(Orientation.HORIZONTAL);
        horsep.setVisible(true);


        /*
         *   Menu
         */
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem menuNew = new MenuItem("New");
        MenuItem menuLoad = new MenuItem("Load");
        MenuItem menuOpen = new MenuItem("Open Random");
        Menu menuEdit = new Menu("Edit");
        MenuItem menuTag = new MenuItem("Tags erstellen");

        menuTag.setOnAction(i -> {
            actualStage.setScene(tagEditor);
        });
        menuEdit.getItems().add(menuTag);

        menuNew.setOnAction(i -> newPlan());
        menuLoad.setOnAction(this::loadPlan);
        menuOpen.setOnAction(i -> {
            db.getRandomPlan().ifPresent(this::openPlan);
        });
        menuFile.getItems().addAll(menuNew, menuLoad, menuOpen);
        menuBar.getMenus().addAll(menuFile, menuEdit);
        


        /*
         *  MainPane
         */
        Button open = new Button("open Plan");
        Button btn = new Button("Click me!");
        createPlan = new Button("new Plan");


        //ActionHandler für die Buttons
        createPlan.setOnAction(this::switchScene);
        open.setOnAction(i -> openPlan(2));
        btn.setOnAction(this::loadPlan);


        ToggleGroup toggleOptions = new ToggleGroup();
        ToggleButton showAllPlans = new ToggleButton("all Plans");
        ToggleButton showBookmarks = new ToggleButton("Bookmarks");

        showAllPlans.setToggleGroup(toggleOptions);
        showBookmarks.setToggleGroup(toggleOptions);

        toggleOptions.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if(newToggle == null){
                stateListView = 0;
                searchview.getItems().clear();
            }
            else if(newToggle.equals(showAllPlans)){
                stateListView = 1;
                data = db.selectAllPlan();
                searchview.setItems(data);
            }
            else {
                stateListView = 2;
                data = db.selectAllBookmarks();
                searchview.setItems(data);
            }
        });

        searchview = new ListView<>();
        searchview.setPrefSize(300, 400);
        searchview.setStyle("-fx-background-insets: 0 ;");

        searchview.setCellFactory(l -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu menu = new ContextMenu();
            MenuItem openItem = new MenuItem();

            openItem.textProperty().bind(Bindings.format("Open ", cell.itemProperty()));
            openItem.setOnAction((ActionEvent e ) -> {
                String input = cell.getItem();
                if(stateListView == 2){
                    input = input.replace("Plan ", "");
                }
                input = input.substring(0, input.indexOf(" -"));
                openPlan(Integer.parseInt(input));
            });

            MenuItem deleteItem = new MenuItem();

            deleteItem.textProperty().setValue("Delete");
            deleteItem.setOnAction((ActionEvent e) -> {


                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Löschen");
                alert.setContentText("Sind Sie sicher das der Eintrag gelöscht werden soll?");
                Optional<ButtonType> result = alert.showAndWait();

                if(result.isPresent() && result.get() == ButtonType.OK){
                    String input = cell.getItem();

                    if(stateListView == 1){
                        input = input.substring(0, input.indexOf(" -"));
                        int id = Integer.parseInt(input);
                        db.deletePlan(id);
                        searchview.getItems().remove(cell.getItem());
                        errorLog.setText("Plan mit der ID("+id+") gelöscht!");
                    }
                    else{
                        input = input.substring(input.indexOf(" - ") + 3);
                        db.removeBookmark(input);
                        searchview.getItems().remove(cell.getItem());
                        errorLog.setText("Das Lesezeichen ("+ input +") wurde gelöscht!");
                    }
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
                    if(stateListView == 2){
                        input = input.replace("Plan ", "");
                    }
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

        toggleItems.setSpacing(20.0);

        shortCut.getChildren().addAll(btn, createPlan, open);
        toggleItems.getChildren().addAll(showAllPlans, showBookmarks);
        verticalBox.getChildren().addAll(shortCut, horsep, searchview, menuBar, checkBox, toggleItems, errorLog);
//        verticalBox.add(btn,0,0 );
//        verticalBox.add(horsep,0 ,1,3,1);
//        verticalBox.add(createPlan, 1,0);
//        verticalBox.add(open, 2,0);
//        verticalBox.add(searchview, 0,3,3,2);
//        verticalBox.add(checkBox, 0,5);
//        verticalBox.add(errorLog,1,5);
//        verticalBox.add(menuBar,0,6);

        pane.setTop(menuBar);
        pane.setCenter(verticalBox);

        main = new Scene(pane);

        stage.setScene(main);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }


    private void switchScene(ActionEvent e){
        if(e.getSource().equals(createPlan)){
            newPlan();
        }
        else{
            actualStage.setScene(main);
        }
    }
    
    private void loadPlan(ActionEvent e){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TEXT", "*.txt"));
        File file = fileChooser.showOpenDialog(actualStage);
        if(file != null && file.isFile() && file.canRead()){
            String res = (PlanUtils.readFromFile(file.toPath()) ? " erfolgreich geladen!" : " konnte nicht geladen werden!");
            errorLog.setText(file.getName() + res );
            System.out.println(file.getAbsolutePath() + res );

        }
    }
    
    private void newPlan(){
        actID = -1;
        list.getChildren().clear();
        list.getChildren().add(addEmptyLine());
        labelDistance.setText("Distanz: ");
        actualStage.setScene(planview);
        tagList.clear();
        tagged.setText("");
        tags.setItems(tagList.getData());

    }

    private void openPlan(Plan plan){
        actID = plan.getId();
        list.getChildren().clear();
        labelDistance.setText("Distanz: " + plan.getDistance() + "m");
        tags.setItems(tagList.getData());
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
        errorLog.setText("open " + actID);


        actualStage.setScene(planview);
    }

    private void openPlan(int id){
        Optional<Plan> planOptional = db.selectPlan(id);
        planOptional.ifPresent(this::openPlan);
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
