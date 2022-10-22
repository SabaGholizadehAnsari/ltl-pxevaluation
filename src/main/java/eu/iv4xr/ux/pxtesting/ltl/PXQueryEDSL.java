package eu.iv4xr.ux.pxtesting.ltl;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import eu.iv4xr.ux.pxtestingPipeline.LRState;

import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import static eu.iv4xr.framework.extensions.ltl.LTL2Buchi.* ;

import java.util.*;

public class PXQueryEDSL {
	
	/*
	public enum BoundType { boundBLT, boundBGT, boundBRACKET }  
	
	public static class Bound {
		public BoundType bty ;
		public Integer left ;
		public Integer right ;
	}
	*/
	
	/**
	 * p until q, in at most k-steps.
	 */
	static <State> LTL<State> until_atMost(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return psi ;
		LTL<State> g = until_atMost(phi,psi,bound-1);
		return psi.ltlOr(phi.ltlAnd(next(g))) ;
	}
	
	/**
	 * p until q, in at least k-steps.
	 */
	static <State> LTL<State> until_atLeast(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return phi.until(psi) ;
		LTL<State> g = until_atMost(phi,psi,bound-1);
		return phi.ltlAnd(next(g)) ;
	}
	
	/**
	 * p until q, in at least k-steps, and at most n-steps.
	 */
	static <State> LTL<State> until_within(
			LTL<State> phi, 
			LTL<State> psi,
			int lowerbound,
			int upperbound) {
		if (lowerbound > upperbound)
			throw new IllegalArgumentException() ;
		int delta = upperbound - lowerbound ;
		LTL<State> g = until_atLeast(phi,until_atMost(phi,psi,delta),lowerbound) ;
		return g ;
	}
	
	static <State> LTL<State> removeNotNot(LTL<State> phi) {
		return removeNotNotWorker(phi).treeClone() ;
	}
	
	private static <State> LTL<State> removeNotNotWorker(LTL<State> phi) {
		if (! (phi instanceof Not)) {
			return phi ;
		}
		
		// check if phi = Not(Not(psi)). If so, inner = Not(psi), else
		// inner is null:
		Not<State> inner = isNotNot(phi) ; 
		if (inner == null)
			return phi ;
		return removeNotNotWorker(inner.phi) ;
	}
	
	static <State> LTL<State> sequence(LTL<State> ... phis) {
		List<LTL<State>> phis_ = new LinkedList<>() ;
		for (int k=0; k<phis.length; k++) {
			phis_.add(phis[k]) ;
		}
		return sequence(phis_) ;
	}
	static <State> LTL<State> sequence(List<LTL<State>> phis) {
		if (phis.size() == 0) 
			throw new IllegalArgumentException() ;
		
		LTL<State> ltl = null ;
		for(int n = phis.size()-1 ; 0<=n; n--) {
		    LTL<State> f = removeNotNot(phis.get(n)) ;
		    if (f instanceof Not) {
		    	// f is a negative-formula:
		    	if (n==phis.size()-1) {
		    		// f is the last formula
		    		ltl = always(f) ;
		    	}
		    	else {
		    		ltl = f.until(ltl) ;
		    	}
		    }
		    else {
		    	// f is a positive formula
		    	if (n==phis.size()-1) {
		    		// f is the last formula
		    		ltl = f.treeClone() ;
		    	}
		    	else {
		    		// f is not the last atom;
    				// we need to look at the next atom:
		    		LTL<State> g = phis.get(n+1) ;
		    		if (g instanceof Not) {
		    			ltl = f.ltlAnd(next(g)) ;
		    		}
		    		else {
		    			ltl = f.ltlAnd(next(eventually(ltl))) ;
		    		}	
		    	}
		    }	
		}
		LTL<State> f0 = phis.get(0) ;
		if (f0 instanceof Not)
			return ltl ;
		else 
			return eventually(ltl) ;
	}
	
	public static Now<XState> H() {
		return now((XState S) -> S.dHope() != null && S.dHope()>0) ;
	}
	
	public static Not<XState> nH() {
		return ltlNot(H()) ;
	}
	
	// test
	public static void main(String[] args) {
		LTL<XState> bla = sequence(H(),nH()) ;
	}

}
