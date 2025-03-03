package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private Graph<String, DefaultWeightedEdge> grafo;
	private EventsDao dao;
	private List<String> best;  //lista del cammino migliore
	
	public Model() {
		dao=new EventsDao();
	}
	
	
	public void creaGrafo(String categoria, int mese) {
		grafo=new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiunta vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		//aggiunta archi
		//non devo controllare che i vertici siano presenti nel grafo perchè l'ho fatto nella query
		for(Adiacenza a: dao.getArchi(categoria, mese)) {
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		
		System.out.println("Grafo creato!");
		System.out.println("# VERTICI: "+this.grafo.vertexSet().size());
		System.out.println("# ARCHI: "+this.grafo.edgeSet().size());
	}
	
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	
	public List<Adiacenza> getArchi(){
		List<Adiacenza> archi = new ArrayList<Adiacenza>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			archi.add(new Adiacenza(this.grafo.getEdgeSource(e),
					this.grafo.getEdgeTarget(e), 
					(int) this.grafo.getEdgeWeight(e)));
		}
		return archi;
	}
	
	
	public List<String> getCategorie(){
		return this.dao.getCategorie();
	}
	
	
	
	/**
	 * ritorna una lista di archi con peso maggiore del peso medio
	 */
	public List<Adiacenza> getArchiMaggioriPesoMedio(){
		
		//scorro gli archi del grafo e calcolo il peso medio
		double pesoTot=0.0;  //se mi faccio dare il peso dalla libreria è un double
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoTot+=this.grafo.getEdgeWeight(e);
		}
		double avg=pesoTot/this.grafo.edgeSet().size();
		System.out.println("PESO MEDIO: "+avg);
		
		//ri-scorro tutti gli archi prendendo quelli maggiori di avg
		List<Adiacenza> result=new ArrayList<>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > avg)
				result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)this.grafo.getEdgeWeight(e)));
		}
		
		return result;
	}
	
	
	public List<String> calcolaPercorso(String sorgente, String destinazione){
		best=new LinkedList<>();
		List<String> parziale=new LinkedList<>();
		parziale.add(sorgente);   //posso già aggiungere la sorgente perchè so che è il primo passo
		
		cerca(parziale, destinazione);  //volendo provo a mettere il livello ma poi mi accorgo che non serve quindi lo tolgo
		
		return best;
		
	}


	private void cerca(List<String> parziale, String destinazione) {
		//condizione di terminazione --> se arrivo alla destinazione controllo se è la soluzione migliore
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			//è la soluzione migliore?
			if(parziale.size()>best.size()) {
				best=new LinkedList<>(parziale);  //ne creo una copia
			}
			return;
		}
		
		
		//scorro i vicini dell'ultimo inserito e provo le varie strade
		for(String v: Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(v)) {  //per evitare che il cammino abbia cicli 
				parziale.add(v);
				cerca(parziale, destinazione);
				parziale.remove(parziale.size()-1);
			}
			
		}
		
		
	}
}
