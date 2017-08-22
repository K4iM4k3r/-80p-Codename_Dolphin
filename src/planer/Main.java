package planer;

import datamodel.DatabaseHandler;
import datamodel.Plan;
import datamodel.Uebung;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
    private String projectname = "Project - Dolphin";
    private TableView<Uebung> table = new TableView<>();
    private final ObservableList<Uebung> data =
            FXCollections.observableArrayList(
                    new Uebung("100m", "Einschwimmen"),
                    new Uebung("4x 50m", "Johnson")

            );


    @Override
    public void start(Stage stage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();

//        Scene scene = new Scene(new Group());
        stage.setWidth(450);
        stage.setHeight(500);
        stage.setTitle(projectname);
        stage.getIcons().add(new Image("file:Logo - 64x64.png"));

        Label errorlog = new Label("Hello World");

        Separator horsep = new Separator(Orientation.HORIZONTAL);
        horsep.setVisible(true);

        GridPane table = new GridPane();
        table.setGridLinesVisible(true);
//        table.setHgap(10);
//        table.setVgap(10);
        table.setPadding(new Insets(15,15,15,15));


        Button btnAddRow = new Button("add Line");
        btnAddRow.setOnAction((ActionEvent e)-> {

        });

        TextField cell1 = new TextField();
        TextField cell2 = new TextField();
        TextField cell3 = new TextField();
        TextField cell4 = new TextField();
        cell1.setManaged(true);
        cell1.setMinWidth(100);
        cell2.setManaged(true);
        cell2.setMinWidth(300);
        cell3.setManaged(true);
        cell4.setManaged(true);



        table.add(cell1,0,0);
        table.add(cell2,1,0);
        table.add(cell3,0,1);
        table.add(cell4,1,1);

        table.setStyle("-fx-background-color: #C0C0C0;");

        Scene create = new Scene(table);


        Button btnAddPlan = new Button("new Plan");
        btnAddPlan.setOnAction((ActionEvent e) ->{
            stage.setScene(create);
        });

        Button btn = new Button("Click me!");
        btn.setOnAction((ActionEvent e)  ->  {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if(file != null && file.isFile() && file.canRead()){
                errorlog.setText(file.getName() + (PlanUtils.readFromFile(file.toPath()) ? " erfolgreich geladen!" : " konnte nicht geladen werden!"));
                System.out.println(file.getAbsolutePath());

            }
        });




        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15,15,15,15));
//        grid.setGridLinesVisible(true);
//        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(btn,0,0 );
        grid.add(horsep,0 ,1);
        grid.add(errorlog,0,2);
        grid.add(btnAddPlan, 1,0);
        Scene scene = new Scene(grid, 500,500);
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(450);
        stage.show();

//
//        final Label label = new Label("Trainingsplan");
//        label.setFont(new Font("Arial", 20));
//
//        table.setEditable(true);
//
//
//        TableColumn dist = new TableColumn("Distanz");
//        dist.setMinWidth(100);
//        dist.setCellFactory(TextFieldTableCell.forTableColumn());
//        dist.setOnEditCommit(
//                new EventHandler<TableColumn.CellEditEvent<Uebung, String>>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent<Uebung, String> t) {
//                        ((Uebung) t.getTableView().getItems().get(
//                                t.getTablePosition().getRow())
//                        ).setDistanz(t.getNewValue());
//                    }
//                }
//        );
//        dist.setCellValueFactory(
//                new PropertyValueFactory<Uebung, String>("distanz"));
//
//        TableColumn ueb = new TableColumn("Uebung");
//        ueb.setMinWidth(200);
//        ueb.setSortable(false);
//        ueb.setCellValueFactory(
//                new PropertyValueFactory<Uebung, String>("uebung"));
//        ueb.setCellFactory(TextFieldTableCell.forTableColumn());
//        ueb.setOnEditCommit(
//                new EventHandler<TableColumn.CellEditEvent<Uebung, String>>() {
//                    @Override
//                    public void handle(TableColumn.CellEditEvent<Uebung, String> t) {
//                        ((Uebung) t.getTableView().getItems().get(
//                                t.getTablePosition().getRow())
//                        ).setUebung(t.getNewValue());
//                    }
//                }
//        );
//
//
//
//        table.setItems(data);
//        table.getColumns().addAll(dist, ueb);
//
//
//        final VBox vbox = new VBox();
//        vbox.setSpacing(5);
//        vbox.setPadding(new Insets(10, 0, 0, 10));
//        vbox.getChildren().addAll(label, table);
//        vbox.setFillWidth(true);
//
//
//        ((Group) scene.getRoot()).getChildren().addAll(vbox);
//        stage.setScene(scene);
//        stage.show();
//        DatabaseHandler db = new DatabaseHandler();


    }


    public static void main(String[] args) {
        launch(args);
    }
}
