package application;

public class KillableThread extends Thread {
	public boolean killRequest;
	public static KillableThread runningThread;
	
	public KillableThread() {
		this.killRequest = false;
	}

	public void requestKill() {
		killRequest=true;
	}
}