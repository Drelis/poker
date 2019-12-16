import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.StringTokenizer;
import java.util.Arrays;

public class PokerRunner {
	//counters for final result
	private int total = 0;
	private int wins = 0;
	private int ties = 0;
	
	// convert hand to int array and sort by the value
	private int[] convertHand(String [] hand){
		int[] result = new int [hand.length];
		for (int i = 0; i < hand.length; i++){
			result[i] = value(hand[i]);
		}
		Arrays.sort(result);
		return result;
	}
	private int rateStraight(String [] hand) {
		// does not count a straight of A 2 3 4 5
		int [] handMod = convertHand(hand);
		for (int i = 1; i < hand.length; i++) {
			if (handMod[i-1] != handMod[i]-1){
				return 0;
			}
		}
		return handMod[hand.length-1];
	}
	private int rateFour(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod[0] == handMod[3]) {
			return rate(handMod[0], handMod[4]);
		} else if (handMod[1] == handMod[4]) {
			return rate(handMod[4], handMod[0]);
		}
		return 0;
	}
	private int rateFullHouse(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod[0] == handMod[2] && handMod[3] == handMod[4]) {
			return rate(handMod[0], handMod[4]);
		} else if (handMod[0] == handMod[1] && handMod[2] == handMod[4]) {
			return rate(handMod[4], handMod[0]);
		}
		return 0;
	}
	private int rateThree(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod[0] == handMod[2]) {
			return rate(handMod[0], handMod[4], handMod[3]);
		} else if (handMod[1] == handMod[3]) {
			return rate(handMod[1], handMod[4], handMod[0]);
		} else if (handMod[2] == handMod[4]) {
			return rate(handMod[2], handMod[1], handMod[0]);
		}
		return 0;
	}
	private int rateDoublePair(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod[0] != handMod[1] && handMod[1] == handMod[2] && handMod[3] == handMod[4]) {
			return rate(handMod[4], handMod[2], handMod[0]);
		} else if (handMod[0] == handMod[1] && handMod[2] != handMod[3] && handMod[3] == handMod[4]) {
			return rate(handMod[4], handMod[1], handMod[2]);
		} else if (handMod[0] == handMod[1] && handMod[2] == handMod[3] && handMod[3] != handMod[4]) {
			return rate(handMod[3], handMod[1], handMod[4]);
		}
		return 0;
	}
	private int ratePair(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod[0] == handMod[1]) {
			return rate(handMod[0], handMod[4], handMod[3], handMod[2]);
		} else if (handMod[1] == handMod[2]) {
			return rate(handMod[1], handMod[4], handMod[3], handMod[0]);
		} else if (handMod[2] == handMod[3]) {
			return rate(handMod[2], handMod[4], handMod[1], handMod[0]);
		} else if (handMod[3] == handMod[4]) {
			return rate(handMod[3], handMod[2], handMod[1], handMod[0]);
		}
		return 0;
	}
	private int rateHigh(String [] hand){
		int [] handMod = convertHand(hand);
		if (handMod.length != 5) {
			return 0; // safety
		}
		return rate(handMod[4], handMod[3], handMod[2], handMod[1], handMod[0]);
	}
	// value of the card 1..13
	private int value(String card){
		if (card.charAt(0) > '1' && card.charAt(0) <= '9'){
			return card.charAt(0) - '1';
		}
		switch (card.charAt(0)) {
			case 'T': return 9;
			case 'J': return 10;
			case 'Q': return 11;
			case 'K': return 12;
			case 'A': return 13;
		}
		return 0;
	}
	
	// rate the array
	private int rate(int... crd){
		int result = 0;
		for (int i = 0; i < crd.length; i++){
			result = result * 15 + crd[i];
		}	
		return result;
	}
	
	// checks if a hand is a straight
	private boolean isFlush(String [] hand) {
		boolean result = true;	
		char first = hand[0].charAt(1);
		for (String card : hand) {
			result = result && (first == card.charAt(1));
		}		
		return result;
	}
	
	private int compare(int r1, int r2) {
		if (r1 > r2) {
			return 1;
		} else {
			return (r1 == r2)? 0 : -1;
		}
	}
	
	private int compareHands(String [][] deal){
		int result = 0;
		boolean hand0Flush = isFlush(deal[0]);
		boolean hand1Flush = isFlush(deal[1]);
		
		int straight0 = rateStraight(deal[0]);
		int straight1 = rateStraight(deal[1]);
		
		if (hand0Flush && straight0 > 0) {
			if (!hand1Flush) {
				return 1;
			}
			return compare(straight0, straight1);
		}
		
		int four0 = rateFour(deal[0]);
		int four1 = rateFour(deal[1]);		
		if (four0 + four1 > 0) {
			return compare(four0, four1);
		}
		
		int full0 = rateFullHouse(deal[0]);
		int full1 = rateFullHouse(deal[1]);
		if (full0 + full1 > 0) {
			return compare(full0, full1);
		}
	
		int high0 = rateHigh(deal[0]);
		int high1 = rateHigh(deal[1]);
		if (hand0Flush) {
			return hand1Flush ? compare(high0, high1) : 1;
		} else if (hand1Flush) {
			return -1;
		}
		
		if (straight0 + straight1 > 0) {
			return compare(straight0, straight1);
		}
		
		int three0 = rateThree(deal[0]);
		int three1 = rateThree(deal[1]);
		if (three0 + three1 > 0) {
			return compare(three0, three1);
		}
		
		int dpair0 = rateDoublePair(deal[0]);
		int dpair1 = rateDoublePair(deal[1]);
		if (dpair0 + dpair1 > 0) {
			return compare(dpair0, dpair1);
		}
		
		int pair0 = ratePair(deal[0]);
		int pair1 = ratePair(deal[1]);
		if (pair0 + pair1 > 0) {
			return compare(pair0, pair1);
		}
		
		return compare(high0, high1);
	}
	
	// single line processor increese counters after each line
	private void processLine(String line) {
		total++;
		
		String [][] deal = {new String[5], new String[5]};
		
		StringTokenizer tokenizer = new StringTokenizer(line);
		int cnt = 0;

		while (tokenizer.hasMoreElements()) {
			deal[cnt / 5][cnt % 5] = tokenizer.nextElement() + "";
			cnt++;
		}
		// test(deal[0]);
		int result = compareHands(deal);
		if (result == 0) {
			ties++;
		} else if (result > 0) {
			wins++;
		}
	}
	
	// main method read file process every line
	public static void main(String... args) throws IOException {
		PokerRunner poker = new PokerRunner();		
		String fileName = "p054_poker.txt";
		if (args.length > 0) {
			fileName = args[0];
		}
		Stream<String> stream = Files.lines(Paths.get(fileName));
        stream.forEach(line -> poker.processLine(line));
		System.out.println(String.format("Player 1 wins %s (%s were ties) of total %s deals", poker.wins, poker.ties, poker.total));
	}
}