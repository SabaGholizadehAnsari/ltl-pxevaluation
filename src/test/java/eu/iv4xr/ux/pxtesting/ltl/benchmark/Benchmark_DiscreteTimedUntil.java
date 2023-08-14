package eu.iv4xr.ux.pxtesting.ltl.benchmark;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.* ;

import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.ltl.Area;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import eu.iv4xr.ux.pxtesting.ltl.offline.XStateTrace;
import nl.uu.cs.aplib.utils.Pair;

public class Benchmark_DiscreteTimedUntil {
	
	LTL<XState> f1 = until_withinD(
			now(S -> S.val("hp") >= 5 && S.val("hp") <= 9),
			now(S -> S.val("hp") <= 0),
			5,500) ;
	
	LTL<XState> f2 = until_withinDX(
			now(S -> S.val("hp") >= 5 && S.val("hp") <= 9),
			now(S -> S.val("hp") <= 0),
			5,500) ;
	
	void bench(int k) {
		System.out.println("Bench") ;
		System.out.println("=== k=" + k) ;
		XStateTrace trace = BenchmarkCommon.genTrace(k) ;

		long t0 = System.currentTimeMillis() ;
		trace.enrichTrace("hp");
		long t1 = System.currentTimeMillis() ;
		SATVerdict f1Result = trace.satisfy(f1) ;
		long t2 = System.currentTimeMillis() ;
		SATVerdict f2Result = trace.satisfy(f2) ;
		long t3 = System.currentTimeMillis() ;
		
		System.out.println("    enriching time=" + (t1 - t0)) ;	
		System.out.println("    naive timed-until f1:" + f1Result + ", time=" + (t2-t1)) ;
		System.out.println("    optimized timed-until f2:" + f2Result + ", time=" + (t3-t2)) ;
		System.out.println("    tot-time=" + (t3-t0)) ;
	}
	
	@Test
	void bench1() {
		bench(10000) ;
	}

}
