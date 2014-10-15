/**
 * Created by Administrator on 8/2/14.
 */
public class ChangeListener implements Runnable {

	private Object object;
	private Object oldObject;
	private long waitTime;

	private int ct = 0;

	public ChangeListener(Object object)
	{
		this.object = object;
		this.waitTime = 10;
	}

	public ChangeListener(Object object, long waitTime)
	{
		this.object = object;
		this.waitTime = waitTime;
	}

	public Thread start()
	{
		Thread changeListenerThread = new Thread(this);
		changeListenerThread.start();
		return changeListenerThread;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				if (!this.object.equals(this.oldObject))
					System.out.println("Changed.");
				this.oldObject = object;
				if (this.waitTime != 0)
					Thread.sleep(waitTime);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
