package eu.iv4xr.ux.pxtesting.ltl;

import java.util.function.Predicate;

import eu.iv4xr.ux.pxtesting.ltl.offline.XState;

public class SeqTerm {
	
	public boolean positive = true ;
	public Predicate<XState> P ;
	public Float lowerTimeBound = null ;
	public Float upperTimeBound = null ;
	public boolean absoluteTime = true ;
		
	SeqTerm(Predicate<XState> P) {
		this.P = P ;
	}
	
	/**
	 * Return the inner atom represented by this term. If this is a positive term, then
	 * the atom is just its P, otherwise it is not P.
	 */
	public Predicate<XState> getAtom() {
		if (positive) return P ;
		return (XState S) -> ! P.test(S) ;
	}
	
	public boolean hasNoTimeBound() {
		return lowerTimeBound==null && upperTimeBound == null ;
	}
	
	/**
	 * Construct a positive seq-term P, with no time-bound. A positive term P is interpreted
	 * simply as P, as opposed to a negative term that represents negation.
	 */
	public static SeqTerm occur(Predicate<XState> P) {
		return new SeqTerm(P) ;
	}
	
	/**
	 * Construct a positive seq-term P with a time-bound, expressed as a lower and an upper
	 * bound. One of these can be null. The given time bound is interpreted as absolute time. 
	 */
	public static SeqTerm occur_within(Predicate<XState> P, Float lbound, Float ubound) {
		var term = new SeqTerm(P) ;
		term.lowerTimeBound = lbound ;
		term.upperTimeBound = ubound ;
		return term ;
	}
	
	/**
	 * Construct a positive seq-term P with a time-bound, expressed as a lower and an upper
	 * bound. One of these can be null. The given time bound is interpreted as relative time. 
	 */
	public static SeqTerm present_rwithin(Predicate<XState> P, Float lbound, Float ubound) {
		if (lbound == null && ubound == null)
			throw new IllegalArgumentException() ;
		var term = new SeqTerm(P) ;
		term.lowerTimeBound = lbound ;
		term.upperTimeBound = ubound ;
		term.absoluteTime = false ;
		return term ;
	}
	
	/**
	 * Construct a <b>negative</b> seq-term P, with no time-bound. A negative term P is interpreted
	 * as "not P". A negative term cannot have a time-bound.
	 */
	public static SeqTerm absent(Predicate<XState> P) {
		var term = new SeqTerm(P) ;
		term.positive = false ;
		return term ;
	}
	
}
