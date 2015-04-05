/*
 * AutoPrimer3
 * Copyright (C) 2013,2014 David A. Parry
 * d.a.parry@leeds.ac.uk
 * https://github.com/gantzgraf/autoprimer3
 * 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.autoprimer3;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author David A. Parry <d.a.parry at your.uk.ac.leeds>
 */
public class AboutController implements Initializable {

    @FXML
    Button closeButton; 
    @FXML
    Label versionLabel;
    String VERSION;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
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
    
    public void setVersion(String version){
        VERSION = version;
        if (VERSION != null){
            versionLabel.setText("Version: " + VERSION);
        }
    }
}
