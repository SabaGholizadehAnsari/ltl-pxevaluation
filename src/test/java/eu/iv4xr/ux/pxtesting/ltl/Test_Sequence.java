package eu.iv4xr.ux.pxtesting.ltl;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.ltl.SATVerdict;

import static org.junit.jupiter.api.Assertions.*;

import static eu.iv4xr.ux.pxtesting.ltl.XStateTraceGenerator.* ;
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.* ;
import static eu.iv4xr.ux.pxtesting.ltl.SeqTerm.* ;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;

public class Test_Sequence {
	
	@Test
	public void test_sequence() {	// seq without time constraint
		
		var tr = genHopeFearTrace(data_("hope",0,0,0, 0),
                                  data_("fear",0,0,0,-1)) ;

		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur(H_())))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(absent(H_())))) ;
		
		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur(F_())))) ;

		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(absent(F_())))) ;
	
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(S -> S.fear() < 0)))) ;
		
		
		tr = genHopeFearTrace(data_("hope",0,0,0, 1, 0, 0,2,0,0),
				              data_("fear",0,0,0,-1,-1,-1,1,0,0)) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(H_()),occur(F_())))) ;

		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur(H_()),occur(F_()),occur(F_())))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(H_()),absent(H_()),occur(F_())))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(H_()),occur(F_()),absent(H_())))) ;
				
	}

}
