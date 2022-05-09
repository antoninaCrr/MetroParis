package it.polito.tdp.metroparis.model;

import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;

public class RegistraAlberoDiVisita implements TraversalListener<Fermata, DefaultEdge> {

	private Graph<Fermata,DefaultEdge> grafo;
	private Map<Fermata,Fermata> alberoInverso; // riferimento all'albero di visita che devo via via popolare
	
	public RegistraAlberoDiVisita(Map<Fermata, Fermata> alberoInverso, Graph<Fermata,DefaultEdge> grafo) {
		super();
		this.alberoInverso = alberoInverso;
		this.grafo = grafo;
	}

	// sono tutti i metodi da compilare per implementare l'interfaccia
	@Override
	public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
		// TODO Auto-generated method stub
		//System.out.println(e.getEdge()); // fammi vedere quali sono gli archi che attraversi
		
		// Fermata source = e.getSource(); // il source dell'evento è la classe che l'ha generato, non ciò che interessa a noi
	    Fermata source = this.grafo.getEdgeSource(e.getEdge());
	    Fermata target = this.grafo.getEdgeTarget(e.getEdge());
	    //System.out.println(source + "---" + target);
	
	    if(!alberoInverso.containsKey(target)) { // se nella mappa non c'è ancora il target vuol dire che l'ho scoperto grazie a source
	    	alberoInverso.put(target, source);
	        // System.out.println(target + " si raggiunge da  " + source);
	    }
	    else if(!alberoInverso.containsKey(source)) {
	    	alberoInverso.put(source, target); // se nella mappa non c'è ancora il source vuol dire che l'ho scoperto grazie al target
	    	// System.out.println(source + " si raggiunge da  " + target);
	    }
	}

	@Override
	public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void vertexFinished(VertexTraversalEvent<Fermata> e) {
		// TODO Auto-generated method stub
		
	}

}
