import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.swing.text.html.BlockView;

public class Online {
	// Task [] task = new Task[Constances.NUM_TASK];
	int[] machine = new int[Constances.NUM_MACHINE];
	int [] dom_end = new int[Constances.NUM_MACHINE];
	int[] block_end = new int[Constances.NUM_MACHINE];
	int[] block_start = new int[Constances.NUM_MACHINE];
	// J
	Comparator<Task> cmp_generate = new Comparator<Task>() {
		public int compare(Task e1, Task e2) {
			return e1.release_time - e2.release_time;
		}
	};
	Queue<Task> q_task = new PriorityQueue<Task>(cmp_generate);
	Queue<Task> q_solution = new PriorityQueue<Task>();

	// Inintialization
	public void init_process(Task[] tem_task) {
		for (int i = 0; i < Constances.NUM_TASK; i++) {
			q_task.add(tem_task[i]);
		}

		for (int i = 0; i < Constances.NUM_MACHINE; i++) {
			machine[i] = 0;
			block_end[i] = 0;
			block_start[i] = 0;
			dom_end[i] = 0;
		}
	}

	// online schedule
	public void onlineSchedule() {
		// J'
		Comparator<Task> cmp_waiting = new Comparator<Task>() {
			public int compare(Task e1, Task e2) {
				if (e1.lst != e2.lst) {
					return e1.lst - e2.lst;
				} else {
					return e2.process_span - e1.process_span;
				}
			}
		};
		Queue<Task> q_waiting = new PriorityQueue<Task>(cmp_waiting);

		for (int t = 0; t < Constances.T_LATEST; t++) {

			// A new task arrives
			while (!q_task.isEmpty() && t == q_task.peek().release_time) {
				// System.out.println("A new job arrives......" + t);
				Task new_job = q_task.poll();

				// allocate to machine
				boolean to_machine = false;
				for (int i = 0; i < Constances.NUM_MACHINE; i++) {
					to_machine = checkInsert(new_job, i);

					if (to_machine) {
						if (Constances.on_debug) {
							System.out.println("new_task[" + t + "," + i + "]:"
									+ "...[" + new_job.release_time + ","
									+ new_job.process_span + ","
									+ new_job.due_time + "]");
						}
						break;
					}
				}// for

				if (to_machine == false) {
					q_waiting.add(new_job);
				}

			}// while

			while (!q_waiting.isEmpty() && t == q_waiting.peek().lst) {
				// System.out.println("A dominant job generates......" + t);
				// locate the minimum machine
				int m = locateMinmachine();

				Task dom_job = q_waiting.poll();
				machine[m] += dom_job.process_span;
				dom_end[m] = dom_job.due_time;
				block_end[m] = dom_job.due_time;
				block_start[m] = dom_job.lst;
				//
				if (Constances.on_debug) {
					System.out.println("dom_task[" + t + "," + m + "]:"
							+ "...[" + dom_job.release_time + ","
							+ dom_job.process_span + "," + dom_job.due_time
							+ "]");
					System.out.println("machine[" + m + "]:...["
							+ block_start[m] + "," + block_end[m] + "]");
				}
				//

				// ±éÀúJ'
				Queue<Task> q_tem = new PriorityQueue<Task>(cmp_waiting);
				while (!q_waiting.isEmpty()) {
					Task cur_job = q_waiting.poll();
					if (false == domCheckInsert(cur_job, m)) {
						q_tem.add(cur_job);

					} else if (Constances.on_debug) {

						System.out.println("...appendix job[" + t + "," + m
								+ "," + machine[m] + "]:" + "...["
								+ cur_job.release_time + ","
								+ cur_job.process_span + "," + cur_job.due_time
								+ "]");
					}
				}

				while (!q_tem.isEmpty()) {
					q_waiting.add(q_tem.poll());
				}
			}
		}
	}

	public void testPrint() {
		System.out.println("print task.......");
		while (!q_task.isEmpty()) {
			Task job = q_task.poll();
			System.out.println("[" + job.release_time + ", " + job.process_span
					+ ", " + job.due_time + "....." + job.start_time);
		}
	}

	public int showSolution() {
		int max = 0;
		for (int i = 0; i < Constances.NUM_MACHINE; i++) {
			System.out.println("machine[" + i + "]:" + machine[i]);
			if (machine[i] > max) {
				max = machine[i];
			}

		}

		System.out.println("online schedule:" + max);
		return max;
	}

	public int locateMinmachine() {
		int index = -1;
		int min = Constances.T_LATEST;
		for (int i = 0; i < Constances.NUM_MACHINE; i++) {
			if (machine[i] < min) {
				index = i;
				min = machine[i];
			}
		}

		return index;
	}

	public boolean checkInsert(Task job, int m) {
		/*
		 * check if a new task job can inserted to machine m 
		 * by comparing with dom_task of machine m*/

		if (job.release_time < dom_end[m]
				&& (dom_end[m] - job.release_time) >= job.process_span * 0.5) {

			job.setStart(job.release_time);
			int finish_time = job.release_time + job.process_span;

			if (finish_time > block_end[m]) {
				machine[m] += finish_time - block_end[m];
				block_end[m] = finish_time;
			}// if

			return true;
		}// if

		return false;
	}

	public boolean domCheckInsert(Task job, int m) {
		/*
		 * check if job in waiting queue can form block with dom_task in machine m
		 * */

		if (job.release_time <= block_start[m]) {
			if ((dom_end[m] - block_start[m]) >= job.process_span * 0.5) {
				job.setStart(block_start[m]);
				int finish_time = job.start_time + job.process_span;
				if (finish_time > block_end[m]) {
					machine[m] += finish_time - block_end[m];
					block_end[m] = finish_time;
				}// if

				return true;
			} else {
				return false;
			}

		} else if (job.release_time < block_end[m]) {
			if ((dom_end[m] - job.release_time) >= job.process_span * 0.5) {
				job.setStart(job.release_time);
				int finish_time = job.start_time + job.process_span;

				if (finish_time > block_end[m]) {
					machine[m] += finish_time - block_end[m];
					block_end[m] = finish_time;
				}// if

				return true;
			} else {
				return false;
			}
		}// if

		return false;
	}
}
