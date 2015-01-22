/*
 * Copyright (C) 2014 David A. Parry <d.a.parry@leeds.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.autoprimer3;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author david
 */
public class Primer3ResultViewController implements Initializable {

   
   @FXML
   MenuBar menuBar;
   @FXML
   TableView<Primer3Result> primerTable;
   @FXML
   TableColumn indexCol;
   @FXML
   TableColumn nameCol;
   @FXML
   TableColumn idCol;
   @FXML
   TableColumn leftPrimerCol;
   @FXML
   TableColumn rightPrimerCol;
   @FXML
   TableColumn productSizeCol;
   @FXML
   TableColumn regionCol;
   @FXML
   TableColumn ispcrCol;
   @FXML
   Button closeButton;
   @FXML 
   Label summaryLabel;
   @FXML
   TextArea designTextSummary;
   @FXML
   TextArea referenceTextArea;
   @FXML
   Tab refTab;
   @FXML
   ChoiceBox refChoiceBox;
   
   NumberFormat nf = NumberFormat.getNumberInstance();
   CoordComparator coordCompare = new CoordComparator();
   
   private final ObservableList<Primer3Result> data = FXCollections.observableArrayList();
   
   @Override
    public void initialize(URL url, ResourceBundle rb) {
        menuBar.setUseSystemMenuBar(true);
        indexCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Integer>("index"));
        leftPrimerCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("leftPrimer"));
        nameCol.setCellValueFactory(new
                PropertyValueFactory<Primer3Result, String>("name"));
        idCol.setCellValueFactory(new
                PropertyValueFactory<Primer3Result, String>("transcripts"));
        rightPrimerCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("rightPrimer"));
        productSizeCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Integer>("productSize"));
        regionCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, String>("region"));
        ispcrCol.setCellValueFactory(new 
                PropertyValueFactory<Primer3Result, Hyperlink>("isPcrLink"));
        primerTable.getSortOrder().add(indexCol);
        primerTable.getSortOrder().add(regionCol);
        primerTable.getSortOrder().add(productSizeCol);
        regionCol.setComparator(coordCompare);
        primerTable.getSelectionModel().setCellSelectionEnabled(true);
        primerTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refTab.selectedProperty().addListener(new ChangeListener<Boolean>(){
            @Override
            public void changed (ObservableValue ov, Boolean value, final Boolean newValue){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        refChoiceBox.setVisible(newValue);
                    }
                });
            }
        });
        
        MenuItem item = new MenuItem("Copy");
        item.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TablePosition> posList = primerTable.getSelectionModel().getSelectedCells();
                int old_r = -1;
                StringBuilder clipboardString = new StringBuilder();
                for (TablePosition p : posList) {
                    int r = p.getRow();
                    int c = p.getColumn();
                    Object cell = primerTable.getColumns().get(c).getCellData(r);
                    if (cell == null)
                        cell = "";
                    if (old_r == r)
                        clipboardString.append('\t');
                    else if (old_r != -1)
                        clipboardString.append('\n');
                    clipboardString.append(cell);
                    old_r = r;
                    
                }
                final ClipboardContent content = new ClipboardContent();
                content.putString(clipboardString.toString());
                Clipboard.getSystemClipboard().setContent(content);
            }
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(item);
        item.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        primerTable.setContextMenu(menu);
        
        
        closeButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent e){
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        Stage stage = (Stage) closeButton.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        });
        
    }
    
    public void displayData(ArrayList<Primer3Result> regions, 
            ArrayList<String> design, final HashMap<String, String> ref){
        int totalPairs = 0;
        int failures = 0;
        if (ref != null){
            refTab.setDisable(false);
            refChoiceBox.getItems().clear();
            refChoiceBox.getItems().addAll(ref.keySet());
            refChoiceBox.getSelectionModel().selectedIndexProperty().addListener
                (new ChangeListener<Number>(){
                @Override
                public void changed (ObservableValue ov, Number value, final Number new_value){ 
                    if (new_value.intValue() >= 0){
                        final String id = (String) refChoiceBox.getItems().get(new_value.intValue());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("Selecting " + id );
                                System.out.println("Seq =  " + ref.get(id) );
                                referenceTextArea.setText(ref.get(id));
                            }
                        });
                    }

                }
            });
            refChoiceBox.getSelectionModel().selectFirst();
        }else{
            refTab.setDisable(true);
        }
        StringBuilder designString = new StringBuilder();
        for (Primer3Result r: regions){
            data.add(r);
            primerTable.setItems(data);
            if (r.getProductSize() > 0){
                totalPairs++;
            }else{
                failures++;
            }
        }
        for (String s: design){
            designString.append(s).append("\n");
        }
        designTextSummary.setText(designString.toString());
        StringBuilder labelText = new StringBuilder (totalPairs + " primer pairs designed");
        if (failures > 0){
            labelText.append(". ").append(failures).append(" design");
            if (failures > 1){
                labelText.append("s");
            }
            labelText.append(" failed.");
        }
        summaryLabel.setText(labelText.toString());
    }
}
