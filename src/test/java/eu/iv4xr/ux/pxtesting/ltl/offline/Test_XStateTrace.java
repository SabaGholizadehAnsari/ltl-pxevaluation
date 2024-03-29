package eu.iv4xr.ux.pxtesting.ltl.offline;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import  java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import eu.iv4xr.framework.extensions.ltl.BoundedLTL;
import eu.iv4xr.framework.extensions.ltl.BoundedLTL.*;
import eu.iv4xr.framework.extensions.ltl.LTL;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;
import eu.iv4xr.ux.pxtesting.ltl.Area ;
import eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL;
import eu.iv4xr.ux.pxtestingPipeline.EmotionCoverage;
import eu.iv4xr.ux.pxtestingPipeline.LRState;
import nl.uu.cs.aplib.utils.CSVUtility;
import eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.*;
import static eu.iv4xr.ux.pxtesting.ltl.Area.* ;

import eu.iv4xr.framework.spatial.Vec3;


public class Test_XStateTrace {
	
	
	static float EPSILON = 0.01f ;
	
	boolean aboutEquals(float x, float y) {
		return Math.abs(x - y) <= EPSILON ;
	}

	String projectroot = System.getProperty("user.dir") ;
	String slash = FileSystems.getDefault().getSeparator();
	String datadir = projectroot + slash + "src" + slash + "test" + slash + "data" ;

	//@Test
	public void test1() throws IOException {
		String file1 = datadir + slash + "sampleTracefile.csv" ;
		String file2 = datadir + slash + "samplePXTracefile.csv" ;
    	System.out.println(file1) ;
    	
    	XStateTrace.use_default_xyzt_naming();
    	var trace1 = XStateTrace.readFromCSV(file1) ;
    	System.out.println(trace1) ;

    	assertEquals(10,trace1.trace.size()) ;
    	assertEquals(new Vec3(3,0,4), trace1.trace.get(9).pos) ;
    	assertEquals(9,trace1.trace.get(9).time) ;
    	assertEquals(20,trace1.trace.get(3).health()) ;
    	assertEquals(40,trace1.trace.get(6).val("gold")) ;
    	
    	trace1.enrichTrace("health");

    	assertEquals(null, trace1.trace.get(0).diff("health")) ;
    	assertEquals(20, trace1.trace.get(1).diff("health")) ;
    	assertEquals(190, trace1.trace.get(6).diff("health")) ;
    	
    	assertEquals(20, trace1.trace.get(0).max("health")) ;
    	assertEquals(200, trace1.trace.get(9).max("health")) ;
    	assertEquals(10, trace1.trace.get(9).history("health").size()) ;
    	assertEquals(2, trace1.trace.get(9).history("health", v -> v>=100).size()) ;
    	assertEquals(1, trace1.trace.get(9).history("health", v -> v>=200).size()) ;
    	assertTrue(trace1.trace.get(9).history("health", v -> v>=200).contains(new Vec3(3,0,1))) ;

    	
    	XStateTrace.use_xyzt_naming();
    	var trace2 = XStateTrace.readFromCSV(file2) ;
    	trace2.enrichTrace("hope");
    	
    	assertEquals(535,trace2.trace.size()) ;
    	assertEquals(0.4775f , trace2.trace.get(8).hope()) ;
    	assertEquals(0.475f , trace2.trace.get(9).hope()) ;
    	assertEquals(0.475f , trace2.trace.get(9).fear()) ;
    	assertEquals(1, trace2.trace.get(438).joy()) ;
    	assertEquals(0.8f, trace2.trace.get(533).satisfaction()) ;
    	assertEquals(0, trace2.trace.get(9).distress()) ;
    	assertEquals(0, trace2.trace.get(9).disappointment()) ;
    	
    	assertEquals(0.475f - 0.4775f , trace2.trace.get(9).dHope()) ;
    	assertEquals(0.475f - 0.4775f , trace2.trace.get(9).dFear()) ;
    	assertEquals(1, trace2.trace.get(438).dJoy()) ;
    	assertEquals(0.8f, trace2.trace.get(533).dSatisfaction()) ;
    	assertEquals(0, trace2.trace.get(9).dDistress()) ;
    	assertEquals(0, trace2.trace.get(9).dDisappointment()) ;

	}
	
	@Test
	public void test_ltl() throws IOException {
		String file = datadir + slash + "samplePXTracefile.csv" ;
		
		XStateTrace.use_xyzt_naming();
		XStateTrace.posyName = "z" ;
		XStateTrace.poszName = "y" ;
		XStateTrace trace = XStateTrace.readFromCSV(file) ;
    	trace.enrichTrace("satisfaction", "Hope");
		
		LTL<XState> f1 = always(S -> S.distress() == 0) ;
		LTL<XState> f2 = eventually(S -> S.satisfaction() > 0) ;
		LTL<XState> f3 = eventually(S -> S.dHope() != null && S.dHope() > 0) ;
		LTL<XState> f4 = eventually(S -> S.distress() > 0) ;
		
		Area A1 = rect(new Vec3(45,0,15), new Vec3(50,0,20)) ;
		
		LTL<XState> f5 = eventually(S ->  A1.covered(S.history("satisfaction")).size() > 0) ;  //Occur in A1 at least once
		LTL<XState> f6 = eventually(S ->  A1.covered(S.history("joy",v -> v >= 10)).size() > 0.9) ; //Emotion more than the threshold , covered 90% of A1 area
		LTL<XState> f7 = eventually(S ->  S.satisfaction()>0 && A1.contains(S.pos)) ;  // s position is in A1 and the condition occur in A1 at least once
	
		LTL<XState> f8 = eventually(S ->
	      A1.coveredPortion(S.history("joy", v-> v>=0.3))>= 0.5) ;
		LTL<XState> f9 = eventually(S ->
	      A1.coveredPortion(S.history("joy", v-> v>=0.3)) >= 0.5) ;
		
		assertEquals(SATVerdict.SAT, trace.satisfy(f1))  ;
		assertEquals(SATVerdict.SAT, trace.satisfy(f2))  ;
		assertEquals(SATVerdict.SAT, trace.satisfy(f3))  ;
		assertEquals(SATVerdict.UNSAT, trace.satisfy(f4))  ;
		assertEquals(SATVerdict.SAT, trace.satisfy(f5))  ;
		
		//assertEquals(SATVerdict.UNSAT, trace.satisfy(f8))  ;

	}
	

}
	
