import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Workcenter {
	HashMap<String, LinkedList<String>> chromosomes = new HashMap<>();

	public Workcenter(int amtTasks, int index, int st, int et, Line l) {
		LinkedList<String> typeGenes = new LinkedList<>();
		if (l.hasEnd && l.hasFeeder)
			typeGenes.add("0");
		else if (l.hasEnd && !l.hasFeeder) {
			typeGenes.add("1");
			l.hasFeeder = true;
			LinkedList<String> timeGenes = new LinkedList<>();
			timeGenes.add(st + "");
			timeGenes.add(et + "");
			chromosomes.put("time", timeGenes);
		} else if (!l.hasEnd && l.hasFeeder) {
			typeGenes.add("2");
			l.hasEnd = true;
		} else {
			int N = (int)Math.floor(Math.random() * 2);
			if (N == 0) {
				l.hasFeeder = true;
				LinkedList<String> timeGenes = new LinkedList<>();
				timeGenes.add(st + "");
				timeGenes.add(et + "");
				chromosomes.put("time", timeGenes);
				typeGenes.add(1 + "");
			}
			else if (N == 1) {
				l.hasEnd = true;
				typeGenes.add(2 + "");
			}
		}
		chromosomes.put("type", typeGenes);

		LinkedList<String> idGenes = new LinkedList<>();
		idGenes.add(new DecimalFormat("000").format(index));
		chromosomes.put("id", idGenes);

		LinkedList<String> taskGenes = new LinkedList<>();
		for (int i = 0; i < 3; i++)
			taskGenes.add(randomDecimalBetween(0, amtTasks - 1, 3));
		chromosomes.put("tasks", taskGenes);

		LinkedList<String> ipkGenes = new LinkedList<>();
		ipkGenes.add("" + randomDecimalBetween(0, 10, 2));
		chromosomes.put("ipk", ipkGenes);
	}

	public void instantiateConnections() {
		LinkedList<String> connectionGenes = new LinkedList<>();
		String[] possible = new String[Line.centerQuantity];
		for (int i = 0; i < Line.centerQuantity; i++) 
			possible[i] = new DecimalFormat("000").format(i);
		if (!chromosomes.get("type").get(0).equals("1")) {
			for (int i = 0; i < Math.ceil(Math.random() * 2); i++) {
				int choice = (int)Math.floor(Math.random() * possible.length);
				if (!possible[choice].equals("used") && !possible[choice].equals(chromosomes.get("id").get(0))) {
					connectionGenes.add(possible[choice]);
					possible[i] = "used";
				} else {
					i--;
				}
			}
		}
		chromosomes.put("conn_back", connectionGenes);

		LinkedList<String> connectionGenes2 = new LinkedList<>();
		if (!chromosomes.get("type").get(0).equals("2")) {
			for (int i = 0; i < Math.ceil(Math.random() * 2); i++) {
				int choice = (int)Math.floor(Math.random() * possible.length);
				if (!possible[choice].equals("used") && !possible[choice].equals(chromosomes.get("id").get(0))) {
					connectionGenes2.add(possible[choice]);
					possible[i] = "used";
				} else {
					i--;
				}
			}
		}
		chromosomes.put("conn_forw", connectionGenes2);
	}

	public static String randomDecimalBetween(int lower, int upper, int stop) {
		DecimalFormat df = null;
		if (stop == -1)
			return ThreadLocalRandom.current().nextInt(lower, upper + 1) + "";
		else if (stop == 0) {
			String z = "";
			for (int i = 0; i < (upper + "").length(); i++)
				z += "0";
			df = new DecimalFormat(z);
		} else if (stop > 0) {
			String z = "";
			for (int i = 0; i < stop; i++)
				z += "0";
			df = new DecimalFormat(z);
		}
		return df.format(ThreadLocalRandom.current().nextInt(lower, upper + 1)) + "";
	}
}
