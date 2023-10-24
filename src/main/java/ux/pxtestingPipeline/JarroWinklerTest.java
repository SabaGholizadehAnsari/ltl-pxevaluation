package ux.pxtestingPipeline;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import info.debatty.java.stringsimilarity.JaroWinkler;

public class JarroWinklerTest {
	
	
	static void pairwiseDif(float min, float end, float max, float step) {
		System.out.println("**============") ;
		System.out.println("*** end:" + end + ", step=" + step) ;
		
		List<Float> population = new LinkedList<>() ;
		for(float k=min; k<=end; k = k+step) {
			population.add(k) ;
		}
		pairwiseDif(population, min, max) ;	
	}
	
	static Random rnd = new Random() ;
	
	static List<Float> genRandomPopulation(int min, int max, int N) {
		List<Float> numbers = new LinkedList<>() ;
		for (int k=0; k<N; k++) {
			int r = rnd.nextInt(max + 1 - min) + min ;
			numbers.add((float) r) ;
		}
		return numbers ;
	}
	
	static List<Float> mkPopulation(Float ...floats) {
		List<Float> pop = new LinkedList<>() ;
		for (int k=0; k<floats.length; k++) pop.add(floats[k]) ;
		return pop ;
	}
	
	static void pairwiseDif(List<Float> population, float min, float max) {	
		int numOfPairs = 0 ;
		float totDist = 0 ;
		int N = population.size() ;
		for(int i=0; i<N; i++) {
			for (int k=i+1; k<N; k++) {
				numOfPairs++ ;
				totDist += Math.abs(population.get(k) - population.get(i)) ;
			}
		}
		float maxDist = max - min ;
		float normalizedTotDistance = totDist / maxDist ;
		float avrgDist = normalizedTotDistance / numOfPairs ;
		
		float sum = 0 ;
		for(var x : population) sum += x ;
		float mean = sum/population.size() ;
		float variance = 0 ;
		for(var x : population) {
			variance += (x - mean) * (x - mean) ;
		}
		variance = variance / population.size() ;
		float dev = (float) Math.sqrt(variance) ;
		float normalizedDev = dev/maxDist ;
		
		System.out.println("==============") ;
		System.out.println("*** minval:" + min + ", max=" + max + ", #population=" + population.size()) ;
		System.out.println("*** mean:" + mean) ;	
		System.out.println("*** variance:" + variance) ;	
		System.out.println("*** dev:" + dev) ;	
		System.out.println("*** normalized dev:" + normalizedDev) ;	
		//System.out.println("*** population:" + population) ;
		System.out.println("*** #pairs:" + numOfPairs) ;
		System.out.println("*** tot normalized dist:" + normalizedTotDistance) ;
		System.out.println("*** avrg dist:" + avrgDist) ;	
		
	}
	
	public static int intCode(String s) {
		char[] chars = s.toCharArray() ;
		int code = 0 ;
		for(char c : chars) {
			int c_ = c ;
			code += c_ ;
		}
		return code ;
	}
	
	public static char encode(String s, String[] vocabulary) {
		List<String>  vocabs = new LinkedList<>() ;
		for(int k=0; k<vocabulary.length; k++) vocabs.add(vocabulary[k]) ;
		vocabs.sort((s1,s2) -> s1.compareTo(s2));
		return (char) ('z' + vocabs.indexOf(s)) ;
	}
	
	public static void main(String[] artgs) {
		
		String[] vocabs = {"b0", "b1", "b3", "d1T", "d1O" } ;
		String s1 = "b0-{explore}->b1\nb1={toggle}->b1\nb1={explore}->d1T\nd1T={explore}->d1O\nd1O-{explore}->b1" ;
		String s2 = "b0-{explore}->b2\nb2={toggle}->b2\nb2={explore}->d1T\nd1T={explore}->d1O\nd1O-{explore}->b3" ;
		String s1b = "b0eb1tb1ed1Ted1Oeb1" ;
		String s2b = "b0eb2tb2ed1Ted1Oeb3" ;
		//String s1bb = "bectcexeyec" ;
		//String s2bb = "bedtdexeyea" ;
		String s1c = "" + encode("b0",vocabs) + "e" 
		                + encode("b1",vocabs) + "t" 
				        + encode("b1",vocabs) + "e"
				        + encode("d1T",vocabs)+ "e"
				        + encode("d1O",vocabs) 
				        + encode("b1",vocabs) 
				        ;
		String s2c = "" + encode("b0",vocabs) + "e" 
				        + encode("b2",vocabs) + "t" 
				        + encode("b2",vocabs) + "e"
					    + encode("d1T",vocabs)+ "e"
						+ encode("d1O",vocabs) 
						+ encode("b3",vocabs) 
				;
		
		
		JaroWinkler jw = new JaroWinkler(-1);
		System.out.println(1 - jw.similarity(s1,s2));
		System.out.println(1 - jw.similarity(s1b,s2b));
		System.out.println(1 - jw.similarity(s1c,s2c));
		
		pairwiseDif(1,10,10,1f) ;
		pairwiseDif(1,10,10,0.1f) ;
		pairwiseDif(1,10,10,2f) ;
		
		var population = genRandomPopulation(0,99,10) ;
		pairwiseDif(population,0,144) ;
		List<Float> pop1 = mkPopulation(1f,2f,3f,4f,5f,6f,7f,8f,9f,10f) ;
		pairwiseDif(pop1,1,18) ;
		//List<Float> pop2 = mkPopulation(1f,3f,5f,7f,9f) ;
		List<Float> pop2 = mkPopulation(1f,3f,7f,10f) ;
		pairwiseDif(pop2,1,18) ;
		
		//jw1(1,6,12,1f) ;
		//jw1(1,80,80,1) ;
		//jw1(1,81,81,02) ;
		//jw1(1,70,1) ;
		//jw1(1,200,1) ;
	}

}
