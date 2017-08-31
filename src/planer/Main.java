package planer;

import datamodel.DatabaseHandler;
import datamodel.Distance;
import datamodel.Plan;
import datamodel.TagList;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends Application {
    private String projectname = "Project - Dolphin";
    private int actID = -1;
    private int stateListView = 0;
    private DatabaseHandler db = new DatabaseHandler();
    private ObservableList<String> data = FXCollections.observableArrayList();
    private TagList tagList;

    private Stage actualStage;
    private Scene main;
    private Scene planview;
    private Scene tagEditor;

    private Button createPlan;

    private Label labelDistance;
    private Label tagged;
    private Label errorLog;
    private Label infoSelectedItems;

    private VBox list;
    private ComboBox<String> tags;
    private TextField inputKeywords;
    private ToggleGroup toggleDistance;
    private ToggleGroup toggleOptions;
    private ListView<String> searchview;
    private ListView<String> allActTag;
    private ListView<String> selectionTags;
    private MenuItem menuItemExportPdf;
    private MenuItem menuItemExportTxt;


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
        VBox header = new VBox();
        HBox heading = new HBox();
        HBox actions = new HBox();
        list = new VBox();


        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        actions.setAlignment(Pos.CENTER);
        actions.setSpacing(10);
        actions.setPadding(new Insets(10,10,10,10));
        heading.setSpacing(50.0);

        // MenuBar
        MenuBar menuBarPlan = new MenuBar();
        Menu menuFilePlan = new Menu("File");

        MenuItem menuItemSave = new MenuItem("Save");
        menuItemExportPdf = new MenuItem("Export as PDF");
        menuItemExportTxt = new MenuItem("Export as TXT");
        MenuItem menuItemText = new MenuItem("Copy as Text");
        MenuItem menuItemSend = new MenuItem("Send EMail");

        menuItemSave.setOnAction(this::savePlan);
        menuItemExportPdf.setOnAction(this::exportPlan);
        menuItemExportTxt.setOnAction(this::exportPlan);
        menuItemText.setOnAction(this::copyAsText);
        menuItemSend.setOnAction(this::sendPlan);

        menuFilePlan.getItems().addAll(menuItemSave, menuItemExportPdf, menuItemExportTxt, menuItemText, menuItemSend);
        menuBarPlan.getMenus().add(menuFilePlan);


        // Überschrift
        Label headingDistance = new Label("Distanz");
        Label headingPractice = new Label("Übung");
        headingDistance.setFont(Font.font(14.0));
        headingPractice.setFont(Font.font(14.0));
        headingDistance.setMinWidth(100);
        headingPractice.setMinWidth(300);

        Label label = new Label("Ihr Trainingsplan");
        labelDistance = new Label("Distanz: ");
        //TagPane
        Accordion accordion = new Accordion();
        TitledPane tagPane = new TitledPane();
        BorderPane containerTags = new BorderPane();
        HBox tagControll = new HBox();
        tagged = new Label();
        Button addTag = new Button("add");

        tagList = db.getAllTagsOnPlan(actID);
        tagControll.setSpacing(10);
        tagControll.setAlignment(Pos.CENTER_RIGHT);

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
        tagPane.setText("Tags");
        tagPane.setContent(containerTags);

        // BookmarkPane
        TitledPane bookmarkPane = new TitledPane();
        HBox containerBookmark = new HBox();
        TextField inputComment = new TextField();
        Button btnAddBookmark = new Button("add");

        btnAddBookmark.setOnAction(i -> {
            if(!inputComment.getText().isEmpty()){
                db.addBookmark(actID, inputComment.getText());
            }
        });

        containerBookmark.getChildren().addAll(inputComment, btnAddBookmark);
        bookmarkPane.setText("Bookmark");
        bookmarkPane.setContent(containerBookmark);

        accordion.getPanes().addAll(tagPane, bookmarkPane);
        accordion.setExpandedPane(tagPane);

        Button savePlan = new Button("save");
        Button exit = new Button("exit");
        exit.setOnAction(this::switchScene);

        savePlan.setOnAction(this::savePlan);



        list.getChildren().addAll(addEmptyLine());
        scrollPane.setContent(list);
        heading.getChildren().addAll(headingDistance, headingPractice);
        actions.getChildren().addAll(savePlan, exit);
        header.getChildren().addAll(menuBarPlan, label, heading);
        footer.getChildren().addAll(accordion, labelDistance, actions);
        borderPane.setTop(header);
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
        allActTag = new ListView<>(tagList.getData());
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

            refreshAllElementWithData();
        });

        deleteTag.setOnAction(i -> {
            if(!allActTag.getSelectionModel().isEmpty()){
                db.deleteTag(tagList.getId(allActTag.getSelectionModel().getSelectedItem()));
                refreshAllElementWithData();
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
        VBox footerMainPane = new VBox();
        HBox shortCut = new HBox();
        HBox toggleItems = new HBox();
        VBox toggleDistanceItems = new VBox();
        verticalBox.setPadding(new Insets(15,15,15,15));
        verticalBox.setSpacing(15);
        shortCut.setSpacing(20);
        footerMainPane.setPadding(new Insets(15,15,15,15));
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

        menuTag.setOnAction(i -> actualStage.setScene(tagEditor));
        menuEdit.getItems().add(menuTag);

        menuNew.setOnAction(i -> newPlan());
        menuLoad.setOnAction(this::loadPlan);
        menuOpen.setOnAction(i -> db.getRandomPlan().ifPresent(this::openPlan));
        menuFile.getItems().addAll(menuNew, menuLoad, menuOpen);
        menuBar.getMenus().addAll(menuFile, menuEdit);
        


        /*
         *  MainPane
         */
//        Button open = new Button("open Plan");
//        Button btn = new Button("Click me!");
//        Button btnExport = new Button("export");
//        createPlan = new Button("new Plan");


        //ActionHandler für die Buttons
//        createPlan.setOnAction(this::switchScene);
//        open.setOnAction(i -> openPlan(2));
//        btn.setOnAction(this::loadPlan);
//        btnExport.setOnAction(i -> {
//
//
//        });



        /*
         *  SearchPane
         */
        BorderPane search = new BorderPane();
        Button btnSearch = new Button("search");
        selectionTags = new ListView<>(tagList.getData());
        infoSelectedItems = new Label();
        inputKeywords = new TextField();
        toggleDistance = new ToggleGroup();
        Label labelToggleDistance = new Label("Distanz:");
        RadioButton notImportant = new RadioButton("egal");
        RadioButton lessThanTwo = new RadioButton("bis 2km");
        RadioButton betweenTwoAndThree = new RadioButton("ab 2km bis 3 km");
        RadioButton betweenThreeAndFour = new RadioButton("ab 3km bis 4km");
        RadioButton greaterThanFour = new RadioButton("ab 4km");

        lessThanTwo.setToggleGroup(toggleDistance);
        betweenTwoAndThree.setToggleGroup(toggleDistance);
        betweenThreeAndFour.setToggleGroup(toggleDistance);
        greaterThanFour.setToggleGroup(toggleDistance);
        notImportant.setToggleGroup(toggleDistance);

        notImportant.setSelected(true);

        notImportant.setUserData(Distance.EMPTY);
        lessThanTwo.setUserData(Distance.LOW);
        betweenTwoAndThree.setUserData(Distance.SHORT);
        betweenThreeAndFour.setUserData(Distance.MEDIUM);
        greaterThanFour.setUserData(Distance.LONG);
        toggleDistance.selectedToggleProperty().addListener((observable, oldValue, newValue) -> generateSearchLabel());

        inputKeywords.setPromptText("Freitextsuche");
        inputKeywords.setOnKeyPressed(event -> {
            if(event.getCode().equals(KeyCode.ENTER)){
                searchKeyword();
            }
        });
        inputKeywords.setOnKeyReleased(event -> {
            String input = inputKeywords.getText();
            if(!input.isEmpty()){
                infoSelectedItems.setText("Suche nach " + input);
            }
            else {
                infoSelectedItems.setText("");
            }
        });

        toggleDistanceItems.getChildren().addAll(labelToggleDistance, notImportant, lessThanTwo, betweenTwoAndThree, betweenThreeAndFour, greaterThanFour);

        selectionTags.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        selectionTags.setMaxHeight(250.0);
        selectionTags.setMaxWidth(120.0);
        selectionTags.setMinHeight(100.0);
        selectionTags.setCellFactory((ListView<String> lv) -> {
            ListCell<String> listCell = new ListCell<>();
            MultipleSelectionModel<String> selectionModel = selectionTags.getSelectionModel();
            listCell.textProperty().bind(listCell.itemProperty());
            listCell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                allActTag.requestFocus();
                if (!listCell.isEmpty()) {
                    int idx;
                    idx = listCell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(idx)) {
                        selectionModel.clearSelection(idx);
                    } else {
                        selectionModel.select(idx);
                    }
                    generateSearchLabel();
                    event.consume();
                }
            });
            return listCell;
        });

        btnSearch.setOnAction(this::search);

        Insets spacing = new Insets(10.0,10.0,0.0,10.0);
        BorderPane.setMargin(inputKeywords, spacing);
        BorderPane.setMargin(selectionTags, spacing);
        BorderPane.setMargin(toggleDistanceItems, spacing);
        BorderPane.setMargin(btnSearch, spacing);
        BorderPane.setAlignment(btnSearch, Pos.CENTER_LEFT);

        search.setTop(inputKeywords);
        search.setLeft(selectionTags);
        search.setCenter(toggleDistanceItems);
        search.setRight(btnSearch);
        search.setBottom(infoSelectedItems);


        /*
         * ToggleGroup AllBookmarks or AllPlan
         */


        toggleOptions = new ToggleGroup();
        ToggleButton showAllPlans = new ToggleButton("all Plans");
        ToggleButton showBookmarks = new ToggleButton("Bookmarks");

        showAllPlans.setToggleGroup(toggleOptions);
        showBookmarks.setToggleGroup(toggleOptions);
        showAllPlans.requestFocus();

        toggleOptions.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            if(newToggle == null){
                stateListView = 0;
                searchview.getItems().clear();
                errorLog.setText("");
            }
            else if(newToggle.equals(showAllPlans)){
                stateListView = 1;
                data = db.selectAllPlan();
                searchview.setItems(data);
                errorLog.setText("Anzahl Pläne: "+ data.size());
            }
            else {
                stateListView = 2;
                data = db.selectAllBookmarks();
                searchview.setItems(data);
                errorLog.setText("Anzahl Bookmarks: "+ data.size());
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
                    if(input != null){
                        if(stateListView == 2){
                            input = input.replace("Plan ", "");
                        }
                        input = input.substring(0, input.indexOf(" -"));
                        openPlan(Integer.parseInt(input));
                    }
                }
            });

            return cell;
        });


        toggleItems.setSpacing(15.0);

//        shortCut.getChildren().addAll(btn, createPlan, open, btnExport);
        toggleItems.getChildren().addAll(showAllPlans, showBookmarks);
        verticalBox.getChildren().addAll(search, horsep, searchview);
        footerMainPane.getChildren().addAll(toggleItems, errorLog);
        pane.setTop(menuBar);
        pane.setCenter(verticalBox);
        pane.setBottom(footerMainPane);

        main = new Scene(pane);

        stage.setOnCloseRequest(event -> db.close());
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

    private void exportPlan(ActionEvent e){

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Plan");
        FileChooser.ExtensionFilter filter =
                e.getSource().equals(menuItemExportPdf) ?
                        new FileChooser.ExtensionFilter("PDF", "*.pdf") :
                        new FileChooser.ExtensionFilter("TXT", "*.txt");

        fileChooser.getExtensionFilters().add(filter);
        File selectedFile = fileChooser.showSaveDialog(actualStage);
        if(selectedFile != null){
            if(e.getSource().equals(menuItemExportPdf)){
                db.selectPlan(actID).ifPresent( p -> PlanUtils.exportAsPdf(selectedFile, p));
            }
            else if(e.getSource().equals(menuItemExportTxt)){
                PlanUtils.exportAsTxt(selectedFile, generatePlanString());
            }
        }
    }

    private HBox addEmptyLine(){
        return addLine("", "");
    }

    private HBox addLine(String distance, String unit){
        HBox row = new HBox();

        TextField cell1 = new TextField();
        TextField cell2 = new TextField();
        Button btnAdd = new Button();
        Button btnRemove = new Button();

        cell1.setMinWidth(100);
        cell2.setMinWidth(300);
        cell1.setText(distance);
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

    private void refreshAllElementWithData(){
        tagList.updateAllTag(db);
        allActTag.setItems(tagList.getData());
        selectionTags.setItems(tagList.getData());
    }

    private void search(ActionEvent e){
        ObservableList<String> items = selectionTags.getSelectionModel().getSelectedItems();
        String clause =  ((Distance) toggleDistance.getSelectedToggle().getUserData()).getClause();
        if(!items.isEmpty() || !clause.isEmpty() ){
            System.out.println("Suche ...");
            Toggle toggle = toggleOptions.getSelectedToggle();
            if(toggle != null) toggle.setSelected(false);
            db.searchPlanByUser(items, clause).ifPresent(searchview::setItems);
            errorLog.setText("Treffer: " + searchview.getItems().size());
        }
        else if(!inputKeywords.getText().isEmpty()){
            searchKeyword();
        }
    }

    private void searchKeyword(){
        String input = inputKeywords.getText();
        if(!input.isEmpty()){
            db.searchByUserKeyword(input).ifPresent(searchview::setItems);
            errorLog.setText("Treffer: " + searchview.getItems().size());
        }
    }

    private void generateSearchLabel(){
        StringBuilder s = new StringBuilder("Suche nach: ");
        for(String item : selectionTags.getSelectionModel().getSelectedItems()){
            s.append(item).append(", ");
        }
        if(s.length()> 1)        s.delete(s.length()-2, s.length());
        String dis = ((Distance) toggleDistance.getSelectedToggle().getUserData()).getInformation();
        if (!dis.isEmpty()){
            s.append(" - ").append(dis);
        }
        infoSelectedItems.setText(s.toString());
    }

    private void sendPlan(ActionEvent e){
        getHostServices().showDocument("mailto:%20?subject=My%20Plan&body=" + generatePlanString(true));
    }

    private String generatePlanString(){
        return generatePlanString(false);
    }

    private String generatePlanString(boolean mail){
        String spacing = mail ? "%20" : " ";
        String linebreak = mail ? "%0d%0a" : "\n";
        StringBuilder result = new StringBuilder();
        for(Node node :  list.getChildren()){
            HBox row = (HBox) node;
            TextField distance = (TextField) row.getChildren().get(0);
            TextField practice = (TextField) row.getChildren().get(1);
            String stringDistance = distance.getText();
            String stringPractice = practice.getText();
            if (mail){
                stringDistance = stringDistance.replaceAll(" ", spacing);
                stringPractice = stringPractice.replaceAll(" ", spacing);
            }

            result.append(stringDistance).append(spacing).append(stringPractice).append(linebreak);
        }
        return result.toString();
    }

    private void copyAsText(ActionEvent e){
        String plan = generatePlanString();
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(plan);
        clipboard.setContent(clipboardContent);
    }

}
