package eu.iv4xr.framework.extensions.ltl;

import eu.iv4xr.framework.extensions.ltl.LTL.Until;

/**
 * Representing a bounded-version of the until-operator. So, p untilF q has 
 * a lowerbound L and upperbound U. 
 * 
 * <ul>
 * <li>If both are null, then the meaning is the same as the standard p until q;
 * <li>else, if L is null the property is p until[<=U] q. So, q must be established in
 * at most U-steps. Until then, p must be maintained.
 * <li>Else, if U is null, the property is p until[>=L] q. So, q must be established no 
 * sooner than L-steps. Until then, p must be maintained.
 * <li>Else the property is p until[L..U] q. So, q must be established no sooner than
 * L-steps and no later than U-steps. If L and U are the same, then q must be established
 * in exactly L-steps. Until then, p must be maintained.
 * </ul>
 * @author Wish
 *
 * @param <State>
 */
public class UntilF<State>  extends  Until<State> {
	
	public Integer upperBound ;
	public Integer lowerBound ;

	UntilF() { }
	
	public UntilF(LTL<State> phi1, LTL<State> phi2, Integer lowerBound, Integer upperBound) {
		if (lowerBound != null && upperBound != null && lowerBound > upperBound)
			throw new IllegalArgumentException() ;
		this.phi1 = phi1.treeClone() ;
		this.phi2 = phi2.treeClone() ;
		this.lowerBound = lowerBound ;
		this.upperBound = upperBound ;
	}
	
	@Override
    public UntilF<State> treeClone() {
    	var clone = new UntilF<State>() ;	
    	clone.upperBound = this.upperBound ;
    	clone.lowerBound = this.lowerBound ;
        clone.phi1 = this.phi1.treeClone() ;
        clone.phi2 = this.phi2.treeClone() ;
    	return clone ;        	
    }
	
	enum UNTIL_TYPE { UNTIL_STD, UNTIL_ATMOST, UNTIL_ATLEAST, UNTIL_WITHIN, UNTIL_EXACTLY } 
    
	
	public UNTIL_TYPE untilType() {
		if (upperBound == null && lowerBound == null)
			return UNTIL_TYPE.UNTIL_STD ;
		if (upperBound == null) 
			return UNTIL_TYPE.UNTIL_ATLEAST ;
		if (lowerBound == null) {
			return UNTIL_TYPE.UNTIL_ATMOST ;
		}
		if (lowerBound == upperBound) 
			return UNTIL_TYPE.UNTIL_EXACTLY ;
		return UNTIL_TYPE.UNTIL_WITHIN ;
	}
	
	@Override
    public SATVerdict sat() {
		
		if(fullyEvaluated) 
    		return evals.getFirst().verdict;
		
		UNTIL_TYPE uty = untilType() ;
		if (uty == UNTIL_TYPE.UNTIL_STD) {
			return super.sat() ;
		}
		
		phi1.sat();
        phi2.sat();
        var iterator = evals.descendingIterator();
        var iteratorPhi1 = phi1.evals.descendingIterator();
        var iteratorPhi2 = phi2.evals.descendingIterator();

        // keep track if phi1 until phi2 holds at sigma(k+1)
        boolean nextSat = false;
        
        int distance_to_q = -1 ;

        // calculate phi1 until phi2 holds on every sigma(k); we calculate this
        // backwards for every state in the interval:
        while (iterator.hasNext()) {
            var psi = iterator.next();
            var p = iteratorPhi1.next().verdict;
            var q = iteratorPhi2.next().verdict;
            if (q == SATVerdict.SAT) {
            	distance_to_q = 0 ;
                nextSat = true;          
            } else {
                if (nextSat && p == SATVerdict.SAT) {
                	distance_to_q++ ;
                }              
                else {
                    distance_to_q = -1 ;
                    nextSat = false;
                }
            }
            if (nextSat) {
            	if ((this.upperBound == null || distance_to_q <= this.upperBound)
                    &&
                    (this.lowerBound == null || this.lowerBound <= distance_to_q)) {
            		
                    psi.verdict = SATVerdict.SAT;
                }
                else {
                    psi.verdict = SATVerdict.UNSAT;
                 }
            }
            else {
            	psi.verdict = SATVerdict.UNSAT;
            }
        }
        
        return evals.getFirst().verdict;
	}
	
    @Override 
    public String toString() { 
    	switch (untilType()) {
    	case UNTIL_STD : 
    		return "(" + phi1 + ") U (" + phi2 + ")" ; 
    	case UNTIL_ATLEAST: 
    		return "(" + phi1 + ") U[>=" + lowerBound + "] (" + phi2 + ")" ; 
    	case UNTIL_ATMOST:
    		return "(" + phi1 + ") U[<=" + upperBound + "] (" + phi2 + ")" ; 
    	case UNTIL_EXACTLY:
    		return "(" + phi1 + ") U[=" + upperBound + "] (" + phi2 + ")" ; 
    	default:
    		return "(" + phi1 + ") U[" + lowerBound + ".." + upperBound + "] (" + phi2 + ")" ; 
    	}
    }

}
