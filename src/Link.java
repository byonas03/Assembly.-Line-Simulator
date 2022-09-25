import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class Link {
	public Station in, out;
	public int IPKs;
	public LinkedList<String> acceptedNames = new LinkedList<String>();

	public Link(Station i, Station o) {
		in = i;
		out = o;
		IPKs = o.IPKs;
		acceptedNames.add("All");
	}

	public void setAcceptedNames(Collection<String> s) {
		acceptedNames = new LinkedList<String>();
		acceptedNames.addAll(s);
	}
}
