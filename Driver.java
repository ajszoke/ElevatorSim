import java.util.Scanner;

public class Driver {   
	public static void main(String[] args) {
		String input;
		int inputNum;
		long t0 = System.currentTimeMillis();
		ElevatorBox eb = new ElevatorBox();
		Controller c = new Controller(t0);
		Thread ebThread = new Thread(eb);
		Thread cThread = new Thread(c);
		Scanner s = new Scanner(System.in);

		eb.connectController(c);
		c.connectElevatorBox(eb);
		ebThread.start();
		cThread.start();
		System.out.println("System online");
		System.out.print("Choose a starting floor for t_0 >> ");
		inputNum = Integer.parseInt(s.nextLine().trim());
		c.setStartFloor(inputNum);
		System.out.println("\nElevator on floor: " + c.getLastFloor());
		while (true) {
			System.out.print(
					"\nSelect an option (enter number only):" +
							"\n\t<0> Call from ground floor" +
							"\n\t<1> Call from first floor" +
							"\n\t<2> Call from second floor" +
					"\n\t<3> Call from third floor");
			if (eb.isY3()) System.out.print(
					"\n\t<5> Toggle IRon (current: ON)");
			else System.out.print(
					"\n\t<5> Toggle IRon (current: OFF)");
			System.out.print(
					"\n\t<9> INSERT FIRE KEY" +
							"\n\t<a> Press ground floor button" +
							"\n\t<b> Press first floor button" +
							"\n\t<c> Press second floor button" +
							"\n\t<d> Press third floor button" +
					"\n>>> ");
			input = s.nextLine().trim().toLowerCase();
			if (!input.equals("0") && !input.equals("1") &&
					!input.equals("2") && !input.equals("3") &&
					!input.equals("a") && !input.equals("b") &&
					!input.equals("c") && !input.equals("d") &&
					!input.equals("9") && !input.equals("5")) {
				System.out.print("ERROR: invalid selection\n>>> ");
			} else if (input.equals("9")) { // START FIRE MODE BLOCK
				c.setZ9(true);
				System.out.println("FIRE KEY INSERTED");
				while (true) {
					System.out.print(
							"Select an option (enter number only):" +
									"\n\t<0> Call from ground floor" +
									"\n\t<1> Call from first floor" +
									"\n\t<2> Call from second floor" +
									"\n\t<3> Call from third floor" +
									"\n\t<a> Press ground floor button" +
									"\n\t<b> Press first floor button" +
									"\n\t<c> Press second floor button" +
									"\n\t<d> Press third floor button" +
									"\n\t<7> FIRE MODE--OPEN DOOR" +
									"\n\t<8> FIRE MODE--CLOSE DOOR" +
									"\n\t<9> REMOVE FIRE KEY" +
							"\n>>> ");
					input = s.nextLine().trim().toLowerCase();
					if (!input.equals("0") && !input.equals("1") &&
							!input.equals("2") && !input.equals("3") &&
							!input.equals("7") && !input.equals("8") &&
							!input.equals("a") && !input.equals("b") &&
							!input.equals("c") && !input.equals("d") &&
							!input.equals("9")) {
						System.out.print("ERROR: invalid selection\n>>> ");
					} else if (input.equals("7")) {
						eb.setY7(false);
						eb.setY6(true);						
					} else if (input.equals("8")) {
						eb.setY6(false);
						eb.setY7(true);
					} else if (input.equals("9")) {
						c.setZ9(true);
						System.out.println("FIRE KEY REMOVED");
						break;
					} else {
						try {
							inputNum = Integer.parseInt(input);
							c.addCall(inputNum);
						} catch (NumberFormatException nfe) {
							if      (input.equals("a")) c.addDest(0);
							else if (input.equals("b")) c.addDest(1);
							else if (input.equals("c")) c.addDest(2);
							else if (input.equals("d")) c.addDest(3);
						}
					}
				} // END FIRE MODE BLOCK
			} else if (input.equals("5")) {
				if (!eb.isY4()) System.out.println("\nERROR: Doors not open, IRon change blocked");
				else if (eb.isY3()) {
					System.out.println("IRon toggled OFF...");
					eb.setY3(false);
				} else {
					eb.setY3(true);
				}
			} else {
				try {
					inputNum = Integer.parseInt(input);
					c.addCall(inputNum);
				} catch (NumberFormatException nfe) {
					if      (input.equals("a")) c.addDest(0);
					else if (input.equals("b")) c.addDest(1);
					else if (input.equals("c")) c.addDest(2);
					else if (input.equals("d")) c.addDest(3);
				}   
			}
		}
	}
}