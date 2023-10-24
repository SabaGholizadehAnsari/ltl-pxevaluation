package ux.pxtesting.ltl.offline;

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
import static ux.pxtesting.ltl.Area.*;
import static ux.pxtesting.ltl.PXQueryEDSL.*;
import static ux.pxtesting.ltl.SeqTerm.*;

import eu.iv4xr.framework.extensions.ltl.BoundedLTL;
import eu.iv4xr.framework.extensions.ltl.BoundedLTL.*;
import eu.iv4xr.framework.extensions.ltl.LTL;
import static eu.iv4xr.framework.extensions.ltl.LTL.* ;
import eu.iv4xr.framework.extensions.ltl.SATVerdict;

import static java.util.function.Predicate.*;
import eu.iv4xr.framework.spatial.Vec3;
import ux.pxtesting.ltl.Area;
import ux.pxtesting.ltl.PXQueryEDSL;
import ux.pxtesting.ltl.offline.XState;
import ux.pxtesting.ltl.offline.XStateTrace;

import org.junit.jupiter.api.BeforeAll;

/* 
 * F=fear, H=hope, D=distress, P=disappointment, J=joy, S=satisfaction
 * 
 */
public class Test_ZenopuslevelSpecifications {
	
	

	static String projectroot = System.getProperty("user.dir") ;
	static String slash = FileSystems.getDefault().getSeparator();
	static String datadir = projectroot + slash + "src" + slash + "test" + slash + "data" ;
	
	static List<XStateTrace> list_trace= new ArrayList<XStateTrace>();
	
	static Area roomP;
	static Area roomG;
	static Area roomF1;
	static Area roomF2;
	static Area CorGf1;

	
	//loading trace files. 
	@BeforeAll
	public static void loadtraces() throws IOException {
	
		XStateTrace.use_xyzt_naming();
		XStateTrace.poszName = "z" ;
		XStateTrace.posyName = "y" ;
		XStateTrace.posxName = "x" ;
		XStateTrace.timeName="t";
		DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(datadir + File.separator+ "Zenopus")) ;
		for (Path path : stream) {
			if (!Files.isDirectory(path)) {
				String fname = path.getFileName().toString();
				if (! fname.toLowerCase().endsWith(".csv")) continue ;
				XStateTrace trace = XStateTrace.readFromCSV(datadir + File.separator+ "Zenopus" + File.separator + fname) ;
				//trace.enrichTrace( "fear","hope", "joy","disappointment", "Finish","remainedhealth","b0","b1","minSqDistFlag", "dFN2","minSqDistEnemy");
				//trace.enrichTrace( "fear", "joy","remainedhealth","b0","b1","minSqDistFlag","joy-GH","dFN2");
				trace.calculateDiffs();
				list_trace.add(trace);
			}
			
		}
		System.out.println("Traces are loaded: " + list_trace.size());
		roomP = rect(new Vec3(192,3,48), new Vec3(208,3,64)) ;
		roomG = rect(new Vec3(174,0,83), new Vec3(185,0,95)) ;
		roomF1 = rect(new Vec3(149,3,107), new Vec3(167,3,125)) ;
		roomF2 = rect(new Vec3(149,0,134), new Vec3(167,0,156)) ;
		CorGf1 = rect(new Vec3(156,3,90), new Vec3(166,3,106)) ;
	}
	
	@Test
	public void precond() {
		
		System.out.println( "Seq in room G is : "+XStateTrace.satisfy(sequence(occur(in(roomG))), list_trace));
		System.out.println( "Seq in room F2 is : "+XStateTrace.satisfy(sequence(occur(in(roomF2))), list_trace));
		System.out.println( "long requirment on left side of  spec13 is : "+XStateTrace.satisfy(	sequence(occur(in(roomG)),occur(in(roomP).and((XState S)-> S.minSqDistFlag()!=null && S.minSqDistFlag()<=1).and(HP_()))), list_trace));
		System.out.println( "In room F1 and near enemy is : "+XStateTrace.satisfy(sequence(occur(in(roomF1).and((XState S)-> S.minSqDistEnemy()!=null && S.minSqDistEnemy()<=1))), list_trace));
		System.out.println( "In room F2 and near enemy is : "+XStateTrace.satisfy(sequence(occur(in(roomF2).and((XState S)-> S.minSqDistEnemy()!=null && S.minSqDistEnemy()<=1))), list_trace));
		System.out.println( "have possibility of being in room generally near enemy is : "+XStateTrace.satisfy(sequence(occur((XState S)-> S.minSqDistEnemy()!=null && S.minSqDistEnemy()<=1)), list_trace));

		
		//assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;
	}

	//Specification 1: Every player would feel excited at the beginning of the game, in area P.
	@Test
	public void test_Specification1() throws IOException {

		LTL<XState> spec = sequence(0f,7000f,occur((H_()))) ;	
		assertEquals(true, XStateTrace.valid(spec, list_trace))  ;
		System.out.println("Specifcation1 size: "+ formulaSize(spec));
	}

	//Specification 2: Every player would feel anticipation on how to go through the fire in area G without dying.
	@Test
	public void test_Specification2() throws IOException {

		LTL<XState> spec = sequence(occur(in(roomG))).implies(sequence(occur(in(roomG)),occur(in(roomG).and(H_()).and((XState S) -> S.health()!=null && S.health()>0)))) ;	
		
		assertEquals(true, XStateTrace.satisfy(
				eventually(S -> {
					XState S_ = (XState) S ;
					boolean ok = in(roomG).test(S_) && H_().test(S_)
							&& S_.health()!=null && S_.health()>0 ;
					if (ok)
						System.out.println(">>> pos=" + S_.pos + ", t=" + S_.time + ", hope" + S_.hope()) ;
					return ok ;	
				})
				, list_trace))  ;
		System.out.println("Specifcation2 size: "+ formulaSize(spec));

		//assertEquals(true, XStateTrace.satisfy(sequence(occur(in(roomG))), list_trace));
		assertEquals(true, XStateTrace.valid(spec, list_trace))  ;
	}

	//Specification 3: Every player would feel a bit worried about going through area G, since there is fire there.
	@Test
	public void test_Specification3() throws IOException {

		LTL<XState> spec = sequence(occur(in(roomG))).implies(sequence(occur(in(roomG).and(F_()))));	
		System.out.println("Specifcation3 size: "+ formulaSize(spec));
		assertEquals(true, XStateTrace.valid(spec, list_trace))  ;
	}
	//Specification 4:  There is at least a game-play in which a player would feel hopeful to complete the game as soon as they reach F2.
	@Test
	public void test_Specification4() throws IOException {

		LTL<XState> spec = sequence(occur(in(roomF2)), occur_rwithin(H_().and(in(roomF2)),0f,2000f)) ;
		System.out.println("Specifcation4 size: "+ formulaSize(spec));
		assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;
	}

	//Specification 5:  There is at least a game-play in which a player would feel a bit disappointed after walking for a while in specified areas.
	@Test
	public void test_Specification5() throws IOException {
		
		LTL<XState> spec = ltlAnd(sequence(occur(in(roomP))),sequence(occur(in(roomG))),
									sequence(occur(in(roomF1))),sequence(occur(in(roomF2))), sequence(occur(P_()))) ;	
		System.out.println("Specifcation5 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.unsat(spec, list_trace))  ;
	}
	//Specification 6:  When every player begins playing the game, they should be to accomplish something right away like pressing a button (b0 or b1).
		@Test
		public void test_Specification6() throws IOException {
			
			LTL<XState> spec = sequence(occur_rwithin((XState S)-> (S.B0()!=null && S.B0()==1) ||(S.B1()!=null && S.B1()==1),0f,30000f),occur_rwithin(J_(),0f,50000f));
			System.out.println("Specifcation6 size: "+ formulaSize(spec));

			assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;

		}
		//Specification 7:  Every player should feel a sense of fear as well as joy at least once in the whole game even if they lose the level fairly quickly
		@Test
		public void test_Specification7() throws IOException {
			
			LTL<XState> spec = ltlAnd(sequence(occur(JGH_())),sequence(occur(F_())));
			System.out.println("Specifcation7 size: "+ formulaSize(spec));

			assertEquals(true, XStateTrace.valid(spec, list_trace));
			
		}
	//Specification 8:  There is a game-play in which the player keep hopes that fire flames in room P can be avoided easily.
	@Test
	public void test_Specification8() throws IOException {
		
		//seq [ 𝑃 ; 𝑠𝑢𝑠𝑡𝑎𝑖𝑛 (𝑃 .ℎ > 0) ; ¬𝑃 ]
		//LTL<XState> spec = always(in(roomP).and((XState S) -> S.hope()!=null && S.hope()>0));
		 LTL<XState> spec = sequence(occur(in(roomG)),sustain(in(roomG).and((XState S) -> S.hope()!=null && S.hope()>0)),
				 			 occur(not(in(roomG))));
			System.out.println("Specifcation8 size: "+ formulaSize(spec));

		 assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;
	}

	//Specification 9:  In every gameplay, the user should face a small challenge in area F1 to feel scared before they face the horde of zombies in F2.
		@Test
		public void test_Specification9() throws IOException {
			
			 LTL<XState> spec = sequence(occur(in(roomF2))).implies(sequence(occur(in(roomF1).and(F_()))));
			//assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;
				System.out.println("Specifcation9 size: "+ formulaSize(spec));

			assertEquals(true, XStateTrace.valid(spec, list_trace));

		}
		//Specification 10: 
		//express state that happens not the causality.
		@Test
		public void test_Specification10() throws IOException {
			
			LTL<XState> spec = sequence(occur((XState S)->Vec3.distSq(S.pos,new Vec3(159,3,108))<=100 && S.dFN2()==0),occur_rwithin(F_(),0f,10000f));
			System.out.println("Specifcation10 size: "+ formulaSize(spec));

			assertEquals(true, XStateTrace.satisfy(spec, list_trace));

		}
		//Specification 11: Every player passing through room G will feel frustration.
		@Test
		public void test_Specification11() throws IOException {
			
			LTL<XState> spec = sequence(occur(in(roomG)), absent(in(roomG).and(P_())));
			System.out.println("Specifcation11 size: "+ formulaSize(spec));

			assertEquals(true, XStateTrace.satisfy(spec, list_trace))  ;
		}

		//Specification 13:Some players will feel delighted when they pass through the fire in room G and gain their health back by interacting with a healing flag in room P.
		@Test
		public void test_Specification13() throws IOException {
			
			LTL<XState> spec1 = sequence(occur(in(roomG)),occur(in(roomP).and((XState S)-> S.minSqDistFlag()!=null && S.minSqDistFlag()<=1).and(HP_())));
			LTL<XState> spec2= sequence(occur(in(roomP).and(JGH_())));
			int at_least=1;
			System.out.println("Specifcation13 size: "+ formulaSize(ltlAnd(spec1,spec2)));

			assertEquals(true, XStateTrace.satisfyC(ltlAnd(spec1,spec2), list_trace, at_least));
		}	
	//Specification 14: Some people may feel fear/ scared when encountering the zombies in room F1.
	@Test
	public void test_Specification14() throws IOException {
		
		LTL<XState> spec = sequence(occur(in(roomF1).and((XState S)-> S.minSqDistEnemy()!=null && S.minSqDistEnemy()<=20 )),occur(F_().and(in(roomF1))));
		
		int at_least= 2;
		System.out.println("Specifcation14 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.satisfyC(spec, list_trace,at_least));
	}
	

	
	
	//Specification 16: Players always feel a sense of fear when presented with the zombies in room F2.
	@Test
	public void test_Specification16() throws IOException {
		
		LTL<XState> spec= always(now(in(roomF2).and((XState S)-> S.minSqDistEnemy()!=null && S.minSqDistEnemy()<=1)).implies(sequence(occur(F_().and(in(roomF2))))));		
		// I can not use occure bc it returns seqterm not predicate and always only accepts predicate
		System.out.println("Specifcation16 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.valid(spec, list_trace));
	}
	
	// If the player first moves towards F1 via corridor GF1, and meets the closed door there, 
	// they will typically feel anticipation for going the other way, finding out how to open this door.
	@Test
	public void test_Specification18() throws IOException {
		
		LTL<XState> spec= sequence(occur(in(CorGf1).and((XState S)->S.dFN2()==0))).implies(sequence(occur(H_().and((XState S)->S.dFN2()==0))));
		System.out.println("Specifcation18 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.valid(spec, list_trace));
	}
		
	//Specification 19: Some player always feels joyful when reaching the finish flag alive.
	@Test
	public void test_Specification19() throws IOException {
		
		LTL<XState> spec = sequence(occur(J_().and(in(roomF2))),occur(in(roomF2).and((XState S) -> S.health()!=null && S.health()>0 && S.Finish()!=null && S.Finish()==1)));
		int at_least=4;
		System.out.println("Specifcation19 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.satisfyC(spec, list_trace,at_least));
	}
	//Specification 20: Some players feel nervous when traversing the maze in room G.
	@Test
	public void test_Specification20() throws IOException {
		
		LTL<XState> spec = sequence(occur(F_().and(in(roomG))));
		int at_least=7;
		System.out.println("Specifcation20 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.satisfyC(spec, list_trace,at_least));
	}
	
	//Specification 22: Every player feels nervous when they enter room F1 for the first time.
	@Test
	public void test_Specification22() throws IOException {
	
		LTL<XState> spec = sequence(occur(in(roomF1))).implies(sequence(absent(in(roomF1)),occur(in(roomF1)),occur(in(roomF1).and(F_()))));
		int at_least=5;
		System.out.println("Specifcation22 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.valid(spec, list_trace));
	}
	
	//Specification 24: For at least one gameplay, the player feels happy when they are in room F2.
	@Test
	public void test_Specification24() throws IOException {
	
		LTL<XState> spec = sequence(occur(in(roomF2).and(J_())));
		//int at_least=7;
		System.out.println("Specifcation24 size: "+ formulaSize(spec));

		assertEquals(true, XStateTrace.satisfy(spec, list_trace));
	}
	
	//Specification 25: There are no gameplays where the player feels hopeless in room P. 
	@Test
	public void test_Specification25() throws IOException {
	
		LTL<XState> spec = sequence(occur(in(roomP).and((XState S)-> S.hope()<=0.0001)));
		System.out.println("Specifcation25 size: "+ formulaSize(spec));
		 //assertEquals(true, XStateTrace.satisfy(spec, list_trace));
		assertEquals(true, XStateTrace.satisfy(spec, list_trace));
	}
	static Predicate<XState> in(Area A) {
		return S -> A.contains(S.pos) ;
	}
		

}
	
