package it.polito.tdp.metroparis;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.metroparis.model.Fermata;
import it.polito.tdp.metroparis.model.Model;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MetroController {
	
	private Model model;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ComboBox<Fermata> boxArrivo;

    @FXML
    private ComboBox<Fermata> boxPartenza;

    @FXML
    private TextArea txtResult;
    
    @FXML
    private TableColumn<Fermata, String> colFermata; // il primo generic è sempre uguale a quello della Table, il secondo rappresenta il tipo di dato visualizzato

    @FXML
    private TableView<Fermata> tbPercorso; // ciascuna riga rappresenta oggetti di tipo Fermata

    @FXML
    void handleCerca(ActionEvent event) {
    	
    	Fermata partenza = boxPartenza.getValue();
    	Fermata arrivo = boxArrivo.getValue();
    	
    	if(partenza!=null && arrivo!=null && !partenza.equals(arrivo)) { // piccolo controllo sui dati in input
    		List<Fermata> percorso = model.calcolaPercorso(partenza, arrivo);
    		
    		tbPercorso.setItems(FXCollections.observableArrayList(percorso)); // ricordare che gli items della tabella accettano solo OsservableList
    		txtResult.setText("Percorso trovato con "+percorso.size()+" stazioni");
    	}else {
    		txtResult.setText("Devi selezionare due stazioni diverse tra loro\n");
    	}
    }

    @FXML
    void initialize() {
        assert boxArrivo != null : "fx:id=\"boxArrivo\" was not injected: check your FXML file 'Metro.fxml'.";
        assert boxPartenza != null : "fx:id=\"boxPartenza\" was not injected: check your FXML file 'Metro.fxml'.";
        assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'Metro.fxml'.";
        
        // istruisco la colonna su come estrarre dall'oggetto di interesse il dato da visualizzare
        colFermata.setCellValueFactory(new PropertyValueFactory<Fermata,String>("nome"));
    }

	public void setModel(Model m) {
		// TODO Auto-generated method stub
		this.model = m;
		// non appena conosco qual è il modello, chiedo le fermate per popolare le comboBox
		List<Fermata> fermate = this.model.getFermate();
		boxPartenza.getItems().addAll(fermate); // getItems è una collection inizialmente vuota a cui "travaso" dentro la lista delle fermate ottenuta dal DB
		boxArrivo.getItems().addAll(fermate);
	}

}
