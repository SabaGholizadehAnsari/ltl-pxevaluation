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
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.*;
import static eu.iv4xr.ux.pxtesting.ltl.Area.* ;

import eu.iv4xr.framework.spatial.Vec3;
import org.junit.jupiter.api.BeforeAll;


public class Test_ListofXStateTrace {
	
	

	static String projectroot = System.getProperty("user.dir") ;
	static String slash = FileSystems.getDefault().getSeparator();
	static String datadir = projectroot + slash + "src" + slash + "test" + slash + "data" ;
	static List<XStateTrace> list_trace= new ArrayList<XStateTrace>();
	static Area room1;
	static Area room2;
	static Area room3;
	static Area room4;
	static Area room5;
	static Area room6;
	static Area room7;
	static Area room8;

	
@BeforeAll
public static void loadtraces() throws IOException {

	XStateTrace.use_xyzt_naming();
	XStateTrace.posyName = "z" ;
	XStateTrace.poszName = "y" ;
	DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(datadir + File.separator+ "NWave-the-flag_MAMRF_f0_z24_51")) ;
	for (Path path : stream) {
		if (!Files.isDirectory(path)) {
			String fname = path.getFileName().toString();
			if (! fname.toLowerCase().endsWith(".csv")) continue ;
			XStateTrace trace = XStateTrace.readFromCSV(datadir + File.separator+ "NWave-the-flag_MAMRF_f0_z24_51" + File.separator + fname) ;
			trace.enrichTrace( "fear","hope");
			list_trace.add(trace);
		}
	}
	
	 room1 = rect(new Vec3(41,0,51), new Vec3(48,0,58)) ;
	 room2 = rect(new Vec3(21,0,49), new Vec3(29,0,58)) ;
	 room3 = rect(new Vec3(1,0,51), new Vec3(9,0,59)) ;
	 room8=  rect(new Vec3(41,0,9), new Vec3(49,0,18)) ;
	


}

@Test
public void test_roomsfortraces() throws IOException {
	
	//test with simpler commands
	LTL<XState> g1 = PXQueryEDSL.sequence(
	       ltlAnd(in(room1),PXQueryEDSL.F()),
	       ltlAnd(in(room2),PXQueryEDSL.H())
		) ;
	
	assertEquals(true, XStateTrace.satisfy(g1, list_trace))  ;

}

	@Test
	public void test_greycorridortraces() throws IOException {
		
		Area C1 = rect(new Vec3(84,0,39), new Vec3(85,0,45)) ;
		Area C2 = rect(new Vec3(56,0,44), new Vec3(85,0,45)) ;
		Area C3 = rect(new Vec3(4,0,64), new Vec3(55,0,65)) ;
		Area C4 = rect(new Vec3(4,0,60), new Vec3(5,0,63)) ;
		Area C5 = rect(new Vec3(54,0,44), new Vec3(55,0,62)) ;
		Area greycorridor= C1.union(C2).union(C3).union(C4).union(C5);
		
		LTL<XState> t1 =  PXQueryEDSL.sequence(ltlAnd(in(greycorridor),PXQueryEDSL.H()),
						 ltlAnd(in(greycorridor),PXQueryEDSL.F())) ;

		
		assertEquals(false, XStateTrace.satisfy(t1, list_trace)); 

	}
	
	@Test
	public void test_bluecorridortraces() throws IOException {
		
		Area C1 = rect(new Vec3(43,0,3), new Vec3(46,0,8)) ;
		Area C2 = rect(new Vec3(43,0,3), new Vec3(66,0,5)) ;
		Area C3 = rect(new Vec3(64,0,4), new Vec3(66,0,17)) ;
		Area C4 = rect(new Vec3(63,0,14), new Vec3(76,0,16)) ;
		Area C5 = rect(new Vec3(75,0,13), new Vec3(76,0,26)) ;
		Area C6 = rect(new Vec3(74,0,24), new Vec3(96,0,25)) ;
		Area C7 = rect(new Vec3(94,0,25), new Vec3(96,0,35)) ;
		Area C8 = rect(new Vec3(89,0,33), new Vec3(96,0,36)) ;

		Area bluecorridor= C1.union(C2).union(C3).union(C4).union(C5).union(C6).union(C7).union(C8);
		//test room and corridors
		LTL<XState> t1 = eventually(S-> bluecorridor.covered(S.history("hope")).size()>0) ;
		LTL<XState> t2 = eventually(S-> bluecorridor.covered(S.history("fear")).size()>0) ;
		
		LTL<XState> t3 =  PXQueryEDSL.sequence(ltlAnd(in(bluecorridor),PXQueryEDSL.F()), 
						 					   ltlAnd(in(bluecorridor),PXQueryEDSL.D()),
						 					   ltlAnd(in(bluecorridor),PXQueryEDSL.D()),
						 					  ltlAnd(in(bluecorridor),PXQueryEDSL.nD())) ; //FDDnD
		
		assertEquals(true, XStateTrace.satisfy(t1, list_trace))  ;
		assertEquals(true, XStateTrace.satisfy(t2, list_trace));
		assertEquals(true, XStateTrace.satisfy(t3, list_trace)); 

	}
	
	@Test
	public void test_Inescexample1() throws IOException {
	
		LTL<XState> ex1= now((XState S)->S.dHope()!=null&&S.dHope()>0)
				.until(
				ltlAnd(now((XState S)->room2.contains(S.pos)&& S.dHope()!=null&&S.dHope()>0),
				 eventually ((XState S)->room3.contains(S.pos)&& S.dFear()!=null && S.dFear()>0 )));
		
		
		System.out.println(">>> ex1: " + XStateTrace.satisfy(ex1,list_trace)) ;
			 
     }
	
	@Test
	public void test_Inescexample2() throws IOException {
	
		LTL<XState>  ex2= PXQueryEDSL.sequence(now(S-> room1.contains(S.pos)&&  S.dHope() != null && S.dHope() < 0 &&  S.dFear() != null && S.dFear()>0),
				ltlOr(now((XState S)-> room2.contains(S.pos) && S.dHope() != null && S.dHope() > 0),
						now((XState S)-> room3.contains(S.pos) && S.dHope() != null && S.dHope() > 0)), 
				now(S-> room8.contains(S.pos)&&S.dDistress() != null &&S.dDistress()>0));
	
		
		System.out.println(">>> ex2: " + XStateTrace.satisfy(ex2,list_trace)) ;
			 
     }

	@Test
	public void test_Inescexample3() throws IOException {
	
		LTL<XState> ex3= always((XState S)->  S.dHope() != null && S.dHope() > 0)
				 .implies(now((XState S)->S.dFear() != null && S.dFear()<0));
		
			assertEquals(true, XStateTrace.satisfy(ex3, list_trace))  ;
			System.out.println(">>> ex3: " + XStateTrace.satisfy(ex3,list_trace)) ;

     }

	@Test
	public void test_Inescexample4() throws IOException {
	

		LTL<XState> ex4=ltlAnd(
				ltlAnd(
						always(in(room1).implies(ltlAnd(PXQueryEDSL.nF(),in(room1))))
						,always(in(room2).implies(ltlAnd(PXQueryEDSL.nF(), in(room2)))))
						, eventually(in(room3)).implies(eventually(ltlAnd(PXQueryEDSL.nF(), in(room3)))));
		
			assertEquals(true, XStateTrace.satisfy(ex4, list_trace));
			System.out.println(">>> ex4: " + XStateTrace.satisfy(ex4,list_trace)) ;

     }

	@Test
	public void test_Inescexample5() throws IOException {
	

		LTL<XState> t1=ltlAnd(always(PXQueryEDSL.nD()),eventually(PXQueryEDSL.S()));
		
		LTL<XState> ex5= PXQueryEDSL.sequence(always(PXQueryEDSL.nD()),PXQueryEDSL.S());
		assertEquals(true, XStateTrace.satisfy(t1, list_trace))  ;
		assertEquals(true, XStateTrace.satisfy(ex5, list_trace))  ;
			System.out.println(">>> ex5: " + XStateTrace.satisfy(ex5,list_trace)) ;

     }
	

	@Test
	public void test_Inescexample6() throws IOException {
	
		LTL<XState> boundedex= until_within(now(S->true),
										until_atLeast(F(),nF(),3),4,4);
				
		//LTL<XState> ex7_2= ltlOr(eventually(S-> room8.contains(S.pos)&& S.satisfaction()>0), eventually(S-> S.dDisappointment()!=null && S.dDisappointment()>0));
		
		assertEquals(true, XStateTrace.satisfy(boundedex,list_trace));
		System.out.println(">>> ex6: " + XStateTrace.satisfy(boundedex,list_trace)) ;
		
		
	}
	@Test
	public void test_Inescexample9() throws IOException {
	
		//LTL<XState> ex9= eventually((XState S)-> S.dDistress()!=null && S.dDistress()>0).next((XState S)->S.dDistress()!=null && S.dDistress()<0); 
		//alternative
		LTL<XState> ex9=PXQueryEDSL.sequence(eventually(PXQueryEDSL.D()),next((PXQueryEDSL.D())));
			assertEquals(true, XStateTrace.satisfy(ex9, list_trace))  ;
			System.out.println(">>> ex9: " + XStateTrace.satisfy(ex9,list_trace)) ;

     }

	static Now<XState> in(Area A) {
		return now(S -> A.contains(S.pos)) ;
	}

}
	
