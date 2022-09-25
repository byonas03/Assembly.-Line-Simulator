import java.awt.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Station {
	public static final int NORMAL = 0, FEEDER = 1, END = 2;

	public double x, y;
	public int IPKs, IPKcap, type;
	public LinkedList<Product> IPKProducts = new LinkedList<Product>();
	public LinkedList<String> acceptedNames = new LinkedList<>();

	public int wasteTime;

	public boolean isWorking;
	public Product currentProduct;
	public Map<String, Integer> productionTime = new LinkedHashMap<>();
	public Map<String, String> productionRequirement = new LinkedHashMap<>();
	public int progress;

	//Type 1 Specific
	public int starttime, endtime;
	public int curtime;
	public int target;
	
	public int totalProduced;
	public LinkedList<Product> productionQueue = new LinkedList<>();

	public LinkedList<Station> next = new LinkedList<Station>(), previous = new LinkedList<Station>();
	public LinkedList<Object[]> animationQueue = new LinkedList<>();

	public Station(double ix, double iy) {
		x = ix;
		y = iy;
		IPKs = 0;
		IPKcap = 0;
		progress = 0;
		totalProduced = 0;
		isWorking = false;
	}

	public void initializeFeeder(String start, String end, LinkedList<String> names, LinkedList<String> quantities) {
		try {
			starttime = stringToMilli(start);
			curtime = stringToMilli(start);
			endtime = stringToMilli(end);
			for (int i = 0; i < names.size(); i++) {
				for (int j = 0; j < Integer.parseInt(quantities.get(i)); j++) {
					productionQueue.add(new Product(x, y, names.get(i)));
				}
			}
		} catch (Exception e) {
			System.out.println("Error: Initiailization of feeder improperly filled.");
			Floor.lingerRed = 25;
		}
	}
	
	public void initializeFeeder(int start, int end, LinkedList<String> names, LinkedList<String> quantities) {
		try {
			starttime = (start);
			curtime = (start);
			endtime = (end);
			for (int i = 0; i < names.size(); i++) {
				for (int j = 0; j < Integer.parseInt(quantities.get(i)); j++) {
					productionQueue.add(new Product(x, y, names.get(i)));
				}
			}
		} catch (Exception e) {
			System.out.println("Error: Initiailization of feeder improperly filled.");
			Floor.lingerRed = 25;
		}
	}
	
	public void addProductionTimes(LinkedList<String> c1, LinkedList<String> c2, LinkedList<String> c3) {
		for (int i = 0; i < c1.size(); i++) {
			if (!productionTime.containsKey(c1.get(i))) {
				if (c2.get(i) != null && !c2.get(i).equals(""))
					productionTime.put(c1.get(i), Integer.parseInt(c2.get(i)) * 60000);
				else 
					productionTime.remove(c1.get(i));
			} else {
				productionTime.remove(c1.get(i));
				if (c2.get(i) != null && !c2.get(i).equals(""))
					productionTime.put(c1.get(i), Integer.parseInt(c2.get(i)) * 60000);
			}
			if (!productionRequirement.containsKey(c1.get(i))) {
				if (c3.get(i) != null && !c3.get(i).equals(""))
					productionRequirement.put(c1.get(i), c3.get(i));
				else 
					productionRequirement.remove(c1.get(i));
			} else {
				productionRequirement.remove(c1.get(i));
				if (c3.get(i) != null && !c3.get(i).equals(""))
					productionRequirement.put(c1.get(i), c3.get(i));
			}
		}
	}

	public void tick() {
		if (curtime < Floor.globalEndtime)
			curtime += 5000;
		System.out.println(curtime);
		if ((type == FEEDER && curtime >= starttime && curtime < endtime) || (type != FEEDER)) {
			if (currentProduct != null && (((!productionRequirement.containsKey(currentProduct.type))) || ((productionRequirement.containsKey(currentProduct.type) && currentProduct.rfPRoduct != null && currentProduct.rfPRoduct.type.equals(productionRequirement.get(currentProduct.type)))))) {
				progress += 5000;
			} else if (currentProduct != null && (currentProduct.rfPRoduct == null || !currentProduct.rfPRoduct.type.equals(productionRequirement.get(currentProduct.type)))) {
				boolean done = false;
				for (int i = 0; i < IPKProducts.size(); i++) {
					if (IPKProducts.get(i).type.equals(productionRequirement.get(currentProduct.type))) {
						animationQueue.add(new Object[] {IPKProducts.get(i).prevStation, this, IPKProducts.get(i), 2, 50});
						currentProduct.rfPRoduct = IPKProducts.remove(i);
						IPKcap--;
						done = true;
						break;
					}
				}
				if (!done) {
					for (int i = 0; i < previous.size(); i++) {
						if (previous.get(i).progress > previous.get(i).productionTime.get(previous.get(i).currentProduct.type) && previous.get(i).currentProduct.type.equals(productionRequirement.get(currentProduct.type))) {
							animationQueue.add(new Object[] {previous.get(i), this, previous.get(i).currentProduct, 0, 100});
							currentProduct.rfPRoduct = previous.get(i).currentProduct;
							previous.get(i).currentProduct = null;
							previous.get(i).progress = -1;
							previous.get(i).isWorking = false;
							break;
						}
					}
				}
			}			
			if (isWorking && type == FEEDER && progress > productionTime.get(currentProduct.type))
				sendNext();
			if (productionQueue.size() > 0 && type == Station.FEEDER) {
				if (!isWorking) {
					isWorking = true;
					Product np = productionQueue.remove(0);
					np.x = x;
					np.y = y;
					totalProduced++;
					currentProduct = np;
				}
			}
			if (isWorking && type != FEEDER && progress > productionTime.get(currentProduct.type))
				sendNext();
		}
	}


	public void sendNext() {
		int indexSend = -1;
		if (type != Station.END) {
			boolean done = false;
			for (int i = 0; i < next.size(); i++) {
				if (!next.get(i).isWorking && next.get(i).productionTime.containsKey(currentProduct.type)) {
					indexSend = i;
					done = true;
					break;
				}
			}
			if (!done) {
				for (int i = 0; i < next.size(); i++) {
					if (next.get(i).IPKcap < next.get(i).IPKs) {
						Station n = next.get(i);
						animationQueue.add(new Object[] {this, n, currentProduct, 1, 100});
						n.IPKcap++;
						Product p = new Product(n.x, n.y, currentProduct.type);
						p.prevStation = this;
						p.currentStation = n;
						n.IPKProducts.add(p);	
						currentProduct = null;
						progress = -1;
						isWorking = false;
						break;
					}
				}
			}
			if (indexSend != -1 && isWorking && next.get(indexSend).productionTime.containsKey(currentProduct.type)) {
				Station n = next.get(indexSend);
				if (!n.isWorking) {
					n.isWorking = true; 
					n.currentProduct = new Product(n.x, n.y, currentProduct.type); n.progress = 0;
					n.currentProduct.currentStation = n;
					currentProduct = null;
					progress = -1;
					isWorking = false;
					animationQueue.add(new Object[] {this, n, n.currentProduct, 0, 100});
				}
			}
		} else {
			System.out.println(currentProduct.type);
			currentProduct = null;
			progress = -1;
			isWorking = false;
		}
	}

	public int stringToMilli(String s) {
		if (s.indexOf(":") == 1)
			s = "0" + s;
		if (s.length() > 5)
			return ((Integer.parseInt(s.substring(0,2)) * 3600000) + (Integer.parseInt(s.substring(3,5)) * 60000) + (Integer.parseInt(s.substring(6,8)) * 1000));
		else 
			return ((Integer.parseInt(s.substring(0,2)) * 3600000) + (Integer.parseInt(s.substring(3,5)) * 60000));
	}

	public String getType() {
		if (type == NORMAL)
			return "Standard";
		else if (type == FEEDER)
			return "Feeder";
		else if (type == END)
			return "End";
		return "";
	}

	public int getDayLengthMillis() {
		return endtime - starttime;
	}
	
	public String getStringTime() {
		int m = curtime;
		String hours = (m / 3600000) + "";
		m -= Integer.parseInt(hours) * 3600000;
		String minutes = (m / 60000) + "";
		m -= Integer.parseInt(minutes) * 60000;
		String seconds = (m / 1000) + "";
		if ((hours + "").length() == 1) hours = "0" + hours;
		if ((minutes + "").length() == 1) minutes = "0" + minutes;
		if ((seconds + "").length() == 1) seconds = "0" + seconds;
		return hours + ":" + minutes + ":" + seconds;
	}

	public boolean containsNext(Station s) {
		return next.contains(s);
	}

	public boolean containsPrevious(Station s) {
		return previous.contains(s);
	}
}