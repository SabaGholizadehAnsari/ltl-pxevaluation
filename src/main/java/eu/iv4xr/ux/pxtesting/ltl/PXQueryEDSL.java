package eu.iv4xr.ux.pxtesting.ltl;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.UntilF;
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
	public static <State> LTL<State> until_atMost(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return psi ;
		LTL<State> g = until_atMost(phi,psi,bound-1);
		return ltlOr(psi,ltlAnd(phi,next(g))) ;
	}
	
	/**
	 * p until q, in at least k-steps.
	 */
	public static <State> LTL<State> until_atLeast(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return phi.until(psi) ;
		LTL<State> g = until_atLeast(phi,psi,bound-1);
		return ltlAnd(phi,ltlNot(psi),next(g)) ;
	}
	
	/**
	 * p until q, in at least k-steps, and at most n-steps.
	 */
	public static <State> LTL<State> until_within(
			LTL<State> phi, 
			LTL<State> psi,
			int lowerbound,
			int upperbound) {
		if (lowerbound > upperbound)
			throw new IllegalArgumentException() ;
		//int delta = upperbound - lowerbound ;
		//LTL<State> g = until_atLeast(phi,until_atMost(phi,psi,delta),lowerbound) ;
		LTL<State> g = ltlAnd( until_atLeast(phi,psi,lowerbound), until_atMost(phi,psi,upperbound)) ;
		return g ;
	}
	
	/**
	 * An optimized (faster) implementation of {@link #until_atMost(LTL, LTL, int)}.
	 */
	public static <State> LTL<State> untilx_atMost(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		return new UntilF(phi,psi,null,bound) ;
	}

	/**
	 * An optimized (faster) implementation of {@link #until_atLeast(LTL, LTL, int)}.
	 */
	public static <State> LTL<State> untilx_atLeast(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		return new UntilF(phi,psi,bound,null) ;
	}
	
	/**
	 * An optimized (faster) implementation of {@link #until_within(LTL, LTL, int, int)}.
	 */
	public static <State> LTL<State> untilx_within(
			LTL<State> phi, 
			LTL<State> psi,
			int lowerbound,
			int upperbound) {
		return new UntilF(phi,psi,lowerbound,upperbound) ;
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
	
	public static <State> LTL<State> sequence(LTL<State> ... phis) {
		List<LTL<State>> phis_ = new LinkedList<>() ;
		for (int k=0; k<phis.length; k++) {
			phis_.add(phis[k]) ;
		}
		return sequence(phis_) ;
	}
	public static <State> LTL<State> sequence(List<LTL<State>> phis) {
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
		    			ltl = ltlAnd(f,next(g)) ;
		    		}
		    		else {
		    			ltl = ltlAnd(f,next(eventually(ltl))) ;
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
	
	public static LTL<XState> H() {
		return now((XState S) -> S.dHope() != null && S.dHope()>0) ;
	}
	
	public static LTL<XState> F() {
		return now((XState S) -> S.dFear() != null && S.dFear()>0) ;
	}
	
	public static LTL<XState> J() {
		return now((XState S) -> S.dJoy() != null && S.dJoy()>0) ;
	}
	
	public static LTL<XState> D() {
		return now((XState S) -> S.dDistress() != null && S.dDistress()>0) ;
	}
	public static LTL<XState> S() {
		return now((XState S) -> S.dSatisfaction() != null && S.dSatisfaction()>0) ;
	}
	public static LTL<XState> P() {
		return now((XState S) -> S.disappointment() != null && S.disappointment()>0) ;
	}
	
	public static LTL<XState> nH() {
		return ltlNot(H()) ;
	}
	
	public static LTL<XState> nF() {
		return ltlNot(F()) ;
	}
	public static LTL<XState> nJ() {
		return ltlNot(J()) ;
	}
	public static LTL<XState> nD() {
		return ltlNot(D()) ;
	}
	public static LTL<XState> nS() {
		return ltlNot(S()) ;
	}
	public static LTL<XState> nP() {
		return ltlNot(P()) ;
	}

	// test
	public static void main(String[] args) {
		LTL<XState> bla = sequence(H(),nH()) ;
	}

}
