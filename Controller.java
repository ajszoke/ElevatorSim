public class Controller implements Runnable {

	/// INPUTS
	private volatile boolean Z0;     //ground call
	private volatile boolean Z1;     //floor1 call
	private volatile boolean Z2;     //floor2 call
	private volatile boolean Z3;     //floor3 call
	private volatile boolean Z4;     //go up
	private volatile boolean Z5;     //go down
	private volatile boolean Z6;     //stop
	private volatile boolean Z7;     //recheck
	private volatile boolean Z8;     //pop stack
	private volatile boolean Z9;     //fire control

	/// OUTPUTS
	private volatile boolean Y1;     // go up
	private volatile boolean Y2;     // go down
	private volatile boolean Y4;     // target floor reached

	/// OTHER
	private ElevatorBox eb;
	private FloorQueue q;
	private int lastFloor;
	private int delta;
	private long t0;          // t_0 (reference system time for clock cycling)
	private boolean f0light;  // ground floor light
	private boolean f1light;  // floor1 light
	private boolean f2light;  // floor2 light
	private boolean f3light;  // floor3 light
	private volatile boolean fireMode;

	public Controller(long _t0) {
		Z0 = Z1 = Z2 = Z3 = Z4 = Z5 = Z6 = Z7 = Z8 = Z9 = Y1 = Y2 = Y4 = fireMode = false;
		lastFloor = 1;
		t0 = _t0;
	}

	public void connectElevatorBox(ElevatorBox _eb) {
		this.eb = _eb;
	}

	@Override
	public void run() {
		q = new FloorQueue();
		goToS0();
	}

	/// S0: Idle
	public void goToS0() {
		while (true) {
			if (Z0 || Z1 || Z2 || Z3) goToS5();
			else if (Z9) goToS6();
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}  
		}  
	}

	/// S1: Go up
	public void goToS1() {
		System.out.print("\nGoing up...\n>>> ");
		while (true) {
			if (Z7) {
				Z7 = false;
				goToS5();
			}
			if (Z9) goToS6();
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
		}      
	}

	/// S2: Go down
	public void goToS2() {
		System.out.print("\nGoing down...\n>>> ");
		while (true) {
			if (Z9) goToS6();
			else if (Z7) {
				goToS5();
			}
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
		}      
	}

	/// S3: Floor reached
	public void goToS3() {
		if      (q.getFloorNum() == 0) Z0 = false;
		else if (q.getFloorNum() == 1) Z1 = false;
		else if (q.getFloorNum() == 2) Z2 = false;
		else if (q.getFloorNum() == 3) Z3 = false;
		
		if (q.getNext() != null) q = q.getNext();
		else q = new FloorQueue();
		while (true) {
			if (Z8) {
				Z8 = false;
				goToS4();
			}
			if (Z9) goToS6();
			else {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
		}  
	}

	/// S4: End call
	public void goToS4() {
		if (!fireMode) {
			t0 = System.currentTimeMillis();
			System.out.print("Accepting user input for 5 seconds...\n>>> ");
			while (System.currentTimeMillis() - t0 < 5000) {
				try { Thread.sleep(1000); }
				catch (InterruptedException e) {}
			}
	
			if (q.getFloorNum() == -1 && lastFloor != 1) {
				q = new FloorQueue(1);
				System.out.print("\nNo more queued floors, returning to first floor...\n>>> ");
				goToS5();
			} else if (q.getFloorNum() == -1 && lastFloor == 1) {
				System.out.print("\nNo more queued floors, elevator waiting on first floor...\n>>> ");
				goToS0();
			} else {
				System.out.print("\nNext floor queued: " + q.getFloorNum() + "\n>>> ");
				goToS5();
			}
		} else goToS0();
	}

	/// S5: Compare current floor to target floor
	public void goToS5() {
		if      (lastFloor < q.getFloorNum())  Z4 = true;
		else if (lastFloor == q.getFloorNum()) Z6 = true;
		else if (lastFloor > q.getFloorNum())  Z5 = true;
		else    System.out.println("ERROR");

		f0light = f1light = f2light = f3light = false;
		if (lastFloor == 0 && Z7) {
			f0light = true;
			System.out.print("\nFloor 0 light flashes ON...\n>>> ");
		} else if (lastFloor == 1 && Z7) {
			f1light = true;
			System.out.print("\nFloor 1 light flashes ON...\n>>> ");
		} else if (lastFloor == 2 && Z7) {
			f2light = true;
			System.out.print("\nFloor 2 light flashes ON...\n>>> ");
		} else if (lastFloor == 3 && Z7) {
			f3light = true;
			System.out.print("\nFloor 3 light flashes ON...\n>>> ");
		}

		Z7 = false;
		if(Z4){
			Z4 = false;
			eb.setY1(true);
			goToS1();
		} else if(Z5){
			Z5 = false;
			eb.setY2(true);
			goToS2();
		} else if(Z6){
			Z6 = false;
			eb.setY4(true);
			goToS3();
		} else {
			try { Thread.sleep(1000); }
			catch (InterruptedException e) {}
			goToS5();
		}
	}

	/// S6: Toggle fire mode
	public void goToS6(){
		fireMode = !fireMode;
		Z9 = false;
		eb.setY5(true);
		q = new FloorQueue();
		Z0 = Z1 = Z2 = Z3 = false;
		if (eb.isY5()) lastFloor += -delta;
		if (!fireMode) {
			q = new FloorQueue();
			goToS4();
		}
		else goToS0();
	}

	public void addCall(int _floorNum) {
		addCall(this.q, _floorNum);
	}

	public void addCall(FloorQueue _q, int _floorNum) {
		if (_q.getFloorNum() == _floorNum && !_q.isDest()) System.out.print("Duplicate floor call ignored\n>>> ");
		else if (_q.getNext() != null) addCall(_q.getNext(), _floorNum);
		else {
			FloorQueue newQ = new FloorQueue(_floorNum);
			if (q.getFloorNum() == -1) q = newQ;
			else _q.setNext(newQ);
			System.out.print("\nCall successful, floor " + _floorNum + " queued\n>>> ");
			if      (_floorNum == 0) Z0 = true;
			else if (_floorNum == 1) Z1 = true;
			else if (_floorNum == 2) Z2 = true;
			else if (_floorNum == 3) Z3 = true;
		}
	}

	public void addDest(int _floorNum) {
		addDest(this.q, _floorNum);
	}

	public void addDest(FloorQueue _q, int _floorNum) {
		if (_q.getFloorNum() == _floorNum && _q.isDest()) System.out.print("Duplicate destination ignored\n>>> ");
		else if (_q.getNext() != null) {
			if (!_q.getNext().isDest()) {
				FloorQueue newQ = new FloorQueue(_floorNum);
				newQ.setNext(_q.getNext());
				newQ.setDest(true);
				_q.setNext(newQ);
			} else addDest(_q.getNext(), _floorNum);
		} else {
			FloorQueue newQ = new FloorQueue(_floorNum);
			newQ.setNext(q);
			newQ.setDest(true);
			q = newQ;
			System.out.print("\nCall successful, destination floor " + _floorNum + " queued\n>>> ");
			if      (_floorNum == 0) Z0 = true;
			else if (_floorNum == 1) Z1 = true;
			else if (_floorNum == 2) Z2 = true;
			else if (_floorNum == 3) Z3 = true;
		}
	}

	public boolean isZ0() {
		return Z0;
	}

	public void setZ0(boolean z0) {
		Z0 = z0;
	}

	public boolean isZ1() {
		return Z1;
	}

	public void setZ1(boolean z1) {
		Z1 = z1;
	}

	public boolean isZ2() {
		return Z2;
	}

	public void setZ2(boolean z2) {
		Z2 = z2;
	}

	public boolean isZ3() {
		return Z3;
	}

	public void setZ3(boolean z3) {
		Z3 = z3;
	}

	public void setZ7(boolean z7) {
		Z7 = z7;
	}

	public void setZ8(boolean z8) {
		Z8 = z8;
	}

	public boolean isZ9() {
		return Z9;
	}

	public void setZ9(boolean z9) {
		Z9 = z9;
	}

	public boolean isY1() {
		return Y1;
	}

	public void setY1(boolean y1) {
		Y1 = y1;
	}

	public boolean isY2() {
		return Y2;
	}

	public void setY2(boolean y2) {
		Y2 = y2;
	}

	public boolean isY4() {
		return Y4;
	}

	public void setY4(boolean y4) {
		Y4 = y4;
	}
	
	public boolean isFireMode() {
		return fireMode;
	}

	public void deltaFloor(int _delta) {
		lastFloor += _delta;
		System.out.print("\nElevator on floor: " + lastFloor);
	}

	public int getLastFloor() {
		return lastFloor;
	}
	
	public void setStartFloor(int startFloor) {
		lastFloor = startFloor;
	}
}

