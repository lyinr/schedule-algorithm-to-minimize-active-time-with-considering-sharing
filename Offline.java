
import java.awt.JobAttributes;
import java.awt.Component.BaselineResizeBehavior;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.text.StyledEditorKit.ForegroundAction;

public class Offline {
	Task [] task = new Task[Constances.NUM_TASK];
	int [][] minsubinterval = new int[Constances.T_LATEST + 1][Constances.T_LATEST + 1];
	int [][] LTaskStart = new int[Constances.T_LATEST + 1][Constances.T_LATEST + 1];
	int [] machine = new int[Constances.NUM_MACHINE];
	int p_max = 0;
	int l_star = 0;

	
	public int getMaxP(){
		return p_max;
	}
	
	public void setL(int n){
		l_star = n;
	}
	
	public void init_process(Task []tem_task){		
		for(int i = 0; i < Constances.NUM_TASK; i++ ){
			task[i] = tem_task[i];
			if( task[i].process_span > p_max){
				p_max = task[i].process_span;
			}
		}
		
		for(int i = 0; i <= Constances.T_LATEST; i++){
			for(int j = 0; j <= Constances.T_LATEST; j++){
				minsubinterval[i][j] = -1;
				LTaskStart[i][j] = -1;
			}
		}	
		
		for(int i = 0; i < Constances.NUM_MACHINE; i++ ){
			machine[i] = 0;
		}
	}
	
	public int offlineSchedule( int linterval, int rinterval){
		
		//computed before
		if(minsubinterval[linterval][rinterval] >= 0){
			return minsubinterval[linterval][rinterval];
		}
		
		int longindex = locateLongTask(linterval, rinterval);
		//if there no task in the interval, return 0
		if(longindex < 0){
			return 0;
		}
		
		int opt_start_time = -1;
		int mintime = Constances.T_LATEST;
		int releasetime = task[longindex].release_time;
		int lengthtask = task[longindex].process_span;
		int deadtime = task[longindex].due_time;
		
		int taskcontri = 0;
		int lpart = 0;
		int rpart = 0;
		for(int ltime = releasetime; ltime <= deadtime - lengthtask; ltime++ ){
			
			int curdeadline = ltime + lengthtask;
			
			//the left part partitioned by longindex
			if(ltime <= linterval){
				lpart = 0;
			}else {
				lpart = offlineSchedule( linterval, ltime);
			}
			
			
			//the right part partitioned by longindex
			if(curdeadline >= rinterval){
				rpart = 0;
			}else {
				rpart = offlineSchedule(curdeadline, rinterval);
			}
			
			if(ltime <= linterval  && curdeadline <= rinterval){
				taskcontri = curdeadline- linterval;
			}else if(ltime >= linterval && curdeadline <= rinterval){
				taskcontri = lengthtask;
			}else if(ltime >= linterval && curdeadline >= rinterval){
				taskcontri = rinterval - ltime;
			}else{
				taskcontri = rinterval - linterval;
			}
			
			int currenttime =  lpart + rpart + taskcontri; 
			if(currenttime < mintime){
				mintime = currenttime;
				opt_start_time = ltime;
			}
			
		}
		minsubinterval[linterval][rinterval] = mintime;
		LTaskStart[linterval][rinterval] = opt_start_time;
//		task[longindex].setStart(opt_start_time);
//		setJobStart(longindex);
		
		return mintime;
	}
	
	public int machineSchedule(){
		//insert into queue based on start_time 
		Comparator<Task> cmp = new Comparator<Task>() {
		      public int compare(Task e1, Task e2) {
		    	if( e1.start_time != e2.start_time){  
		    		return e1.start_time - e2.start_time;
		    	}else{
		    		return e2.process_span - e1.process_span;
		    	}
		      }
		    };
		Queue<Task> q_task = new PriorityQueue<Task>(cmp);
		for(int i = 0; i < Constances.NUM_TASK; i++){
			q_task.add(task[i]);
		}
		
		//allocate to machine
		int m = 0;
		double bound = l_star*1.0/Constances.NUM_MACHINE + p_max ;
		int base_start = 0;
		int base_time = 0;
		while(! q_task.isEmpty()){
			Task job = q_task.poll();
			if( Constances.off_debug){
				System.out.println("machine schedule.....[" + job.release_time + "," + job.process_span
									+ "," +job.due_time + "]......["+ job.start_time + "," + (job.start_time+job.process_span) +"]");
			}
			//*******very important
			if(job.start_time + job.process_span <= base_start){
				continue;
			}
				
			//************
			if( job.start_time >= base_time){
				if( machine[m] + job.process_span <= bound){
					base_time = job.start_time + job.process_span;
					machine[m] += job.process_span;
					
					if(Constances.off_debug){
							System.out.println("......to[" +m + "," + base_time + "]" + machine[m]);
					}
				}else{
					if(Constances.off_debug){
						System.out.println("$$$$$$machine[" + m + "]:" + machine[m]);
						System.out.println("queque:" + q_task.size() + "...start time:" + q_task.peek().start_time );
						pause();
						if( m == Constances.NUM_MACHINE - 1 ){
							debugOffline(q_task);
						}
					}
					m += 1;
					machine[m] = job.process_span;
					base_start = job.start_time;
					base_time = job.start_time + job.process_span;
					
					if(Constances.off_debug){
						System.out.println("......to [" +m + "," + base_time + "]" + machine[m]);
					}
				}
				
			}else{
				
				int tem_load = 0;
				if(job.start_time + job.process_span > base_time){
					tem_load = job.start_time + job.process_span - base_time;
				}else{
					if(Constances.off_debug){
						System.out.println("......covered");
					}
					continue;
				}
				
				
				if(machine[m] + tem_load <= bound){
					base_time = job.start_time + job.process_span;
					machine[m] += tem_load;
					
					if(Constances.off_debug){
						System.out.println("......to[" +m + "," + base_time + "]" + machine[m]);
					}
				}else{
					//System.out.println("map["+ m + "]:" + machine[m]);
					if(Constances.off_debug){
						System.out.println("$$$$$$machine[" + m + "]:" + machine[m]);
						System.out.println("queque:" + q_task.size() + "...start time:" + q_task.peek().start_time );
						pause();
						if( m == Constances.NUM_MACHINE - 1 ){
							debugOffline(q_task);
						}
					}
					m += 1;
					machine[m] = job.process_span;
					base_start = job.start_time;
					base_time = job.start_time + job.process_span;	
					
					if(Constances.off_debug){
						System.out.println("......to[" +m + "," + base_time + "]" + machine[m]);
					}
				}
			}
			
			
		}
		
		int max = 0;
		for(int i = 0; i < Constances.NUM_MACHINE; i++ ){
			System.out.println("machine[" + i +"]:" + machine[i]);
			
			if ( machine[i] > max){
				max = machine[i];
			}
		}
		
		return max;
	}
	
	public int locateLongTask(int linterval, int rinterval){
		/*To locate the longest task in interval [linterval, rinterval]
		 * 
		 * Return: index of longest task; else return -1
		 * */
		int longindex = -1;
		int max = -1; // there might be no task in some subinterval
		
		// longest task in interval [linterval, rinterval] should satisfy:
		//(linterval < ri + li) &&  (rinterval > di-li)
		for(int i = 0; i < Constances.NUM_TASK; i++){
			if( (linterval < task[i].release_time + task[i].process_span) && (rinterval > task[i].due_time - task[i].process_span )){
				
				if(task[i].process_span >= max){
					max = task[i].process_span;
					longindex = i;
				}//if
			
			}//if
			
		}//for
		return longindex;
	}//locateLongTask
	

	public void setJobStart(int index){
		int s_t = task[index].start_time;
		int f_t = task[index].start_time + task[index].process_span;
		int p_t = task[index].process_span;
		for(int i = 0; i < Constances.NUM_TASK;i++){
			if(task[i].process_span <= p_t && task[i].start_time == -1){
				for( int time = task[i].release_time; time <= task[i].due_time-task[i].process_span; time++){
					if( time >= s_t && time +task[i].process_span <= f_t){
						task[i].start_time = time;
						task[i].cover = true;
						break;
					}
				}
			}
		}
		
	}
	
	public void setOptStart(int linterval, int rinterval){

		int index = locateLongTask(linterval, rinterval);
		
		if ( index == -1){
			return ;
		}else if( task[index].start_time == -1){
			task[index].start_time = LTaskStart[linterval][rinterval];
			setJobStart(index);			
			if( linterval < task[index].start_time){
				setOptStart(linterval, task[index].start_time);
			}
			if(task[index].start_time + task[index].process_span < rinterval){
				setOptStart(task[index].start_time + task[index].process_span, rinterval);
			}
			
		}
		
	}
	public void showSchedule(){
		
		System.out.println("Show start time......." );
		
		Comparator<Task> cmp = new Comparator<Task>() {
		      public int compare(Task e1, Task e2) {
		    	return e2.process_span - e1.process_span;		    	
		      }
		    };
		Queue<Task> q_task = new PriorityQueue<Task>(cmp);
		for(int i = 0; i < Constances.NUM_TASK; i++){
			q_task.add(task[i]);
		}
		System.out.println(q_task.size() );
		while(q_task.isEmpty() == false){
			Task job = q_task.poll();
			if( job.cover){
				System.out.println(".......[" + job.release_time + ", " + job.process_span + ", " + job.due_time + "....." + job.start_time);
			}else{
				System.out.println("[" + job.release_time + ", " + job.process_span + ", " + job.due_time + "....." + job.start_time);
			}
		}
	}
	
	public void pause(){
		try{
		    Thread thread = Thread.currentThread();
		    thread.sleep(2000);//暂停1.5秒后程序继续执行
		}catch (InterruptedException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} 
	}
	
	public void debugOffline(Queue<Task> qTasks){
		while( qTasks.isEmpty() == false){
			Task job = qTasks.poll();
			System.out.println("[" + job.start_time + ", " + (job.start_time + job.process_span) + "]");
		}
	}
	
	
	public void printLstar(){
		Comparator<Task> cmp = new Comparator<Task>() {
		      public int compare(Task e1, Task e2) {
		    	if( e1.start_time != e2.start_time){  
		    		return e1.start_time - e2.start_time;
		    	}else{
		    		return e2.process_span - e1.process_span;
		    	}
		      }
		    };
		Queue<Task> q_task = new PriorityQueue<Task>(cmp);
		for(int i = 0; i < Constances.NUM_TASK; i++){
			q_task.add(task[i]);
		}
		
		int opt = 0;
		int base = 0;
		while(q_task.isEmpty() == false){
			Task job = q_task.poll();
			
			if(job.start_time >= base){
				opt += job.process_span;
				base = job.start_time + job.process_span;
			}else if( job.start_time + job.process_span > base){
				opt+= job.process_span + job.start_time - base;
				base = job.process_span + job.start_time;
			}
		}
		
		System.out.println("single schedule: " + opt);
		
	}
}
