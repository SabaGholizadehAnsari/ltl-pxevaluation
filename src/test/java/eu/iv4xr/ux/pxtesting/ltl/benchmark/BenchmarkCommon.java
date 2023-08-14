package eu.iv4xr.ux.pxtesting.ltl.benchmark;

import java.util.LinkedList;
import java.util.List;

import eu.iv4xr.framework.extensions.ltl.LTL;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import eu.iv4xr.framework.spatial.Vec3;
import eu.iv4xr.ux.pxtesting.ltl.SeqTerm;
import static eu.iv4xr.ux.pxtesting.ltl.SeqTerm.* ;
import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.* ;
import eu.iv4xr.ux.pxtesting.ltl.offline.XStateTrace;
import nl.uu.cs.aplib.utils.Pair;

public class BenchmarkCommon {

	/** 
	 * Generate a trace of the given length.
	 */
	static public XStateTrace genTrace(int length) {
		
		XStateTrace trace = new XStateTrace() ;
		for (int k=0; k<length; k++) {
			Float hp = (float) (k % 10) + 1 ;
			Float hope = hp / 10f ;
			Float fear = 1 - hp ;
			XState state = new XState(
					new Vec3(k,0,0),
					k,
					new Pair<String,Float>("hp",hp),
					new Pair<String,Float>(XState.HOPE,hope),
					new Pair<String,Float>(XState.FEAR,fear)
					) ;
			trace.trace.add(state) ;
		}
		return trace ;
	}

	
	static public Object[]  bench(
			String benchId, 
			boolean doEnrichment,
			boolean enrichWithPositionHistory,
			boolean enablePrintingInfo,
			XStateTrace trace, LTL<XState> f) {
		if (enablePrintingInfo)
			System.out.println("== Bench " + benchId + ", k=" + trace.trace.size()) ;

		long t0 = System.currentTimeMillis() ;
		if (doEnrichment)
			trace.enrichTrace(enrichWithPositionHistory,"hp",XState.HOPE,XState.FEAR);
		long t1 = System.currentTimeMillis() ;
		SATVerdict result = trace.satisfy(f) ;
		long t2 = System.currentTimeMillis() ;
		
		Long enrichmentTime = t1 - t0 ;
		Long evalTime = t2 - t1 ;
		Long totTime = t2 - t0 ;
		
		if (! doEnrichment) {
			enrichmentTime = 0L ;
			evalTime = totTime ;
		}
		
		if (enablePrintingInfo) {
			System.out.println("    enriching time=" + enrichmentTime) ;	
			System.out.println("    f:" + result + ", time=" + evalTime) ;
			System.out.println("    tot-time=" + totTime) ;
		}
		Object[] data = { benchId, result, enrichmentTime, evalTime, totTime} ;
		return data ;		
	}
	
	public static LTL<XState> generateSeqFormulas(
			Float lowerTimeBound,
			Float upperTimeBound,
			int N) {
		
		List<SeqTerm> seqInner = new LinkedList<>() ;
		
		for (int k=0; k<N; k++) {
			if (k % 2 == 0) {
				// even k
				seqInner.add(occur(H_())) ;
			}
			else {
				// odd k
				seqInner.add(absent(F_())) ;
			}
		}
		
		if (lowerTimeBound == null) {
			return sequence(seqInner) ;
		}
		else {
			return sequence(lowerTimeBound,upperTimeBound,seqInner) ;
		}
	}
}
