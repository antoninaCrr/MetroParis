package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
 
	private List<Fermata> fermate;
	private Map<Integer,Fermata> fermateIdMap;
	private Graph<Fermata,DefaultEdge> grafo;
	// se l'oggetto fermata non avesse i metodi hashCode e equals correttamente definiti non avremmo potuto inserire oggetti di tale tipo nel Set dei vertici
	
	public List<Fermata> getFermate(){
	  if(this.fermate == null) { // se fermate non è inizializzato allora interrogo il DB, altrimenti restituisco ciò che ho già ottenuto da interrogazione precedente
		MetroDAO dao = new MetroDAO();
	    this.fermate = dao.getAllFermate(); // memorizzo il risultato in una proprietà della classe e non in una var. locale (List<Fermata> fermata =) così da poter riutilizzare il risultato nel metodo creaGrafo
	    
	    this.fermateIdMap = new HashMap<Integer,Fermata>(); // struttura dati d'appoggio in più
		for(Fermata f : fermate) { // popolo subito la mappa grazie alla lista riempita dal metodo del DAO che si interfaccia con il DB
			fermateIdMap.put(f.getIdFermata(), f);
		}
	  }
	    return this.fermate;
	}
	
	public List<Fermata> calcolaPercorso(Fermata partenza, Fermata arrivo){
		creaGrafo();
		Map<Fermata,Fermata> alberoInverso = visitaGrafo(partenza);
		
		// dall'albero inverso devo estrarre la serie di fermate attraversate per giungere dalla partenza all'arrivo
		Fermata corrente = arrivo;
		List<Fermata> percorso = new ArrayList<Fermata>();
		
		while(corrente!=null) { // è il predecessore del nodo di partenza
			
			percorso.add(0,corrente); // la prima volta che entro nel ciclo corrente è inizializzato ad arrivo
									  // aggiungo in testa e non in coda, se vi sono tanti elementi usare per questa operazione una LinkedList
			corrente = alberoInverso.get(corrente);
		}
		return percorso;
	}
	
	// il controller non ha bisogno di sapere che dietro c'è un grafo d'appoggio, quindi posso rendere il metodo di creazione private
	private void  creaGrafo() {
		// potrei mettere una if iniziale per evitare di ricalcolare il grafo qualuno fosse già stato fatto
		
		// leggerà le info dal DB e popolerà il grafo stesso
		// step 1-devo istanziare il grafo stesso ( metto la new nel met. di creazione poichè potrebbe cambiare sulla base delle richieste degli utenti)
		this.grafo = new SimpleDirectedGraph<Fermata,DefaultEdge>(DefaultEdge.class);
		
		// posso iterare sulla lista e usare il metodo this.grafo.addVertex(fi) oppure un metodo di utilità
		//Graphs.addAllVertices(this.grafo, this.fermate); // questa istruzione funziona solo se sono sicura che qualcuno ha chiamato il metodo getFermate()
		Graphs.addAllVertices(this.grafo, getFermate()); // così sono sicura che funzionerà sempre (programmazione difensiva)
		
		MetroDAO dao = new MetroDAO();
	    
		// come creo gli archi?
		// modo 1: considero la def. secondo la quale tra due fermate c'è un arco se esiste una linea
		// in sintesi itero su ogni coppia di vertici
		/*for(Fermata partenza: fermate) {
			for(Fermata arrivo: fermate) {
				if(dao.isFermateConnesse(partenza, arrivo)) // esiste almeno una connessione tra partenza e arrivo?
					this.grafo.addEdge(partenza, arrivo);
			    // si tratta di un metodo molto semplice ma prevede un numero di accessi al DB pari al quad del numero di vertici del nostro grafo
			
			}
		}*/
		
		// modo 2: dato ciascun vertice, trova i vertici ad esso adiacenti
		/*for(Fermata partenza : fermate) {
			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
			for(Integer id : idConnesse) {
				Fermata arrivo = null;
				for(Fermata f : fermate) { // scandisco la lista alla ricerca dell'elemento che ha l'id di mio interesse
				// avrei anche potuto iterare anche in this.grafo.vertexSet()
				// ma non in dao.getAllFermate() in quanto avrei sprecato un tempo computazionale maggiore
				// rispetto a quello impiegato per la ricerca in una struttura dati
					if(f.getIdFermata()==id) {
						arrivo = f;
						break;
					}
				}
				this.grafo.addEdge(partenza, arrivo);
			}
		}*/
		
		// modo 2B: il DAO restituisce un elenco di oggetti Fermata
		/*for(Fermata partenza : fermate) { // ciclo esterno su tutti i vertici
			List<Fermata> arrivi = dao.getFermateConnesse(partenza);
			for(Fermata arrivo : arrivi) { // ciclo interno sui vertici adiacenti
				this.grafo.addEdge(partenza, arrivo); // sto passando due oggetti di tipo Fermata
				// il primo oggetto esiste giò all'interno del grafo
				// il secondo non esiste ancora ma conta solo che sia equals ad un oggetto già presente
			}
		}*/
		
		// modo 2C : il DAO restituisce un elenco di ID numerici che converto in oggetti
		// tramite una Map<Integer,Fermata> - pattern "Identity Map"
		/*for(Fermata partenza : fermate) {
			List<Integer> idConnesse = dao.getIdFermateConnesse(partenza);
			for(Integer id : idConnesse) {
				Fermata arrivo = fermateIdMap.get(id);
				this.grafo.addEdge(partenza, arrivo);
			}
		}*/
		
		
		// modo 3: faccio una query sola che mi restituisca le coppie
		// di fermate da collegare (DELEGO QUASI TUTTO IL CARICO DI LAVORO AL DB)
		// (variante preferita 3C : usare Identity Map)
		List<CoppiaId> fermateDaCollegare = dao.getAllFermateConnesse();
		for(CoppiaId coppia: fermateDaCollegare) {
			this.grafo.addEdge(
					fermateIdMap.get(coppia.getIdPartenza()),
					fermateIdMap.get(coppia.getIdArrivo())
					);
		}
		
		System.out.println(this.grafo);
	    System.out.println("Vertici = "+ this.grafo.vertexSet().size()); // la Lista fermate è stata convertita in un Set dal metodo .vertexSet
        System.out.println("Archi = "+this.grafo.edgeSet().size());
	
        
	}
	
	public Map<Fermata,Fermata> visitaGrafo(Fermata partenza) {
		GraphIterator<Fermata,DefaultEdge> visita = // algortitmo di visita in ampiezza
				new BreadthFirstIterator<>(this.grafo,partenza); // se non specifico il vertice di partenza, lo sceglie il metodo stesso
		// prima di far lavorare l'iteratore, gli aggancio un listener, ovvero qualcosa in grado osservare l'iteratore durante la sua esplorazione
		Map<Fermata,Fermata> alberoInverso = new HashMap<>();
		alberoInverso.put(partenza, null); // la radice dell'albero non ha alcun padre
		visita.addTraversalListener(new RegistraAlberoDiVisita(alberoInverso,this.grafo));
		while(visita.hasNext()) {
			Fermata f = visita.next();
			// System.out.println(f); // nella stampa a video non c'è nulla che mi dice dove finisce un livello e ne inizia un altro
		}
		return alberoInverso;
	}
		
		
}
