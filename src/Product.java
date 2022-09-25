import java.awt.Color;

public class Product {
	public int position;
	public double x, y;
	public String type;
	public Station currentStation;
	public Station prevStation;
	public Color color;
	public Product rfPRoduct;
	
	public Product(double xi, double yi, String t) {
		x = xi;
		y = yi;
		type = t;
		int sumChars = 0;
		for (int i = 0; i < type.length(); i++)
			sumChars += type.charAt(i) * i;
		sumChars %= 255;
		color = new Color(sumChars, (sumChars * 2) % 255, (sumChars * 3) % 255);
	}
	
}
