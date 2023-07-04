package eu.iv4xr.ux.pxtesting.ltl;

import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;

import static org.junit.jupiter.api.Assertions.*;

import static eu.iv4xr.ux.pxtesting.ltl.XStateTraceGenerator.* ;
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.* ;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;

public class Test_TimedUntil {
	
	
	//@Test
	public void test_eventually_within() {	// absolute-time eventually
		
		var tr = genHopeFearTrace(data_("hope",0,0,0,1,2),
				                  data_("fear",0,0,0,-1,-1)) ;
		
		assertEquals(SATVerdict.UNSAT,  tr.satisfy(eventually_within(H(),null,20f))) ;
		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),null,30f))) ;
		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),null,40f))) ;
		
		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),20f,null))) ;
		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),30f,null))) ;
		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),40f,null))) ;
		assertEquals(SATVerdict.UNSAT,  tr.satisfy(eventually_within(H(),45f,null))) ;

		assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_within(H(),40f,40f))) ;
		assertEquals(SATVerdict.UNSAT,  tr.satisfy(eventually_within(H(),45f,50f))) ;
		
		assertEquals(SATVerdict.SAT,  tr.satisfy(next(next(eventually_within(H(),40f,40f))))) ;	
	}
	
	//@Test
	public void test_until_within() { // absolute-time until
		
		var tr = genHopeFearTrace(data_("hope",0,0,0,1,2),
				                  data_("fear",0,0,0,-1,-1)) ;
		
		until_within(now((XState S) -> S.fear() <= 0) , H(),0f,0f) ;
		
		assertEquals(SATVerdict.UNSAT, tr.satisfy(until_within(now(S -> S.hope() <= 0) , H(), null , 20f))) ;
		assertEquals(SATVerdict.SAT, tr.satisfy(until_within(now(S -> S.hope() <= 0) , H(), null , 30f))) ;
		assertEquals(SATVerdict.SAT, tr.satisfy(until_within(now(S -> S.hope() <= 0) , H(), null , 40f))) ;

		assertEquals(SATVerdict.SAT, tr.satisfy(until_within(now(S -> S.hope() <= 0) , H(), 30f , 30f))) ;
		assertEquals(SATVerdict.UNSAT, tr.satisfy(until_within(now(S -> S.hope() <= 0) , H(), 40f , null))) ;

		assertEquals(SATVerdict.SAT, 
				tr.satisfy(next(next(until_within(now(S -> S.hope() <= 0) , H(), 30f , 30f))))) ;
		
		assertEquals(SATVerdict.UNSAT, 
				tr.satisfy(next(next(until_within(now(S -> S.hope() <= 0) , H(), 40f , 40f))))) ;
	}
	
	@Test
	public void test_eventually_rwithin() { // relative-time eventually
		
			var tr = genHopeFearTrace(data_("hope",0,0,0,1,2),
					                  data_("fear",0,0,0,-1,-1)) ;
			
			assertEquals(SATVerdict.UNSAT,tr.satisfy(eventually_rwithin(H(),null,20f))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_rwithin(H(),null,30f))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_rwithin(H(),null,40f))) ;
			
			assertEquals(SATVerdict.UNSAT,tr.satisfy(next(eventually_rwithin(H(),null,10f)))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(next(eventually_rwithin(H(),null,20f)))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(next(eventually_rwithin(H(),null,30f)))) ;
			
			assertEquals(SATVerdict.SAT,  
					tr.satisfy(next(next(next(eventually_rwithin(H(),null,0f)))))) ;
			
			assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_rwithin(H(),20f,null))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_rwithin(H(),40f,null))) ;
			assertEquals(SATVerdict.UNSAT,tr.satisfy(eventually_rwithin(H(),45f,null))) ;
			assertEquals(SATVerdict.SAT,  tr.satisfy(next(eventually_rwithin(H(),30f,null)))) ;
			assertEquals(SATVerdict.UNSAT,tr.satisfy(next(eventually_rwithin(H(),35f,null)))) ;


			assertEquals(SATVerdict.SAT,  tr.satisfy(eventually_rwithin(H(),40f,40f))) ;
			assertEquals(SATVerdict.UNSAT,tr.satisfy(eventually_rwithin(H(),45f,50f))) ;
			
			assertEquals(SATVerdict.UNSAT,tr.satisfy(next(next(eventually_rwithin(H(),40f,40f))))) ;
			assertEquals(SATVerdict.SAT,tr.satisfy(next(next(eventually_rwithin(H(),20f,20f))))) ;
		}
	

}
