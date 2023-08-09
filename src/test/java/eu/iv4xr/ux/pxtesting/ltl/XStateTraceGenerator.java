package eu.iv4xr.ux.pxtesting.ltl;

import eu.iv4xr.ux.pxtesting.ltl.offline.XState;
import eu.iv4xr.ux.pxtesting.ltl.offline.XStateTrace;
import nl.uu.cs.aplib.utils.Pair;

public class XStateTraceGenerator {
	
	public static Pair<String,float[]> data_(String propName, int ... values) {
		
		float[] vals = new float[values.length] ;
		for (int k=0; k< values.length; k++) {
			vals[k] = (float) values[k] ;
		}
		
		return new Pair<String,float[]>(propName,vals) ;
	}
	
	public static XStateTrace genHopeFearTrace(Pair<String,float[]> hope, Pair<String,float[]> fear) {
		XStateTrace tr = new XStateTrace() ;
		long time = 0 ;
		for (int k=0; k < hope.snd.length ; k++) {
			var state = new XState(null,time, 
					new Pair<String,Float>("hope", hope.snd[k]),
					new Pair<String,Float>("fear", fear.snd[k]))  ;
			tr.trace.add(state) ;
			time = time + 10 ;
		}
		tr.enrichTrace();
		return tr ;
	}
	

}
