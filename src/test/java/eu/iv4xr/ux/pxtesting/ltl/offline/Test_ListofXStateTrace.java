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
import java.util.function.Predicate;

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
import static eu.iv4xr.ux.pxtesting.ltl.PXQueryEDSL.*;
import static eu.iv4xr.ux.pxtesting.ltl.SeqTerm.*;
import static eu.iv4xr.ux.pxtesting.ltl.Area.* ;

import eu.iv4xr.framework.spatial.Vec3;
import org.junit.jupiter.api.BeforeAll;

/* Examples of test requirements can be written for PX testing using the given trace files are provided here.
 * F=fear, H=hope, D=distress, P=disappointment, J=joy, S=satisfaction
 * 
 */
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

	
	//loading all trace files. 
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

	//test with simpler commands
	
	//Is there a case that the agent experiences raising fear in room1 and hope in room2.
	@Test
	public void test_roomsfortraces() throws IOException {
		
		LTL<XState> g1 = sequence(
			 occur(F_().and(in(room1))),
			 occur(H_().and(in(room2)))	
			) ;	
		assertEquals(true, XStateTrace.satisfy(g1, list_trace))  ;
	}


	//Check raising hope and raising fear in a specific corridor.
	@Test
	public void test_greycorridortraces() throws IOException {
		
		Area C1 = rect(new Vec3(84,0,39), new Vec3(85,0,45)) ;
		Area C2 = rect(new Vec3(56,0,44), new Vec3(85,0,45)) ;
		Area C3 = rect(new Vec3(4,0,64), new Vec3(55,0,65)) ;
		Area C4 = rect(new Vec3(4,0,60), new Vec3(5,0,63)) ;
		Area C5 = rect(new Vec3(54,0,44), new Vec3(55,0,62)) ;
		Area greycorridor= C1.union(C2).union(C3).union(C4).union(C5);
		
		LTL<XState> t1 =  sequence(
				occur(H_().and(in(greycorridor))),
				occur(F_().and(in(greycorridor)))
				) ;

		assertEquals(false, XStateTrace.satisfy(t1, list_trace)); 
	}
	
	// check FDDnD emotion pattern in a corridor
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
		
		//FDDnD
		LTL<XState> t3 =  sequence(
				occur(F_().and(in(bluecorridor))), 
				occur(D_().and(in(bluecorridor))),
				occur(D_().and(in(bluecorridor))),
				occur(D_().and(in(bluecorridor)))) ; 
		
		assertEquals(true, XStateTrace.satisfy(t1, list_trace))  ;
		assertEquals(true, XStateTrace.satisfy(t2, list_trace));
		assertEquals(true, XStateTrace.satisfy(t3, list_trace)); 
	}
	
	// the agent starts in Room1, so this test checks whether the agent feels any hope.
	// Then hope again increase in room2 and finally gets fearful in room 3. 
	@Test
	public void test_Inescexample1() throws IOException {
	
		/*
		LTL<XState> ex1= now((XState S)->S.dHope()!=null&&S.dHope()>0)
				.until(
				ltlAnd(now((XState S)->room2.contains(S.pos)&& S.dHope()!=null&&S.dHope()>0),
				 eventually ((XState S)->room3.contains(S.pos)&& S.dFear()!=null && S.dFear()>0 )));
		*/
		
		LTL<XState> ex1= now(h_())
				.until(
				   ltlAnd(now(H_().and(in(room2))),
				          eventually(F_().and(in(room3))))) ;
		
		System.out.println(">>> ex1: " + XStateTrace.satisfy(ex1,list_trace)) ;
			 
     }
	
	// no hope but fear in room1, then hope increases either in room2 or room3 and finally 
    // gets distress in room8 (final room).
	@Test
	public void test_Inescexample2() throws IOException {
	
		LTL<XState>  ex2= sequence(
				absent(S-> ! (in(room1).test(S) &&  !h_().test(S) &&  f_().test(S))),
				occur(H_().and(in(room2).or(in(room3)))),
				occur(d_().and(in(room8)))
				);
	
		
		System.out.println(">>> ex2: " + XStateTrace.satisfy(ex2,list_trace)) ;
			 
     }

	// whenever hope increases, fear decreases
	@Test
	public void test_Inescexample3() throws IOException {
	
		LTL<XState> ex3= always(H().implies(now((XState S) -> S.dFear() != null && S.dFear()<0))) ;
		assertEquals(true, XStateTrace.satisfy(ex3, list_trace))  ;
		System.out.println(">>> ex3: " + XStateTrace.satisfy(ex3,list_trace)) ;

     }

	//There should be no increase of hope in rooms 1 and 2, that can only happen on rooms 3 
	@Test
	public void test_Inescexample4() throws IOException {

		LTL<XState> ex4=ltlAnd(
				always(now(in(room1).or(in(room2))).implies(nH())),
				eventually(in(room3).and(H_()))
				) ;
				
		assertEquals(true, XStateTrace.satisfy(ex4, list_trace));
		System.out.println(">>> ex4: " + XStateTrace.satisfy(ex4,list_trace)) ;

     }


	// there is at least one trace in which throughout the whole level, distress should never increase
	// until it reaches a state where satisfaction increases.
	@Test
	public void test_Inescexample5() throws IOException {
	

		LTL<XState> t1=ltlAnd(always(nD()),eventually(S()));
		
		LTL<XState> ex5= sequence(absent(D_()), occur(S_()));
		
		assertEquals(true, XStateTrace.satisfy(t1, list_trace))  ;
		assertEquals(true, XStateTrace.satisfy(ex5, list_trace))  ;
		System.out.println(">>> ex5: " + XStateTrace.satisfy(ex5,list_trace)) ;

     }
	
	// there is an execution in which fear should be increasing for at least 3 time stamp exactly at time 4.

	@Test
	public void test_Inescexample6() throws IOException {
	
		LTL<XState> boundedex= until_withinD(now(S->true),
										     until_atLeastD(F(),nF(),3),4,4);
				
		//LTL<XState> ex7_2= ltlOr(eventually(S-> room8.contains(S.pos)&& S.satisfaction()>0), eventually(S-> S.dDisappointment()!=null && S.dDisappointment()>0));
		
		assertEquals(true, XStateTrace.satisfy(boundedex,list_trace));
		System.out.println(">>> ex6: " + XStateTrace.satisfy(boundedex,list_trace)) ;
		
		
	}
	
	//  distress spikes 2 times back to back 
	@Test
	public void test_Inescexample9() throws IOException {
	
		//LTL<XState> ex9= eventually((XState S)-> S.dDistress()!=null && S.dDistress()>0).next((XState S)->S.dDistress()!=null && S.dDistress()<0); 
		//alternative
		LTL<XState> ex9= sequence(occur(D_()), occur(D_()));
			assertEquals(true, XStateTrace.satisfy(ex9, list_trace))  ;
			System.out.println(">>> ex9: " + XStateTrace.satisfy(ex9,list_trace)) ;

     }

	static Predicate<XState> in(Area A) {
		return S -> A.contains(S.pos) ;
	}
		
	/*
	static Now<XState> in(Area A) {
		return now(S -> A.contains(S.pos)) ;
	}
	*/

}
	
