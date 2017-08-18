package planer;

import datamodel.DatabaseHandler;
import datamodel.Plan;
import datamodel.Uebung;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
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
        Scene scene = new Scene(new Group());
        stage.setWidth(450);
        stage.setHeight(500);

        final Label label = new Label("Trainingsplan");
        label.setFont(new Font("Arial", 20));

        table.setEditable(true);


        TableColumn dist = new TableColumn("Distanz");
        dist.setMinWidth(100);
        dist.setCellFactory(TextFieldTableCell.forTableColumn());
        dist.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Uebung, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Uebung, String> t) {
                        ((Uebung) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setDistanz(t.getNewValue());
                    }
                }
        );
        dist.setCellValueFactory(
                new PropertyValueFactory<Uebung, String>("distanz"));

        TableColumn ueb = new TableColumn("Uebung");
        ueb.setMinWidth(200);
        ueb.setSortable(false);
        ueb.setCellValueFactory(
                new PropertyValueFactory<Uebung, String>("uebung"));
        ueb.setCellFactory(TextFieldTableCell.forTableColumn());
        ueb.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Uebung, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Uebung, String> t) {
                        ((Uebung) t.getTableView().getItems().get(
                                t.getTablePosition().getRow())
                        ).setUebung(t.getNewValue());
                    }
                }
        );



        table.setItems(data);
        table.getColumns().addAll(dist, ueb);


        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table);
        vbox.setFillWidth(true);


        ((Group) scene.getRoot()).getChildren().addAll(vbox);
        stage.setScene(scene);
        stage.show();
        DatabaseHandler db = new DatabaseHandler();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
