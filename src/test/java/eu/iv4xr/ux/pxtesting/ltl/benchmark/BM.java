package eu.iv4xr.ux.pxtesting.ltl.benchmark;

import static eu.iv4xr.framework.extensions.ltl.LTL.always;

import eu.iv4xr.framework.extensions.ltl.LTL;
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.* ;
import static eu.iv4xr.ux.pxtesting.ltl.SeqTerm.* ;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.ltl.Area;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import nl.uu.cs.aplib.utils.CSVUtility;

/**
 * Benchmarking execution-time of LTL-checking over finite trace over
 * traces with increasing length.
 * 
 * Standard (unoptimized) trace-enriching is used.
 */
public class BM {
	
	Area A1 = Area.rect(new Vec3(10,0,0), new Vec3(50,1,1)) ;
	
	public static Predicate<XState> in(Area A) {
		return S -> A.contains(S.pos) ;
	}
	
	LTL<XState> f1 = sequence(
			occur(in(A1).and(H_())), absent(F_()), occur(H_())) ;
	
	
	LTL<XState> f2 = sequence(5000f,50000f,
			occur(in(A1).and(H_())), absent(F_()), occur(H_())) ;
	
	LTL<XState> f3 = sequence(
				  occur(H_()), 
			      absent(F_()), 
			      occur((XState S) -> A1.coveredPortion(S.history(XState.HOPE)) >= 0.1f)) ;
	
	LTL<XState> f4 = sequence(
			  500f,
			  5000f,
			  occur(H_()), 
		      absent(F_()), 
		      occur((XState S) -> A1.coveredPortion(S.history(XState.HOPE)) >= 0.1f)) ;

	
	
	
	@Disabled
	@Test
	public void bm1() throws IOException {
		boolean enhanceWithPositionHistory = false ;
		int repeatRuns = 10 ;
		
		List<String[]> data = new LinkedList<>() ;
		String[] row = { "trace-length", "enrichment-time" , "f1-time", "f2-time" } ;
		data.add(row) ;
		
		for (int k = 10000 ; k <=100000; k += 10000 ) {
			
			
			long enrichmentTime = 0 ;
			long evalTime_f1 = 0 ;
			long evalTime_f2 = 0 ;
			
			Object[] rf1 = {} ;
			Object[] rf2 = {} ;
			// check f1 and f2 on trace of length k, repeated N-times:
			for (int i = 0; i<repeatRuns; i++) {
			    var trace = BenchmarkCommon.genTrace(k) ;
			    rf1 = BenchmarkCommon.bench("f1", true,enhanceWithPositionHistory, false, trace, f1) ;
			    rf2 = BenchmarkCommon.bench("f2", false,enhanceWithPositionHistory, false, trace, f2) ;
			    enrichmentTime += (Long) rf1[2] ;
			    evalTime_f1 += (Long) rf1[3] ;
			    evalTime_f2 += (Long) rf2[3] ;
			}
			
			enrichmentTime = (long) ((float) enrichmentTime / (float) repeatRuns) ;
			evalTime_f1 = (long) ((float) evalTime_f1 / (float) repeatRuns) ;
			evalTime_f2 = (long) ((float) evalTime_f2 / (float) repeatRuns) ;
			
			System.out.println("== Bench k=" + k) ;
			System.out.println("   enriching time=" + enrichmentTime) ;	
			System.out.println("   f1:" + rf1[1] + ", time=" + evalTime_f1) ;
			System.out.println("   f2:" + rf2[1] + ", time=" + evalTime_f2) ;
			
			String[] rowk = { "" + k ,
					         "" + enrichmentTime, 
					         "" + evalTime_f1, 
					         "" + evalTime_f2 } ;
			data.add(rowk) ;
		}	
		
		CSVUtility.exportToCSVfile(',', data, "bm1.csv");
	}
	
	@Disabled
	@Test
	public void bm2() throws IOException {
		
		boolean enhanceWithPositionHistory = true ;
		int repeatRuns = 10 ;
		
		List<String[]> data = new LinkedList<>() ;
		String[] row = { "trace-length", "enrichment-time" , "f3-time", "f4-time" } ;
		data.add(row) ;
		
		for (int k = 1000 ; k <=10000; k += 1000 ) {
			
			
			long enrichmentTime = 0 ;
			long evalTime_f3 = 0 ;
			long evalTime_f4 = 0 ;
			
			Object[] rf3 = {} ;
			Object[] rf4 = {} ;
			// check f1 and f2 on trace of length k, repeated N-times:
			for (int i = 0; i<repeatRuns; i++) {
			    var trace = BenchmarkCommon.genTrace(k) ;
			    rf3 = BenchmarkCommon.bench("f3", true,enhanceWithPositionHistory, true, trace, f3) ;
			    rf4 = BenchmarkCommon.bench("f4", false,enhanceWithPositionHistory, true, trace, f4) ;
			    enrichmentTime += (Long) rf3[2] ;
			    evalTime_f3 += (Long) rf3[3] ;
			    evalTime_f4 += (Long) rf4[3] ;
			}
			
			enrichmentTime = (long) ((float) enrichmentTime / (float) repeatRuns) ;
			evalTime_f3 = (long) ((float) evalTime_f3 / (float) repeatRuns) ;
			evalTime_f4 = (long) ((float) evalTime_f4 / (float) repeatRuns) ;
			
			System.out.println("== Bench k=" + k) ;
			System.out.println("   enriching time=" + enrichmentTime) ;	
			System.out.println("   f3:" + rf3[1] + ", time=" + evalTime_f3) ;
			System.out.println("   f4:" + rf4[1] + ", time=" + evalTime_f4) ;
			
			String[] rowk = { "" + k ,
					         "" + enrichmentTime, 
					         "" + evalTime_f3, 
					         "" + evalTime_f4 } ;
			data.add(rowk) ;
		}	
		
		CSVUtility.exportToCSVfile(',', data, "bm2.csv");
	}
	
	@Test
	public void bm3() throws IOException {
		
		boolean enhanceWithPositionHistory = false ;
		int repeatRuns = 10 ;
		int maxFormulaLength = 20 ;
		int traceLength = 50000 ;
		
		List<String[]> data = new LinkedList<>() ;
		String[] row = { "seq-length", "g1-time", "g2-time" } ;
		data.add(row) ;
		
		var trace = BenchmarkCommon.genTrace(traceLength) ;
		// dummy evaluation just to enhance the trace:
	    BenchmarkCommon.bench("dummy", true,enhanceWithPositionHistory, false, trace,f2) ;
		
		for (int k = 1 ; k <= maxFormulaLength ; k++ ) {
			
			LTL<XState> g1 = BenchmarkCommon.generateSeqFormulas(null, null,k) ;
			LTL<XState> g2 = BenchmarkCommon.generateSeqFormulas(0f, 5000f,k) ;
						
			long evalTime_g1 = 0 ;
			long evalTime_g2 = 0 ;
			
			Object[] rg1 = {} ;
			Object[] rg2 = {} ;
			
			// check f1 and f2 on trace, repeated N-times:
			for (int i = 0; i<repeatRuns; i++) {
			    rg1 = BenchmarkCommon.bench("g1", false,false, false, trace, g1) ;
			    rg2 = BenchmarkCommon.bench("g2", false,false, false, trace, g2) ;
			    evalTime_g1 += (Long) rg1[3] ;
			    evalTime_g2 += (Long) rg2[3] ;
			}
			
			evalTime_g1 = (long) ((float) evalTime_g1 / (float) repeatRuns) ;
			evalTime_g2 = (long) ((float) evalTime_g2 / (float) repeatRuns) ;
			
			System.out.println("== Bench n=" + k) ;
			System.out.println("  g1:" + rg1[1] + ", time=" + evalTime_g1) ;
			System.out.println("  g2:" + rg2[1] + ", time=" + evalTime_g2) ;
			
			String[] rowk = { "" + k ,
					         "" + evalTime_g1, 
					         "" + evalTime_g2 } ;
			data.add(rowk) ;
		}	
		
		CSVUtility.exportToCSVfile(',', data, "bm3.csv");
	}
	

}
