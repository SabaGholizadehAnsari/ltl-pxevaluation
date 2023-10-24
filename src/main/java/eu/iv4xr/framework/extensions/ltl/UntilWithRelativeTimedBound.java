package eu.iv4xr.framework.extensions.ltl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import eu.iv4xr.framework.extensions.ltl.LTL.Until;
import eu.iv4xr.framework.extensions.ltl.UntilF.UNTIL_TYPE;
import ux.pxtesting.ltl.offline.XState;

/**
 * Until operator with relative time time-bound. The bound is specified in terms of
 * a lower bound and an upperbound. When interpreted over a finite sequence:
 * 
 * <blockquote>
 *     sigma,i  |== p until q (lb,ub)
 * </blockquote>
 * 
 * means that the usual p until q, but the q should happen within the inclusive time-bound 
 * of [lb..ub], relative to the time at sigma(i).
 */

public class UntilWithRelativeTimedBound  extends  Until<XState> {
	
	public float upperBound ;
	public float lowerBound ;
	
	LinkedList<Long> time = new LinkedList<>() ;

	UntilWithRelativeTimedBound() { }
	
	/**
	 * Constructor, to construct a formula of the form "p until q" with time-bound [lb..ub].
	 * One of the bounds can be null, but not both of them. A null lb will be converted to
	 * 0. A null ub will be converted to Float.MAX_VALUE.
	 */
	public UntilWithRelativeTimedBound(LTL<XState> phi1, LTL<XState> phi2, 
			Float lowerBound, 
			Float upperBound) {
		if (lowerBound == null && upperBound == null) {
			throw new IllegalArgumentException() ;
		}
		if (lowerBound != null && upperBound != null && lowerBound > upperBound)
			throw new IllegalArgumentException() ;
		
		if (lowerBound == null) {
			lowerBound = 0f ;
		}
		
		if (upperBound == null) {
			upperBound = Float.MAX_VALUE ;
		}
			
		this.phi1 = phi1.treeClone() ;
		this.phi2 = phi2.treeClone() ;
		this.lowerBound = lowerBound ;
		this.upperBound = upperBound ;
	}
	
	@Override
    public UntilWithRelativeTimedBound treeClone() {
    	var clone = new UntilWithRelativeTimedBound() ;	
    	clone.upperBound = this.upperBound ;
    	clone.lowerBound = this.lowerBound ;
        clone.phi1 = this.phi1.treeClone() ;
        clone.phi2 = this.phi2.treeClone() ;
    	return clone ;        	
    }
	
	@Override
    public void startChecking() {
    	time.clear();
    	super.startChecking();
    }
	
	@Override
    void evalAtomSat(XState state) {
		time.add(state.time) ;
		super.evalAtomSat(state);
    }
	
	private boolean withinInterval(Long timeNow, Set<Long> futureQs) {
		for (Long t : futureQs) {
			int tRelative = (int) (t - timeNow) ;
			if (lowerBound <= tRelative && tRelative <= upperBound) {
				return true ;
			}
		}
		return false ;
	}
	
	@Override
    public SATVerdict sat() {
		
		if(fullyEvaluated) 
    		return evals.getFirst().verdict;
		
		phi1.sat();
        phi2.sat();
        var iterator = evals.descendingIterator();
        var timeIterator = time.descendingIterator();
        var iteratorPhi1 = phi1.evals.descendingIterator();
        var iteratorPhi2 = phi2.evals.descendingIterator();

        // keep track if phi1 until phi2 holds at sigma(k+1)
        boolean nextSat = false;
        
        // keep track of the time on which future phi2 happens, which would be
        // candidates of phi1 Until phi2.
        Set<Long> futureQs = new HashSet<>() ;
        
        // calculate phi1 until phi2 holds on every sigma(k); we calculate this
        // backwards for every state in the interval:
        while (iterator.hasNext()) {
            var psi = iterator.next();
            var timeNow = timeIterator.next() ;
            var p = iteratorPhi1.next().verdict;
            var q = iteratorPhi2.next().verdict;
            
            if (q == SATVerdict.SAT) {
            	if (p == SATVerdict.SAT) {
            		// case-1: both p and q holds:
            		futureQs.add(timeNow) ;
            	}
            	else {
            		// case-2: q holds but p does not hold
            		futureQs.clear() ;
            		futureQs.add(timeNow) ;
            	}
            }
            else {
            	if (p == SATVerdict.SAT) {
            		// case-3: p holds, but q does not:
            		// do nothing, just pass on futureQs
            	}
            	else {
            		// case-4: neither p nor q holds:
            		futureQs.clear() ;
            	}
            }
            
            if (withinInterval(timeNow,futureQs)) {
            	psi.verdict = SATVerdict.SAT;
            }
            else {
            	psi.verdict = SATVerdict.UNSAT ;
            }
        }
        
        return evals.getFirst().verdict;
	}

}
