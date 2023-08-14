package eu.iv4xr.ux.pxtesting.ltl.benchmark;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;

import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.ltl.Area;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import eu.iv4xr.ux.pxtesting.ltl.offline.XStateTrace;
import nl.uu.cs.aplib.utils.Pair;

public class Benchmark_TraceEnrichment {
	
	Float max(List<Pair<Vec3,Float>> history) {
		if (history.size() == 0) return null ;
		float m = history.get(0).snd ;
		for (var v : history) {
			m = Math.max(m, v.snd) ;
		}
		return m ;
	}
	
	Float avrg(List<Pair<Vec3,Float>> history) {
		if (history.size() == 0) return null ;
		float sum = 0 ;
		for (var v : history) {
			sum += v.snd ;
		}
		return sum/history.size() ;
	}
	
	List<Pair<Vec3,Float>> filter(List<Pair<Vec3,Float>> history, Predicate<Vec3> p) {
		return history.stream().filter(e -> p.test(e.fst)).collect(Collectors.toList()) ;
	}

	Area A1 = Area.rect(new Vec3(10,0,0), new Vec3(50,1,1)) ;
	LTL<XState> f1 = always(S ->  A1.covered(S.history("hp")).size() > 0) ;
	LTL<XState> f2 = always(S ->  {
		if (A1.contains(S.pos)) {
			float a = max(filter(S.history.get("hp"), p -> A1.contains(p))) ;
			//System.out.println(">>> " + a) ;
			return a <= 9.0 ;
		}
		return true ;	
	}) ;
	
	void benchNaive(int k) {
		System.out.println("Bench NAIVE") ;
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
		System.out.println("    f1:" + f1Result + ", time=" + (t2-t1)) ;
		System.out.println("    f2:" + f2Result + ", time=" + (t3-t2)) ;
		System.out.println("    tot-time=" + (t3-t0)) ;
		
	}
	
	LTL<XState> f3 = always(S -> S.val("hpmaxA1") == null || S.val("hpmaxA1") <= 9) ;
	
	void benchOptimized(int k) {
		System.out.println("Bench IMPROVED") ;
		System.out.println("=== k=" + k) ;
		XStateTrace trace = BenchmarkCommon.genTrace(k) ;

		long t0 = System.currentTimeMillis() ;
		trace.enrichCustom("hpmaxA1", 
				null,
				(prev,S) -> { 
					if (A1.contains(S.pos)) {
						if (prev == null) {
							return S.val("hp") ;
						}
						else {
							float m = Math.max(prev, S.val("hp")) ;
							//System.out.println(">>> " + m) ;
							return m ;
						}
					}
					return null ;					
				});
		long t1 = System.currentTimeMillis() ;
		SATVerdict f3Result = trace.satisfy(f3) ;
		long t2 = System.currentTimeMillis() ;
		System.out.println("    enriching time=" + (t1 - t0)) ;	
		System.out.println("    f3:" + f3Result + ", time=" + (t2-t1)) ;
		System.out.println("    tot-time=" + (t2-t0)) ;
	}
	
	@Test
	void bench1() {
		benchNaive(10000) ;
		benchOptimized(10000) ;
	}

}
