

public class Task {
	int release_time;
	int process_span;
	int due_time;
	int start_time = -1;
	int  lst = -1;
	boolean cover = false;
	
	Task(int r, int p, int d){
		release_time = r;
		process_span = p;
		due_time = d;
		start_time = -1;
		lst = d - p;
		cover = false;
	}
	Task(){
		release_time =0;
		process_span = 0;
		due_time = 0;
		start_time = -1;
		lst = due_time - process_span;
		cover = false;
	}
	
	public void setStart(int time){
		start_time = time;
	}
}
