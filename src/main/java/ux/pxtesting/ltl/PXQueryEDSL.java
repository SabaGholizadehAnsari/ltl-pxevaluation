package ux.pxtesting.ltl;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.UntilF;
import eu.iv4xr.framework.extensions.ltl.UntilWithRelativeTimedBound;
import ux.pxtesting.ltl.offline.XState;
import ux.pxtesting.ltl.offline.XStateTrace;
import ux.pxtestingPipeline.LRState;

import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import static eu.iv4xr.framework.extensions.ltl.LTL2Buchi.* ;
import static ux.pxtesting.ltl.SeqTerm.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * Extending Aplib's LTL DSL, see {@link eu.iv4xr.framework.extensions.ltl.LTL}, with more constructs to query/evaluate
 * traces. Some constructs require enriched trace; see {@link XStateTrace}.
 */
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
	 * Until variant with absolute time upper bound. until(f1,f2,max) is defined
	 * as f1 until (f2 and time &le; max).
	 */
	public static LTL<XState> until_atMost(
			LTL<XState> phi, 
			LTL<XState> psi,
			float ubound) {		
		var psi2 = ltlAnd(now((XState S) -> (float) S.time <= ubound),psi) ;
		return phi.until(psi2) ;
	}
	
	/**
	 * Until variant with absolute time lower bound. until(f1,f2,max) is defined
	 * as f1 until (f2 and time &ge; max).
	 */
	public static LTL<XState> until_atLeast(
			LTL<XState> phi, 
			LTL<XState> psi,
			float lbound) {		
		var psi2 = ltlAnd(now((XState S) -> (float) S.time >= lbound),psi) ;
		return phi.until(psi2) ;
	}
	
	/**
	 * Until-variant with absolute time interval. until(f1,f2,min,max) is defined
	 * as f1 until (f2 and min &le; time and time &le; max).
	 * 
	 * <p> If min is null, then we only have an upper bound. Similarly if max is null
	 * we only have lower bound.
	 */
	public static LTL<XState> until_within(
			LTL<XState> phi, 
			LTL<XState> psi,
			Float lbound, Float ubound) {
		if (lbound == null && ubound == null)
			return phi.until(psi) ;
		if (lbound == null) 
			return until_atMost(phi,psi,ubound) ;
		if (ubound == null)
			return until_atLeast(phi,psi,lbound) ;
		var psi2 = ltlAnd(now((XState S) -> lbound <= (float) S.time &&  (float) S.time <= ubound),psi) ;
		return phi.until(psi2) ;
	}	
	
	public static LTL<XState> until_rwithin(
			LTL<XState> phi, 
			LTL<XState> psi,
			Float lbound, Float ubound) {
		if (lbound == null && ubound == null)
			return phi.until(psi) ;
		
		return new UntilWithRelativeTimedBound(phi, psi,lbound,ubound) ;
	}
	
	/**
	 * Eventually-variant with absolute time interval. eventually(f,min,max) is defined
	 * as true until (f and min &le; time and time &le; max).
	 * 
	 * <p>If min is null, then we only have an upper bound. Similarly if max is null
	 * we only have lower bound.
	 */
	public static LTL<XState> eventually_within(
			LTL<XState> psi,
			Float lbound, Float ubound) {
		 return until_within(now((XState S) -> true), psi, lbound, ubound) ;
	}
	
	public static LTL<XState> eventually_rwithin(
			LTL<XState> psi,
			Float lbound, Float ubound) {
		 return until_rwithin(now((XState S) -> true), psi, lbound, ubound) ;
	}
	
	public static <State> LTL<State> until_atMostD(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return psi ;
		LTL<State> g = until_atMostD(phi,psi,bound-1);
		return ltlOr(psi,ltlAnd(phi,next(g))) ;
	}
	
	/**
	 * p until q, in at least k-steps.
	 */
	public static <State> LTL<State> until_atLeastD(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		if (bound <= 0) return phi.until(psi) ;
		LTL<State> g = until_atLeastD(phi,psi,bound-1);
		return ltlAnd(phi,ltlNot(psi),next(g)) ;
	}
	
	/**
	 * p until q, in at least k-steps, and at most n-steps.
	 */
	public static <State> LTL<State> until_withinD(
			LTL<State> phi, 
			LTL<State> psi,
			int lowerbound,
			int upperbound) {
		if (lowerbound > upperbound)
			throw new IllegalArgumentException() ;
		//int delta = upperbound - lowerbound ;
		//LTL<State> g = until_atLeast(phi,until_atMost(phi,psi,delta),lowerbound) ;
		LTL<State> g = ltlAnd( until_atLeastD(phi,psi,lowerbound), until_atMostD(phi,psi,upperbound)) ;
		return g ;
	}
	
	/**
	 * An optimized (faster) implementation of {@link #until_atMostD(LTL, LTL, int)}.
	 */
	public static <State> LTL<State> until_atMostDX(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		return new UntilF(phi,psi,null,bound) ;
	}

	/**
	 * An optimized (faster) implementation of {@link #until_atLeastD(LTL, LTL, int)}.
	 */
	public static <State> LTL<State> until_atLeastDX(
			LTL<State> phi, 
			LTL<State> psi,
			int bound) {
		return new UntilF(phi,psi,bound,null) ;
	}
	
	/**
	 * An optimized (faster) implementation of {@link #until_withinD(LTL, LTL, int, int)}.
	 */
	public static <State> LTL<State> until_withinDX(
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
	
	/**
	 * This the SEQ operator. It takes a list of seq-terms, and translates the sequence to a single
	 * LTL formula. Each seq-term can be positive or negative. If it is positive, it may have
	 * a time-bound. The time-bound can be expressed as a relative-time bound, or an absolute-time bound.
	 * 
	 * <p>See the paper for how it works.
	 */
	public static LTL<XState> sequence(SeqTerm ... phis) {
		List<SeqTerm> phis_ = new LinkedList<>() ;
		for (int k=0; k<phis.length; k++) {
			phis_.add(phis[k]) ;
		}
		return sequence(phis_) ;
	}
	
	/**
	 * This the SEQ operator, that also takes an absolute-time outer time-bound. The
	 * time-bound is interpreted as absolute time. It is then distributed/copied to
	 * every positive term in phis.
	 */
	public static LTL<XState> sequence(Float lbound, Float ubound,SeqTerm ... phis) {
		List<SeqTerm> phis_ = new LinkedList<>() ;
		for (int k=0; k<phis.length; k++) {
			phis_.add(phis[k]) ;
		}
		return sequence(lbound,ubound,phis_) ;
	}
	

	/**
	 * This the SEQ operator, that also takes an absolute-time outer time-bound. The
	 * time-bound is interpreted as absolute time. It is then distributed/copied to
	 * every positive term in phis.
	 */
	public static LTL<XState> sequence(
			Float lbound, 
			Float ubound, 
			List<SeqTerm> phis) {
		
		if (lbound == null && ubound == null)
			throw new IllegalArgumentException() ;
		
		for (var term : phis) {
			if (term.positive) {
				term.lowerTimeBound = lbound ;
				term.upperTimeBound = ubound ;
				term.absoluteTime = true ;
			}
		}
		return sequence(phis) ;
	}
	
	/**
	 * This the SEQ operator. It takes a list of seq-terms, and translates the sequence to a single
	 * LTL formula. Each seq-term can be positive or negative. If it is positive, it may have
	 * a time-bound. The time-bound can be expressed as a relative-time bound, or an absolute-time bound.
	 * 
	 * <p>See the paper for how it works
	 */
	public static LTL<XState> sequence(
			List<SeqTerm> phis) {
		if (phis.size() == 0) 
			throw new IllegalArgumentException() ;
		
		LTL<XState> ltl = null ;
		
		for(int n = phis.size()-1 ; 0<=n; n--) {
			SeqTerm f = phis.get(n) ;
		    //LTL<XState> f = removeNotNot(phis.get(n)) ;
			LTL<XState> f__ = now(f.getAtom()) ;
			
		    if (! f.positive) {
		    	// f is a negative-formula:
		    	if (n==phis.size()-1) {
		    		// f is the last formula
		    		ltl = always(f__) ;
		    	}
		    	else {
		    	    // f is not the last atom.
	    		    // We need to look at the next atom, which might have a time-bound.
			    	SeqTerm g = phis.get(n+1) ;
		    		if (g.hasNoTimeBound()) {
		    			ltl = f__.until(ltl) ;	
		    		}
		    		else if (g.absoluteTime) {
		    			// g has an absolute-time time-bound
		    			//System.out.println(">> translating to abs-time until, " 
		    			//		+ f.lowerTimeBound
		    			//		+ ", " + f.upperTimeBound) ;
		    			ltl = until_within(f__,ltl,g.lowerTimeBound,g.upperTimeBound) ;
		    		}
		    		else {
		    			// g has a relative-time time-bound
		    			ltl = until_rwithin(f__,ltl,g.lowerTimeBound,g.upperTimeBound) ;
		    		}	    			
		    	}
		    }
		    else {
		    	// f is a positive formula
		    	if (n==phis.size()-1) {
		    		// f is the last formula
		    		//System.out.println(">>> last formula is positive") ;
		    		ltl = f__.treeClone() ;
		    	}
		    	else {
		    		// f is not the last atom;
    				// we need to look at the next atom:
		    		SeqTerm g = phis.get(n+1) ;
		    		if (! g.positive) {
		    			// originally:
		    			// ltl = ltlAnd(f,next(g)) ; --> BUG, should be next(ltl). 
		    			ltl = ltlAnd(f__,next(ltl)) ;
		    		}
		    		else {
		    			// g is a positive term:
		    			if (g.hasNoTimeBound()) {
		    				ltl = ltlAnd(f__,next(eventually(ltl))) ;
		    			}
		    			else if (g.absoluteTime) {
		    				// g has an absolute-time time-bound
		    				ltl = ltlAnd(f__, next(eventually_within(ltl,g.lowerTimeBound,g.upperTimeBound))) ;
		    			}
		    			else {
		    				// g has a relative-time time-bound
		    				ltl = ltlAnd(f__,eventually_rwithin(ltl,g.lowerTimeBound,g.upperTimeBound)) ;
		    			}
		    				
		    		}	
		    	}
		    }	
		}
		SeqTerm f0 = phis.get(0) ;
		if (! f0.positive) {
			//System.out.println(">>> first formula is negative") ;
			return ltl ;
		}
		else if (f0.hasNoTimeBound()) {
			return eventually(ltl) ;
		}
		else if (f0.absoluteTime) {
			// f0 has an absolute-time time-bound
			return eventually_within(ltl,f0.lowerTimeBound,f0.upperTimeBound) ;
		}
		else {
			// f0 has a relative-time time-bound
			return eventually_rwithin(ltl,f0.lowerTimeBound,f0.upperTimeBound) ;
		}	
	}
	
	
	/**
	 * True is on a state with positive hope level.
	 */
	public static Predicate<XState> h_() {
		return (XState S) -> S.hope() > 0 ;
	}
	
	/**
	 * True is on a state with positive fear level.
	 */
	public static Predicate<XState> f_() {
		return (XState S) -> S.fear() > 0 ;
	}
	
	/**
	 * True is on a state with positive joy level.
	 */
	public static Predicate<XState> j_() {
		return (XState S) -> S.joy() > 0 ;
	}
	
	/**
	 * True is on a state with positive distress level.
	 */
	public static Predicate<XState> d_() {
		return (XState S) -> S.distress() > 0 ;
	}
	
	/**
	 * True is on a state with positive satisfaction level.
	 */
	public static Predicate<XState> s_() {
		return (XState S) -> S.satisfaction() > 0 ;
	}
	
	/**
	 * True is on a state with positive disappointment level.
	 */
	public static Predicate<XState> p_() {
		return (XState S) -> S.disappointment() > 0 ;
	}
	
	/**
	 * True is on a state with raising hope.
	 */
	public static Predicate<XState> H_() {
		return (XState S) -> S.dHope() != null && S.dHope()>0 ;
	}

	/**
	 * True is on a state with raising fear.
	 */
	public static Predicate<XState> F_() {
		return (XState S) -> S.dFear() != null && S.dFear()>0 ;
	}
	
	/**
	 * True is on a state with raising joy.
	 */
	public static Predicate<XState> J_() {
		return (XState S) -> S.dJoy() != null && S.dJoy()>0 ;
	}
	public static Predicate<XState> JGH_() {
		return (XState S) -> S.dJoyGH() != null && S.dJoyGH()>0 ;
	}
	/**
	 * True is on a state with raising distress.
	 */
	public static Predicate<XState> D_() {
		return (XState S) -> S.dDistress() != null && S.dDistress()>0 ;
	}

	/**
	 * True is on a state with raising satisfaction.
	 */
	public static Predicate<XState> S_() {
		return (XState S) -> S.dSatisfaction() != null && S.dSatisfaction()>0 ;
	}

	/**
	 * True is on a state with raising disappointment.
	 */
	public static Predicate<XState> P_() {
		return (XState S) -> S.disappointment() != null && S.disappointment()>0 ;
	}
	/**
	 * True is on a state with raising health.
	 */
	public static Predicate<XState> HP_() {
		return (XState S) -> S.health() != null && S.health()>0 ;
	}
	
	public static LTL<XState> H() {
		return now(H_()) ;
	}
	
	public static LTL<XState> F() {
		return now(F_()) ;
	}
	
	public static LTL<XState> J() {
		return now(J_()) ;
	}
	
	public static LTL<XState> D() {
		return now(D_()) ;
	}
	public static LTL<XState> S() {
		return now(S_()) ;
	}
	public static LTL<XState> P() {
		return now(P_()) ;
	}
	public static LTL<XState> HP() {
		return now(HP_()) ;
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
	
	public static int formulaSize(LTL<XState> phi) {
		if (phi instanceof Now) {
			return 0 ;
		}
		if (phi instanceof Until) {
			var phi_ = (Until) phi ;
			return 1 + formulaSize(phi_.phi1) + formulaSize(phi_.phi2) ;
		}
		if (phi instanceof WeakUntil) {
			var phi_ = (WeakUntil) phi ;
			return 1 + formulaSize(phi_.phi1) + formulaSize(phi_.phi2) ;
		}
		if (phi instanceof UntilWithRelativeTimedBound) {
			var phi_=(UntilWithRelativeTimedBound)phi;
			return 1+ formulaSize(phi_.phi1)+ formulaSize(phi_.phi2);
		}
		if (phi instanceof And) {
			var phi_ = (And) phi ;
			int cnt = 1 ;
			for (var psi : phi_.conjuncts) {
				cnt += formulaSize(psi) ; 
			}
			return cnt ;
		}

		if (phi instanceof Or) {
			var phi_ = (Or) phi ;
			int cnt = 1 ;
			for (var psi : phi_.disjuncts) {
				cnt += formulaSize(psi) ; 
			}
			return cnt ;
		}
		if (phi instanceof Next) {
			var phi_ = (Next) phi ;
			return 1+formulaSize(phi_.phi);
		}
		if (phi instanceof Not) {
			var phi_ = (Not) phi ;
			return 1+formulaSize(phi_.phi);
		}	
		throw new IllegalArgumentException() ;
	}

	// test
	public static void main(String[] args) {
		LTL<XState> bla = sequence(occur(H_()), absent(H_()), occur(H_())) ;
	}

}
