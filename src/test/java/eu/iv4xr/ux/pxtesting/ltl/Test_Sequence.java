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

		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(absent(F_()),occur(H_()),occur(F_())))) ;
		
		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur(H_()),occur(F_()),occur(F_())))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(H_()),absent(H_()),occur(F_())))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur(H_()),occur(F_()),absent(H_())))) ;
				
	}
	
	@Test
	public void test_sequence_with_relative_timeconstraints() {	// seq with relative-time constraints
			
		var tr = genHopeFearTrace(data_("hope",0,0,0, 0),
                                  data_("fear",0,0,0,-1)) ;

		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur_rwithin(H_(),null,100f)))) ;
		
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.hope() == 0, 0f, 0f)))) ;
		
		assertEquals(SATVerdict.UNSAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.hope() == 0, 40f, null)))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.fear() < 0, 30f, 30f)))) ;		
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.fear() < 0, 20f, 40f)))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.fear() < 0, 20f, null)))) ;

		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(occur_rwithin(S -> S.fear() < 0, null, 40f)))) ;

		tr = genHopeFearTrace(data_("hope",0,0,0, 1, 0, 0,2,0,0),
				              data_("fear",0,0,0,-1,-1,-1,1,0,0)) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(
						absent(F_()),
						occur_rwithin(H_(),30f,30f),
						occur_rwithin(F_(),30f,30f)))) ;

		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(
						occur_rwithin(H_(),30f,null),
						occur_rwithin(F_(),null,30f),
						absent(S -> S.fear() < 0)))) ;
		
		assertEquals(SATVerdict.SAT,  
				tr.satisfy(sequence(
						occur_rwithin(H_(),30f,null),
						absent(H_()),
						occur_rwithin(F_(),null,30f)))) ;
				
	}

}
