

import java.util.Random;

public class Simulation {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		double off_sum = 0;
		double on_sum = 0;
		double LB = 0;
		double LB_2 = 0;
		for(int i = 1; i<= Constances.LOOP; i++){
			System.out.println("Generating tasks......");
			Task[] task = generateTask();
			
	/*offline schedule*/
			Offline  off_schedule = new Offline();
			off_schedule.init_process(task);
			
			//L*
			int L_star = off_schedule.offlineSchedule(0, Constances.T_LATEST);
			off_schedule.setL(L_star);
			off_schedule.setOptStart(0, Constances.T_LATEST);
			//off_schedule.showSchedule();
			System.out.println("single offline: " + L_star + "....p_max:" + off_schedule.getMaxP());
			off_schedule.printLstar();

			int off_appro = off_schedule.machineSchedule();
			System.out.println("offline Schedule:" + off_appro +"\n");
			off_sum += off_appro;
			LB_2 += L_star*1.0/Constances.NUM_MACHINE + off_schedule.getMaxP();
			if(L_star*1.0/Constances.NUM_MACHINE > off_schedule.getMaxP()){
				LB += L_star*1.0/Constances.NUM_MACHINE;
			}else{
				LB += off_schedule.getMaxP();
			}
			
			
	/*online schedule*/
			Online  on_schedule =  new Online();
			on_schedule.init_process(task);
			on_schedule.onlineSchedule();
			int on_app = on_schedule.showSolution();
			on_sum += on_app;
		}
		
		System.out.println(LB/Constances.LOOP + "  " + off_sum/Constances.LOOP+ "  " 
						   + on_sum/Constances.LOOP + "  " + LB_2/Constances.LOOP );
			
	}
	
	
	
	
	public static Task[] generateTask(){
		/* To generate tasks 
		 * 
		 * Return: task[num][3]: r_i, p_i, d_i
		 * */
			Task[] task = new Task [Constances.NUM_TASK];
			Random rand = new Random();
				
			int release_time = 0;
			int task_span = 0;
			int task_length = 0; 
				
			for (int i = 0; i < Constances.NUM_TASK; i++){
				release_time = Math.abs(rand.nextInt(Constances.T_LATEST)); 
				task_span = Math.abs(rand.nextInt(Constances.T_LATEST - release_time)) + 1; //tasl_span >= 1
				task_length = Math.abs(rand.nextInt(Constances.P_MAX)) + 1; //task_length >= 1 
				if( task_length > task_span){
					task_length = task_span;			}
				
				 task[i] = new Task(release_time, task_length,release_time + task_span);
			}
		
//			Task[] task = new Task[10];
//			task[0] = new Task(13,3,18);
//			task[1] = new Task(17,1,18);
//			task[2] = new Task(2,5,14);
//			task[3] = new Task(4,2,7);
//			task[4] = new Task(3,1,12);
//			task[5] = new Task(13,1,15);
//			task[6] = new Task(12,2,14);
//			task[7] = new Task(19,1,20);
//			task[8] = new Task(1,8,17);
//			task[9] = new Task(10,1,11);
			return task;
		}

}
