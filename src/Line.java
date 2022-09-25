import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Line {
	LinkedList<String[]> allConnectionsForward = new LinkedList<>();
	LinkedList<String[]> allConnectionsBackward = new LinkedList<>();
	LinkedList<Workcenter> workcenters = new LinkedList<>();
	static int centerQuantity, taskQuantity;
	boolean hasFeeder = false, hasEnd = false;
	int feederIndex = -1, endIndex = -1;
	static int starttime;
	static int endtime;
	
	public Line(int amtCenters, int amtTasks, int st, int et, LinkedList<String[]> tasks) {
		//Generate work centers
		centerQuantity = amtCenters;
		taskQuantity = amtTasks;
		starttime = st;
		endtime = et;
		for (int i = 0; i < centerQuantity; i++) {
			workcenters.add(new Workcenter(amtTasks, i, st, et, this));
		}
		for (int i = 0; i < centerQuantity; i++) {
			workcenters.get(i).instantiateConnections();
		}
		
		//Setting index of feeder and end, removing impossible feeders and ends
		for (int i = workcenters.size() - 1; i >= 0; i--) {
			if (workcenters.get(i).chromosomes.get("type").get(0).equals("0")) {
				if (workcenters.get(i).chromosomes.get("conn_back").size() == 0 || workcenters.get(i).chromosomes.get("conn_forw").size() == 0)
					workcenters.remove(i);
			} else if (workcenters.get(i).chromosomes.get("type").get(0).equals("1")) {
				if (workcenters.get(i).chromosomes.get("conn_forw").size() == 0) {
					workcenters.remove(i);
					hasFeeder = false;
					feederIndex = -1;
				} else
					feederIndex = i;
				
			} else if (workcenters.get(i).chromosomes.get("type").get(0).equals("2")) {
				if (workcenters.get(i).chromosomes.get("conn_back").size() == 0) {
					workcenters.remove(i);
					hasEnd = false;
					endIndex = -1;
				} else
					endIndex = i;
			}
		}		
		

		//Removing impossible connections to feeder and end
		for (int i = 0; i < workcenters.size(); i++) {
			for (int j = workcenters.get(i).chromosomes.get("conn_forw").size() - 1; j >= 0; j--) {
				if (Integer.parseInt(workcenters.get(i).chromosomes.get("conn_forw").get(j)) == (feederIndex))
					workcenters.get(i).chromosomes.get("conn_forw").remove(j);
			}
			for (int j = workcenters.get(i).chromosomes.get("conn_back").size() - 1; j >= 0; j--) {
				if (Integer.parseInt(workcenters.get(i).chromosomes.get("conn_back").get(j)) == (endIndex))
					workcenters.get(i).chromosomes.get("conn_back").remove(j);
			}
		}
		
		//Assorting connections into line-universal dictionary
		HashSet<String[]> allConnectionsForwardTemp = new HashSet<>();
		HashSet<String[]> allConnectionsBackwardTemp = new HashSet<>();
		for (int i = 0; i < workcenters.size(); i++) {
			for (int j = workcenters.get(i).chromosomes.get("conn_forw").size() - 1; j >= 0; j--) {
				allConnectionsForwardTemp.add(new String[] {workcenters.get(i).chromosomes.get("id").get(0), workcenters.get(getIndexById(Integer.parseInt(workcenters.get(i).chromosomes.get("conn_forw").get(j)))).chromosomes.get("id").get(0)});
			}
		}
		allConnectionsForward.addAll(allConnectionsForwardTemp);
		for (int i = 0; i < workcenters.size(); i++) {
			for (int j = workcenters.get(i).chromosomes.get("conn_back").size() - 1; j >= 0; j--) {
				boolean toss = false;
				for (int c = 0; c < allConnectionsForwardTemp.size(); c++) {
					if (allConnectionsForward.get(c)[0].equals(workcenters.get(i).chromosomes.get("id").get(0)) && allConnectionsForward.get(c)[1].equals(workcenters.get(getIndexById(Integer.parseInt(workcenters.get(i).chromosomes.get("conn_back").get(j)))).chromosomes.get("id").get(0)))
						toss = true;
				}
				if (toss == false)
					allConnectionsBackwardTemp.add(new String[] {workcenters.get(i).chromosomes.get("id").get(0), workcenters.get(getIndexById(Integer.parseInt(workcenters.get(i).chromosomes.get("conn_back").get(j)))).chromosomes.get("id").get(0)});
				else
					workcenters.get(getIndexById(Integer.parseInt(workcenters.get(i).chromosomes.get("conn_back").remove(j))));
			}
		}
		allConnectionsBackward.addAll(allConnectionsBackwardTemp);
		
		for (int i = 0; i < allConnectionsBackward.size(); i++) {
			allConnectionsForward.add(new String[] {allConnectionsBackward.get(i)[1], allConnectionsBackward.get(i)[0]});
		}
	}
	
	public int getIndexById(int id) {
		for (int i = 0; i < workcenters.size(); i++) {
			if (Integer.parseInt(workcenters.get(i).chromosomes.get("id").get(0)) == id) {
				return i;
			}
		}
		return -1;
	}
}
