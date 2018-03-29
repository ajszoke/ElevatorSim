import java.io.*;
import java.util.*;

public class ElevatorBox implements Runnable {

	/// INPUTS
	private volatile boolean Y1; // "Go up"
	private volatile boolean Y2; // "Go down"
	private volatile boolean Y3; // "IRon"
	private volatile boolean Y4; // "Target floor reached"
	private volatile boolean Y5; // "Fire--key in"
	private volatile boolean Y6; // "Fire--open doors"
	private volatile boolean Y7; // "Fire--close doors"

	/// OUTPUTS
	private volatile boolean Z8; // "Call complete"
	private boolean sound;

	/// OTHER
	private long startTime;
	private long elapsedTime;
	private Controller c;

	public ElevatorBox() {
		Y1 = Y2 = Y4 = Y5 = Y6 = Y7 = Z8 = sound = false;
		Y3 = true;
	}

	public void connectController (Controller _c) {
		this.c = _c;
	}

	public void run() {
		goToS0();
	}

	/// S0: Idle
	public void goToS0() {
		while (true) {
			if (Y5) {
				Y5 = false;
				goToS8();
			}
			else if (Y1) {
				Y4 = false;
				goToS1();
			}
			else if (Y2) {
				Y4 = false;
				goToS2();
			}
			else if (Y4) {
				Y4 = false;
				goToS4();
			} else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
		}
	}

	/// S1: Go up
	public void goToS1() {
		Y1 = false;
		if (Y5) {
			Y5 = false;
			goToS8();
		}
		else goToS3(1);
	}

	/// S2: Go down
	public void goToS2() {
		Y2 = false;
		if (Y5) {
			Y5 = false;
			goToS8();
		}
		else goToS3(-1);
	}

	/// S3: In transit
	public void goToS3(int dir) {
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		while (elapsedTime < 5000) {
			if (Y5) {
				Y5 = false;
				goToS8();
			}
			try { Thread.sleep(1000); }
			catch (InterruptedException e) {}
			elapsedTime = (new Date()).getTime() - startTime;
		}
		goToS9(dir);
	}

	/// S4: Target floor reached
	public void goToS4() {
		System.out.println("\nDestination floor reached. *ding*");
		sound = true;
		if (c.isFireMode()) {
			Y4 = false;
			System.out.print("FIRE MODE: Waiting for \"OPEN DOORS\" (7) or \"FIRE KEY REMOVED\" (9)\n>>>");
			while (c.isFireMode()) {
				if (Y6) goToS5();
				else {
					try { Thread.sleep(1000); }
					catch (InterruptedException e) {}
				}
			}
			System.out.println("FIRE MODE toggled OFF. Resuming normal operation...");
			Y6 = Y7 = false;
			goToS0();
		}
		goToS5();
	}

	/// S5: Doors open
	public void goToS5() {
		sound = false;
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		System.out.println("Opening doors...");
		while (elapsedTime < 1000) {
			elapsedTime = (new Date()).getTime() - startTime;
			try { Thread.sleep(1000); }
			catch (InterruptedException e) {}
		}
		System.out.println("Doors opened.");
		if (!c.isFireMode()) System.out.println("Passengers entering and exiting...");
		goToS6();
	}

	/// S6: Wait for 2s of IRon
	public void goToS6() {
		Y3 = false;
		if (c.isFireMode()) {
			Y6 = false;
			System.out.print("FIRE MODE: Waiting for \"CLOSE DOORS\" (8) or \"FIRE KEY REMOVED\" (9)\n>>>");
			while (c.isFireMode()) {
				if (Y7) goToS7();
				else {
					try { Thread.sleep(1000); }
					catch (InterruptedException e) {}
				}
			}
			System.out.println("FIRE MODE toggled OFF. Resuming normal operation...");
			goToS0();
		}
		System.out.print("\nIRon toggled OFF. Waiting for IRon toggle (5)...\n>>> ");
		while (!Y3) {
			try { Thread.sleep(1000); }
			catch (InterruptedException e) {}
		} // wait for trigger from driver
		System.out.println("\nIRon toggled ON. Doors closing in 2 seconds...");
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		while (elapsedTime < 2000) {
			if (!Y3) goToS5();
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
				elapsedTime = (new Date()).getTime() - startTime;
			}
		}
		Y4 = false;
		goToS7();
	}

	/// S7: Doors close
	public void goToS7() {
		Y7 = false;
		System.out.println("Doors closing...");
		startTime = System.currentTimeMillis();
		elapsedTime = 0L;
		while (elapsedTime < 1000) {
			try { Thread.sleep(1000); }
			catch (InterruptedException e) {}
			elapsedTime = (new Date()).getTime() - startTime;
		}
		System.out.println("Doors closed.");
		c.setZ8(true);
		if (c.isFireMode()) {
			System.out.print(">>> ");
			goToS8();
		}
		else goToS0();
	}

	/// S8: Fire key in
	public void goToS8() {
		Y5 = false;
		while (c.isFireMode()) {
			if (Y1) goToS1();
			else if (Y2) goToS2();
			else if (Y4) goToS4();
			else if (Y6) goToS4();
			else if (Y7) goToS7();
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
		}
		goToS0();
	}

	/// S9: Recheck floor
	public void goToS9(int dir) {
		Y1 = Y2 = false;
		c.deltaFloor(dir);
		c.setZ7(true);
		while (true) {
			if (Y1) goToS1();
			else if (Y2) goToS2();
			else if (Y4) goToS4();
			else {
				try { Thread.sleep(20); }
				catch (InterruptedException e) {}
			}
		}   
	}

	public void setY1(boolean y1) {
		this.Y1 = y1;
	}

	public void setY2(boolean y2) {
		this.Y2 = y2;
	}

	public void setY3(boolean y3) {
		this.Y3 = y3;
	}

	public boolean isY3() {
		return this.Y3;
	}

	public boolean isY4() {
		return this.Y4;
	}
	
	public boolean isY5() {
		return this.Y5;
	}

	public void setY4(boolean y4) {
		this.Y4 = y4;
	}

	public void setY5(boolean y5) {
		this.Y5 = y5;
	}

	public void setY6(boolean y6) {
		this.Y6 = y6;
	}

	public void setY7(boolean y7) {
		this.Y7 = y7;
	}

	public void setZ8(boolean z8) {
		this.Z8 = z8;
	}

	public boolean isZ8() {
		return this.Z8;
	}
}