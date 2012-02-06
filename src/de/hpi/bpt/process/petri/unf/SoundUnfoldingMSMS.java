package de.hpi.bpt.process.petri.unf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.hpi.bpt.graph.algo.CombinationGenerator;
import de.hpi.bpt.process.petri.PetriNet;
import de.hpi.bpt.process.petri.Place;
import de.hpi.bpt.process.petri.Transition;
import de.hpi.bpt.process.petri.unf.order.EsparzaAdequateOrderForArbitrarySystems;

/**
 * Unfolding for soundness checks (multi-source-multi-sink nets)
 * 
 * Proof of concept - must be improved
 * 
 * @author Artem Polyvyanyy
 */
public class SoundUnfoldingMSMS extends SoundUnfolding {

	protected PetriNet originalNet = null;
	
	/**
	 * Constructor
	 * 
	 * @param pn net to unfold
	 */
	public SoundUnfoldingMSMS(PetriNet pn) {
		// perform structural checks
		if (!pn.isFreeChoice()) throw new IllegalArgumentException("Net must be free choice!");
		if (dga.hasCycles(pn)) throw new IllegalArgumentException("Net must be acyclic!");
		
		// initialization
		this.originalNet = pn;
		this.net = this.constructAugmentedVersion(this.originalNet);
		this.totalOrderTs = new ArrayList<Transition>(this.net.getTransitions());
		
		UnfoldingSetup setup = new UnfoldingSetup();
		setup.ADEQUATE_ORDER = new EsparzaAdequateOrderForArbitrarySystems();
		setup.MAX_BOUND		 = Integer.MAX_VALUE;
		setup.MAX_EVENTS	 = Integer.MAX_VALUE;
		this.setup = setup;
		
		// construct unfolding
		this.construct();
	}

	/**
	 * Construct the augmented version of the net
	 * - Add a fresh source place s 
	 * - Add a fresh start transition t_c for every combination c of source places of the net
	 * - Add a fresh flow from s to every start transition
	 * - For every start transition t_c, add fresh flow from t_c to every place in c
	 * 
	 * @param net net
	 */
	private PetriNet constructAugmentedVersion(PetriNet net) {
		PetriNet result = net.clone();
		
		Collection<Place> sources = result.getSourcePlaces();
		Place s = new Place();
		for (int i=0; i<sources.size(); i++) {
			CombinationGenerator<Place> cg = new CombinationGenerator<Place>(sources, i+1);
			while (cg.hasMore()) {
				Collection<Place> comb = cg.getNextCombination();
				Transition t = new Transition();
				result.addFlow(s,t);
				for (Place p : comb) {
					result.addFlow(t,p);
				}
			}
		}
		
		Utils.addInitialMarking(result);
		return  result;
	}
	
	@Override
	public PetriNet getNet() {
		return this.originalNet;
	}
	
	@Override
	public boolean isSound() {
		Collection<Transition> augTs = new ArrayList<Transition>(this.net.getTransitions());
		Collection<Transition> augStartTs = new ArrayList<Transition>(this.net.getPostset(this.net.getSourcePlaces().iterator().next()));
		augTs.removeAll(augStartTs);
		
		Set<Condition> cs = new HashSet<Condition>(this.getLocallyUnsafeConditions());
		cs.addAll(this.getLocalDeadlockConditions());
		
		for (Event e : this.getEvents()) {
			boolean flag = false;
			for (Condition c : cs) {
				if (this.areCausal(e,c) || this.areCausal(c,e)) {
					flag = true;
					break;
				}
			}
			if (flag) continue;
			
			augTs.remove(e.getTransition());
		}
		
		return augTs.isEmpty();
	}
	
	/**
	 * Get original net without augmentation
	 * @return original net
	 */
	public PetriNet getOriginalNet() {
		return this.originalNet;
	}
}