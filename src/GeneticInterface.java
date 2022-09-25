import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

public class GeneticInterface {	
	Floor f;

	LinkedList<Line> population = new LinkedList<>();
	LinkedList<String> names = new LinkedList<>();
	LinkedList<String> quantities = new LinkedList<>();
	LinkedList<String[]> taskCatalog;
	double mutationRate;
	int populationSize;
	
	public GeneticInterface(LinkedList<String[]> tasks, int popSize, double mR, int starttime, int endtime, LinkedList<String> nam, LinkedList<String> quan) {
		names = nam;
		quantities = quan;
		taskCatalog = tasks;
		mutationRate = mR;
		populationSize = popSize;
		for (int i = 0; i < populationSize; i++) {
			population.add(new Line((tasks.size() / 3) + (int)((Math.random() * (tasks.size() / 10)) - (tasks.size() / 20)), tasks.size(), starttime, endtime, tasks));
		}
	}
	
	public void procedeGenerations(int n) {
		for (int i = 0; i < n; i++) {
			LinkedList<Line> offspring = new LinkedList<>();
			while (offspring.size() < population.size()) {
				Workcenter[] parentSet = selectParents();
				reproduce(parentSet[0], parentSet[1]);
			}
			LinkedList<Line> nextGeneration = new LinkedList<>();
			nextGeneration.addAll(offspring);
			nextGeneration.addAll(population);
			HashMap<Double, Integer> fitness_index = new HashMap<>();
			for (int j = 0; j < nextGeneration.size(); j++) 
				fitness_index.put(fitness(nextGeneration.get(j)), j);			
			mutate();
			for (int j = 0; j < fitness_index.size() - populationSize; j++)
				nextGeneration.remove(0);
			population = nextGeneration;
		}
	}
	
	public static double fitness(Line l) {
		double fComplexity, fEfficiency, fIdletime;
		
		return Math.random();
	}
	
	//Tournament/Roulette; compare fitnesses
	public Workcenter[] selectParents() {
		return null;
	}
	
	//Crossover data for chosen lines and crossover stations within each
	public void reproduce(Workcenter p1, Workcenter p2) {
		
	}
	
	//Perform based on 'mutationRate'
	public void mutate() {
		
	}
	
	//Combine the old parents and offspring
	public void merge() {
		
	}
	
	public void visualize(int index) {
		LinkedList<Station> stationsTemp = new LinkedList<>();
		LinkedList<String[]> stringLinks = new LinkedList<>();
		Line line = population.get(index);
		for (int i = 0; i < line.workcenters.size(); i++) {
			Station s = new Station((Math.floor(i / 3) * 100) + 410, ((i % 3) * 100) + 410);
			Workcenter w = line.workcenters.get(i);
			s.type = Integer.parseInt(w.chromosomes.get("type").get(0));
			s.IPKs = Integer.parseInt(w.chromosomes.get("ipk").get(0));
			if (s.type == 1) {
				s.initializeFeeder(Integer.parseInt(w.chromosomes.get("time").get(0)), Integer.parseInt(w.chromosomes.get("time").get(1)), names, quantities);
			}
			f.stations.add(s);
			stationsTemp.add(s);
		}
		for (int i = 0; i < line.workcenters.size(); i++) {
			LinkedList<String> a = new LinkedList<String>(), b = new LinkedList<String>(), c = new LinkedList<String>();
			for (int j = 0; j < line.workcenters.get(i).chromosomes.get("tasks").size(); j++) {
				a.add(taskCatalog.get(Integer.parseInt(line.workcenters.get(i).chromosomes.get("tasks").get(j)))[0]);
				b.add(taskCatalog.get(Integer.parseInt(line.workcenters.get(i).chromosomes.get("tasks").get(j)))[1]);
				c.add(taskCatalog.get(Integer.parseInt(line.workcenters.get(i).chromosomes.get("tasks").get(j)))[2]);
			}
			f.stations.get(i).addProductionTimes(a, b, c);
		}
		for (int i = 0; i < line.workcenters.size(); i++) {
			Workcenter w = line.workcenters.get(i);
			for (int j = 0; j < w.chromosomes.get("conn_back").size(); j++) {
				boolean safe = true;
				String[] pair = new String[] {w.chromosomes.get("conn_back").get(j), w.chromosomes.get("id").get(0)};
				for (int c = 0; c < stringLinks.size(); c++) 
					if ((stringLinks.get(c)[0].equals(pair[0]) && stringLinks.get(c)[1].equals(pair[1])) || (stringLinks.get(c)[0].equals(pair[1]) && stringLinks.get(c)[1].equals(pair[0])))  
						safe = false;
				if (safe) {
					stringLinks.add(pair);
					f.stationSelectedIndexOne = (line.getIndexById(Integer.parseInt(pair[0])));
					f.stationSelectedIndexTwo = (line.getIndexById(Integer.parseInt(pair[1])));
					f.linkStations();
				}
			}
			for (int j = 0; j < w.chromosomes.get("conn_forw").size(); j++) {
				boolean safe = true;
				String[] pair = new String[] {w.chromosomes.get("id").get(0), w.chromosomes.get("conn_forw").get(j)};
				for (int c = 0; c < stringLinks.size(); c++) 
					if ((stringLinks.get(c)[0].equals(pair[0]) && stringLinks.get(c)[1].equals(pair[1])) || (stringLinks.get(c)[0].equals(pair[1]) && stringLinks.get(c)[1].equals(pair[0]))) 
						safe = false;
				if (safe) {
					stringLinks.add(pair);
					f.stationSelectedIndexOne = (line.getIndexById(Integer.parseInt(pair[0])));
					f.stationSelectedIndexTwo = (line.getIndexById(Integer.parseInt(pair[1])));
					f.linkStations();
				}
			}
		}
		int n = 0, x = 210, y = 210;
		LinkedList<Integer> cur = new LinkedList<>();
		LinkedList<Integer> next = new LinkedList<>();
		for (int i = 0; i < line.allConnectionsForward.size(); i++) {
			if (Integer.parseInt(line.allConnectionsForward.get(i)[0]) == line.feederIndex)
				cur.add(i);
		}
		stationsTemp.get(line.feederIndex).x = 110;
		stationsTemp.get(line.feederIndex).y = 610;
		while (n < line.allConnectionsForward.size() && cur.size() != 0) {
			y = 410;
			for (int i = 0; i < cur.size(); i++) {
				stationsTemp.get(Integer.parseInt(line.allConnectionsForward.get(cur.get(i))[1])).x = x;
				stationsTemp.get(Integer.parseInt(line.allConnectionsForward.get(cur.get(i))[1])).y = y;
				next.add(Integer.parseInt(line.allConnectionsForward.get(cur.get(i))[1]));
				boolean moveDown = true;
				while (moveDown) {
					y += 100;
					//System.out.println(y);
					moveDown = false;
					for (int j = 0; j < stationsTemp.size(); j++) {
						if (j != cur.get(i) && (int)stationsTemp.get(j).x == x && (int)stationsTemp.get(j).y == y) {
							moveDown = true;
						}
					}
				}
				n++;
			}
			cur.clear();
			for (int i = 0; i < line.allConnectionsForward.size(); i++) {
				for (int j = 0; j < next.size(); j++) {
					if (Integer.parseInt(line.allConnectionsForward.get(i)[0]) == next.get(j) && !cur.contains(i))
						cur.add(i);
				}
			}
			next.clear();
			x += 100;
		}
		stationsTemp.get(line.endIndex).x = x;
		stationsTemp.get(line.endIndex).y = 610;
	}
	
	
	public static void main(String args[]) {
		LinkedList<String[]> tasks = new LinkedList<>();
		LinkedList<String> names = new LinkedList<>();
		names.add("a");
		LinkedList<String> quantities = new LinkedList<>();
		quantities.add("50");
		for (int i = 0; i < 20; i++) {
			tasks.add(new String[] {((char)(i + 97)) + "", (int)(Math.random() * 8 + 2) + "", ""});
		}
		GeneticInterface gi = new GeneticInterface(tasks, 20, 5, 0, 43200000, names, quantities);
		gi.f = new Floor();
		for (int i = 0; i < 1; i++) {
			gi.f.stations.clear();
			gi.f.links.clear();
			gi.f.stationSelectedIndexOne = -1;
			gi.f.stationSelectedIndexTwo = -1;
			try {
				gi.visualize(i);
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
