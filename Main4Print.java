import java.util.*;
import java.text.*;

public class Main4Print
{
    public static void main(String[] args)
	{
		ProcessPerformer pp1 = new ProcessPerformer();
		Process proc = new Process();
		Queue<Process> processList = pp1.getProcess();
		while(!processList.isEmpty())
		{
			proc = processList.remove();
			System.out.println("\n\nRequired Resources for Process: " + proc.getPid());
			proc.setResources(ResourceAllocator.getResources());
			Thread t1 = new Thread(proc);
			t1.setPriority(11-proc.getPriority());
			t1.start();
		}
    }
}

class ProcessPerformer
{
	public static Queue<Process> getProcess()
	{
		Comparator<Process> comparator = new PriorityComparator();
		PriorityQueue<Process> processList = new PriorityQueue<Process>(10, comparator);
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter no of processes you wish to execute:\n");
		int noOfProcesses = sc.nextInt();
		for(int i = 0; i < noOfProcesses; i++)
		{
			Process proc = new Process();
			System.out.println("Enter priority for process " + (i + 1) + " with id = " + proc.getPid());
			System.out.println("The value of priority must be between 1 and 10 including both. \n1 is for highest priority and 10 for least priority\n");
			proc.setPriority(sc.nextInt());
			processList.add(proc);
		}
		for (Process proc : processList)
			System.out.println(proc.toString());
		return (processList);
	}
}

class ResourceAllocator
{
    static Resource[] availableResources =
	{
        new Resource(),
        new Resource(),
        new Resource(),
        new Resource(),
        new Resource()
    };
    public static Queue<Resource> getResources()
	{
        Queue<Resource> listOfResources = new LinkedList<Resource>();
        int noOfResources = (int) (Math.random() * (availableResources.length - 1) + 2);
        int randomResourceIndex;
        String outputMessage = "\n";
		
		for(int i = 0; i < noOfResources; i++)
		{
			randomResourceIndex = (int) (Math.random() * (availableResources.length));
			listOfResources.add(availableResources[randomResourceIndex]);
			outputMessage += "Resource " + (randomResourceIndex + 1) + "\t";
		}

		outputMessage += "\n";
		for (Resource res : listOfResources)
			outputMessage += res.toString();
		outputMessage += "\n";
		System.out.println(outputMessage);
		return listOfResources;
    }
}

class Process implements Runnable
{
    private int pid;
	private int priority;
	private int resourceRunTime;
	private long startTime, endTime, totalTime;
	
	String strDateFormat = "hh:mm:ss a";
    DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
	Resource res;
	Date date = new Date();
	private Queue<Resource> listOfResources;

    public void setResources(Queue<Resource> listOfResources)
	{
        this.listOfResources = listOfResources;
    }

    Process()
	{
        pid = (int) (Math.random() * 10000 + 1000);
		this.totalTime = 0;
	}

    public int getPid()
	{
        return this.pid;
    }

	public long getTotalTime()
	{
        return this.totalTime;
    }	
	
	public int getRid()
	{
        return this.res.getResourceId();
    }
	
	public int getPriority()
	{
        return this.priority;
    }

	void setPriority(int priority)
	{
		this.priority = priority;
	}
	
	void setRid(Resource res)
	{
		this.res = res;
	}
	
    @Override
    synchronized public void run()
	{
		Resource r1;
        System.out.println("\n\nBeginning Execution of Process: " + this.pid);
        try
		{
             System.out.println("\nProcess " + getPid() + " will run now ");

			while(!listOfResources.isEmpty())
			{
				r1 = listOfResources.peek();
                System.out.println("\n" + getPid() + ": Requesting for " + r1.getResourceId() + " \nAt " + dateFormat.format(date) + "\n");
                this.startTime = System.currentTimeMillis();
				if((r1 != this.res) && !r1.requestResource(this))//r1 is not available for current process
				{
                    System.out.println("\n" + getPid() + ": Waiting for " + r1.getResourceId() + " in use by process " + r1.getProcessId());
					Thread.sleep(500);
                }
				else
                {
					resourceRunTime = (int) (Math.random() * 500) + 400;
					Thread.sleep(resourceRunTime);
                  
					System.out.println("\n" + getPid() + ": Used " + r1.getResourceId() + " now releasing it... " + " \nAt " + dateFormat.format(date));
					
					r1.releaseResource();
					this.endTime = System.currentTimeMillis();
					
					System.out.println("\n" + getPid() + ":Released " + r1.getResourceId() + " \nAt " + dateFormat.format(date) + "\nThe processing time is " + (this.endTime - this.startTime) + " msec\n");
					
					this.res = null;
					listOfResources.remove();
                }
				this.totalTime += this.endTime - this.startTime;
            }
			System.out.println(this.pid + ": has executed for " + this.totalTime + " msec");
        } catch (Exception e)
		{
			System.out.println(e);
            System.out.print(e.getStackTrace());
        }
	}
	
	public String toString()
	{
			return "\nProcess with id = " + this.pid + " and priority = " + this.priority + "\n";
	}
}

class Resource
{
    private int rid;
    private int pid;
	private boolean flagOccupied;
	private Queue<Process> listOfProcesses;//to maintain a list of processes requesting for a particular resource

    Resource()
	{
        this.rid = (int) (Math.random() * 10000 + 1000);
        this.pid = 0;
		this.flagOccupied = false;//since it is initially unoccupied
 		this.listOfProcesses = new LinkedList<Process>();
    }

    public int getResourceId()
	{
        return this.rid;
    }
	
	public int getProcessId()
	{
        return this.pid;
    }

    public boolean requestResource(Process pr)
	{
		if(this.flagOccupied == false)
        {
            this.flagOccupied = true;
            this.pid = pr.getPid();
            return true;
        }
 		if (this.flagOccupied == true)
        {
 			if(this.pid != pr.getPid() && (!listOfProcesses.contains(pr)))
			{
				listOfProcesses.add(pr);
				return false;
			}
			else return true;
        }
        return true;
    }

    public void releaseResource()
	{
		Process pr;
        if(listOfProcesses.isEmpty())//No other process in queue
		{
            this.flagOccupied = false;
			System.out.println(this.pid + ": has realeased " + this.rid);
			this.pid = 0;
			//Resource is unoccupied
			System.out.println("No process is requesting for " + this.rid + " resource");
		}
        else//Other processes in queue
		{
            this.flagOccupied = true;
			//Resource is occupied by next process in the queue
			System.out.println(this.pid + ": has realeased " + this.rid);
			pr = listOfProcesses.remove();
			this.pid = pr.getPid();
			pr.setRid(this);
			System.out.println(this.pid + ": has control of " + this.rid);
        }		
    }

	public String toString()
	{
			return "id = " + this.rid + "\t";
	}

}

class PriorityComparator implements Comparator<Process>
{
	@Override
	public int compare(Process proc1,Process proc2)
	{
		if(proc1.getPriority() == proc2.getPriority())
			return 0;
		else if(proc1.getPriority() > proc2.getPriority())
			return 1;
		else
			return -1;
	}
}