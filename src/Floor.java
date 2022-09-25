import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.MouseInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.Highlighter;

public class Floor {
	static JFrame f = new JFrame("Assembly Line Simulator");
	static JPanel p = new JPanel();

	static LinkedList<String> productNames = new LinkedList<String>();
	static LinkedList<Station> stations = new LinkedList<>();
	static LinkedList<Station[]> links = new LinkedList<>();
	static LinkedList<Link> oLinks = new LinkedList<>();
	static LinkedList<Product> products = new LinkedList<>();
	static LinkedList<Object[]> aQ = new LinkedList<>();

	static boolean mouseHeld = false;
	static int selectedType = -1;
	static int stationSelectedIndexOne = -1, stationSelectedIndexTwo = -1;
	static int dragX = -1, dragY = -1;
	static int movingIndex = -1;

	static int lingerRed = 0;

	static double proportionX = ((double)f.getContentPane().getWidth() / 1920.0), proportionY = ((double)f.getContentPane().getHeight() / 1080);

	static boolean stopPainting = false, keyControl = false, drawLinkNumbers = false, dayStarted = false, reportActive = false;

	static int standardTransferTime = 1000;

	static int globalEndtime = 0;

	public Floor() {
		initialize();
	}

	public void initialize() {
		initializeGraphics();
		f.addMouseListener(new MouseAdapter() { 
			@Override
			public void mousePressed(java.awt.event.MouseEvent arg0) {
				mouseHeld = true;
				double x = arg0.getX(), y = arg0.getY();
				if (Math.sqrt(Math.pow((100 * proportionX) - x, 2) + Math.pow((120 * proportionY) - y, 2)) < 50) {
					selectedType = 0;
					stationSelectedIndexOne = -1;
				} else {
					for (int i = 0; i < stations.size(); i++) {
						Station s = stations.get(i);
						if (Math.sqrt(Math.pow((stations.get(i).x * proportionX) - x, 2) + Math.pow((stations.get(i).y * proportionY) - y, 2)) < 25 * proportionX) {
							if (stationSelectedIndexOne == -1) {
								stationSelectedIndexOne = i;
								selectedType = 1;
								movingIndex = i;
								break;
							} else {
								stationSelectedIndexTwo = i;
								linkStations();
								break;
							}
						}
						if (i == stations.size() - 1) {
							if (stationSelectedIndexOne != -1) {
								AffineTransform affinetransform = new AffineTransform();     
								FontRenderContext frc = new FontRenderContext(affinetransform,true,true);
								Font font = new Font("Calibri", Font.PLAIN, (int)(40 * proportionX));
								int w = (int)(font.getStringBounds("IPKs: " + stations.get(stationSelectedIndexOne).IPKs, frc).getWidth());
								int h = (int)(font.getStringBounds("IPKs: " + stations.get(stationSelectedIndexOne).IPKs, frc).getHeight());
								if (x > (340 * proportionX) && y > (1030 * proportionY) - h && x < w + (340 * proportionX) && y < (1030 * proportionY)) {
									try {
										int inputCapacity = Integer.parseInt(JOptionPane.showInputDialog("Enter IPK Capacity", "" + stations.get(stationSelectedIndexOne).IPKs));
										stations.get(stationSelectedIndexOne).IPKs = inputCapacity;
										stationSelectedIndexOne = -1;
									} catch (Exception e) {
										System.out.println("Incorrect input for type IPK capacity integer.");
									}
								} else {
									font = new Font("Calibri", Font.PLAIN, (int)(20 * proportionX));
									w = (int)(font.getStringBounds("Type: " + stations.get(stationSelectedIndexOne).getType(), frc).getWidth());
									h = (int)(font.getStringBounds("Type: " + stations.get(stationSelectedIndexOne).getType(), frc).getHeight());
									if (x > (50 * proportionX) && y > (1050 * proportionY) - h && x < w + (50 * proportionX) && y < (1050 * proportionY)) {
										String option = (String)JOptionPane.showInputDialog(f, "Choose type", "Options", JOptionPane.PLAIN_MESSAGE, null, new String[] {"Feeder", "Standard", "End"}, stations.get(stationSelectedIndexOne).getType());
										if (option.equals("Feeder")) {
											stations.get(stationSelectedIndexOne).type = 1;
										} else if (option.equals("Standard")) {
											stations.get(stationSelectedIndexOne).type = 0;
										} else if (option.equals("End")) {
											stations.get(stationSelectedIndexOne).type = 2;
										}
										stationSelectedIndexOne = -1;
									} else {
										font = new Font("Calibri", Font.PLAIN, (int)(40 * proportionX));
										w = (int)(font.getStringBounds("Link Outputs by Name: ", frc).getWidth());
										h = (int)(font.getStringBounds("Link Outputs by Name: ", frc).getHeight());
										if (x > (340 * proportionX) && y > (1070 * proportionY) - h && x < w + (340 * proportionX) && y < (1070 * proportionY)) {
											drawLinkNumbers = true;
											String[] opts = new String[stations.get(stationSelectedIndexOne).next.size()];
											for (int g = 0; g < stations.get(stationSelectedIndexOne).next.size(); g++)
												opts[g] = "Link: " + g;
											String option = (String)JOptionPane.showInputDialog(f, "Select Output Types by Link", "Links", JOptionPane.PLAIN_MESSAGE, null, opts, "Link: 1");
											if (option != null) {
												opts = new String[productNames.size() + 1];
												for (int g = 0; g < productNames.size(); g++)
													opts[g + 1] = productNames.get(g);
												opts[0] = "All";
												JPanel gui = new JPanel(new BorderLayout());
												JList<String> list = new JList<String>(opts);
												gui.add(new JScrollPane(list));

												JOptionPane.showMessageDialog(
														null, 
														gui,
														"Select Product Names, or 'All'",
														JOptionPane.QUESTION_MESSAGE);
												int index = Integer.parseInt(option.substring(option.lastIndexOf(" ") + 1));
												getLink(links.get(index)[0], links.get(index)[1]).setAcceptedNames(list.getSelectedValuesList());
												if (list.getSelectedValuesList().size() == 0)
													getLink(links.get(index)[0], links.get(index)[1]).setAcceptedNames(Arrays.asList(new String[] {"All"}));
											}
											drawLinkNumbers = false;
										} else { 
											font = new Font("Calibri", Font.PLAIN, (int)(40 * proportionX));
											w = (int)(font.getStringBounds("Production Times", frc).getWidth());
											h = (int)(font.getStringBounds("Production Times", frc).getHeight());
											if (x > (730 * proportionX) && y > (1070 * proportionY) - h && x < w + (730 * proportionX) && y < (1070 * proportionY)) {
												String[][] data = new String[productNames.size()][3];
												JTable table = new JTable(data, new String[] {"Product Name", "Time (Minutes)", "Req Feed"})  {
													private static final long serialVersionUID = 1L;

													public boolean isCellEditable(int row, int column) {                
														if (column == 1 || column == 2) return true;     
														return false;
													};
												};
												JScrollPane pane = new JScrollPane(table);
												DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
												centerRenderer.setHorizontalAlignment( JLabel.CENTER );
												for (int f = 0; f < 2; f++)  
													table.getColumnModel().getColumn(f).setCellRenderer(centerRenderer);
												table.getColumnModel().getColumn(0).setPreferredWidth((int)(300 * proportionX));
												table.getTableHeader().setFont(new Font("Arial", Font.ITALIC, (int)(28 * proportionX)));
												table.setPreferredScrollableViewportSize(new Dimension((int)(1000 * proportionX), (int)(260 * proportionY)));
												table.setFillsViewportHeight(true);
												table.setRowHeight((int)(40 * proportionY));
												table.setBackground(new Color(190, 204, 226));
												table.getTableHeader().setReorderingAllowed(false);
												table.setFont(new Font("Arial", Font.PLAIN, (int)(40 * proportionX)));
												table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
												table.setRequestFocusEnabled(false);
												table.setAutoCreateColumnsFromModel(false);
												table.getTableHeader().setResizingAllowed(false);
												pane.setBounds((int)(20 * proportionX), (int)(100 * proportionY), (int)(760 * proportionX), (int)(260 * proportionY));
												table.setPreferredScrollableViewportSize(new Dimension((int)(760 * proportionX), (int)(260 * proportionY)));
												pane.setFont(new Font("Calibri", Font.PLAIN, 40));
												table.setFont(new Font("Calibri", Font.PLAIN, 40));
												for (int f = 0; f < productNames.size(); f++) 
													table.setValueAt(productNames.get(f), f, 0);
												JPanel panel = new JPanel();
												panel.add(pane);
												int result = JOptionPane.showConfirmDialog(null, panel, 
														"Please Enter Production Times For Each", JOptionPane.OK_CANCEL_OPTION);
												LinkedList<String> c1 = new LinkedList<>();
												LinkedList<String> c2 = new LinkedList<>();
												LinkedList<String> c3 = new LinkedList<>();
												for (int f = 0; f < data.length; f++) {
													c1.add((String)table.getValueAt(f, 0));
													c2.add((String)table.getValueAt(f, 1));
													c3.add((String)table.getValueAt(f, 2));
												}
												stations.get(stationSelectedIndexOne).addProductionTimes(c1, c2, c3);
											} else if (stations.get(stationSelectedIndexOne).type == Station.FEEDER) {
												font = new Font("Calibri", Font.PLAIN, (int)(40 * proportionX));
												w = (int)(font.getStringBounds("Day Customization", frc).getWidth());
												h = (int)(font.getStringBounds("Day Customization: ", frc).getHeight());
												if (x > (1560 * proportionX) && y > (1030 * proportionY) - h && x < w + (1560 * proportionX) && y < (1030 * proportionY)) {
													JTextField startTime = new JTextField();
													startTime.setMaximumSize(new Dimension(120, 60));
													startTime.setPreferredSize(new Dimension(120, 60));
													startTime.setFont(new Font("Calibri", Font.PLAIN, 50));
													JTextField endTime = new JTextField();
													endTime.setMaximumSize(new Dimension(120, 60));
													endTime.setPreferredSize(new Dimension(120, 60));
													endTime.setFont(new Font("Calibri", Font.PLAIN, 50));

													JTable table = new JTable(new String[][] {{"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}, {"", ""}}, new String[] {"Product Name", "Quantity"});
													JScrollPane pane = new JScrollPane(table);
													DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
													centerRenderer.setHorizontalAlignment( JLabel.CENTER );
													for (int f = 0; f < 2; f++)  
														table.getColumnModel().getColumn(f).setCellRenderer(centerRenderer);

													table.getColumnModel().getColumn(0).setPreferredWidth((int)(300 * proportionX));
													table.getTableHeader().setFont(new Font("Arial", Font.ITALIC, (int)(28 * proportionX)));
													table.setPreferredScrollableViewportSize(new Dimension((int)(1000 * proportionX), (int)(260 * proportionY)));
													table.setFillsViewportHeight(true);
													table.setRowHeight((int)(40 * proportionY));
													table.setBackground(new Color(190, 204, 226));
													table.getTableHeader().setReorderingAllowed(false);
													table.setFont(new Font("Arial", Font.PLAIN, (int)(40 * proportionX)));
													table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
													table.setRequestFocusEnabled(false);
													table.setAutoCreateColumnsFromModel(false);
													table.getTableHeader().setResizingAllowed(false);
													pane.setBounds((int)(20 * proportionX), (int)(100 * proportionY), (int)(760 * proportionX), (int)(260 * proportionY));
													table.setPreferredScrollableViewportSize(new Dimension((int)(760 * proportionX), (int)(260 * proportionY)));
													pane.setFont(new Font("Calibri", Font.PLAIN, 40));
													table.setFont(new Font("Calibri", Font.PLAIN, 40));


													JPanel myPanel = new JPanel();
													myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.Y_AXIS));
													myPanel.add(new JLabel("Start Time:"));
													myPanel.add(startTime);
													myPanel.add(Box.createVerticalStrut(15));
													myPanel.add(new JLabel("End Time:"));
													myPanel.add(endTime);
													myPanel.add(Box.createVerticalStrut(15));
													myPanel.add(pane);
													myPanel.add(Box.createVerticalStrut(15));

													int result = JOptionPane.showConfirmDialog(null, myPanel, 
															"Please Enter Day Data", JOptionPane.OK_CANCEL_OPTION);
													LinkedList<String> c1 = new LinkedList<>();
													LinkedList<String> c2 = new LinkedList<>();
													for (int f = 0; f < 13; f++) {
														if (!table.getValueAt(f, 0).equals(""))
															c1.add((String)table.getValueAt(f, 0));
														if (!table.getValueAt(f, 1).equals(""))
															c2.add((String)table.getValueAt(f, 1));
													}
													for (int f = 0; f < c1.size(); f++)
														if (!productNames.contains(c1.get(f)))
															productNames.add(c1.get(f));
													if (result == JOptionPane.OK_OPTION) 
														stations.get(stationSelectedIndexOne).initializeFeeder(startTime.getText(), endTime.getText(), c1, c2);	
												} else {
													stationSelectedIndexOne = -1;
													dragX = arg0.getX();
													dragY = arg0.getY();

												}
											} else {
												stationSelectedIndexOne = -1;
												dragX = arg0.getX();
												dragY = arg0.getY();
											}
										}
									}
								}
							} 
						}
					}
				}

				if (stations.size() == 0 && stationSelectedIndexOne != -1) {
					stationSelectedIndexOne = -1;
					dragX = arg0.getX();
					dragY = arg0.getY();
				}
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent arg0) {
				mouseHeld = false;
				proportionX = ((double)f.getContentPane().getWidth() / 1920.0); proportionY = ((double)f.getContentPane().getHeight() / 1057);
				if (selectedType == 0 && arg0.getX() > 0 && arg0.getX() < 1920 * proportionX && arg0.getY() > 250 * proportionY && arg0.getY() < (1080 - 100) * proportionY) {
					Station s = new Station(arg0.getX() * (1 / proportionX), arg0.getY() * (1 / proportionY));
					if (stations.size() == 0)
						s.type = Station.FEEDER;
					stations.add(s);
				} else if (selectedType == 1 && arg0.getX() > 0 && arg0.getX() < 1920 * proportionX && arg0.getY() > 250 * proportionY && arg0.getY() < (1080 - 100) * proportionY) {
					if (Math.abs(arg0.getX() - stations.get(movingIndex).x) > 25 || Math.abs(arg0.getY() - stations.get(movingIndex).y) > 25) {
						stations.get(movingIndex).x = arg0.getX() * (1 / proportionX);
						stations.get(movingIndex).y = arg0.getY() * (1 / proportionY);
					}
				}
				movingIndex = -1;
				selectedType = -1;
				dragX = -1;
				dragY = -1;
			}
		}); 
		f.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					if (stationSelectedIndexOne != -1) {
						Station s = stations.get(stationSelectedIndexOne);
						for (int i = 0; i < s.next.size(); i++)
							s.next.get(i).previous.remove(s);
						for (int j = 0; j < s.previous.size(); j++)
							s.previous.get(j).next.remove(s);
						for (int i = links.size() - 1; i >= 0; i--)
							for (int j = 0; j < links.get(i).length; j++)
								if (links.get(i)[j].equals(s)) {
									links.remove(i);
									break;
								}
						stations.remove(stationSelectedIndexOne);
						stationSelectedIndexOne = -1;
					}
				} else if (stationSelectedIndexOne != -1 && e.getKeyCode() == KeyEvent.VK_UP) {
					if (!keyControl)
						stations.get(stationSelectedIndexOne).y--;
					else
						stations.get(stationSelectedIndexOne).y -= 3;
				} else if (stationSelectedIndexOne != -1 && e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (!keyControl)
						stations.get(stationSelectedIndexOne).y++;
					else
						stations.get(stationSelectedIndexOne).y += 3;
				} else if (stationSelectedIndexOne != -1 && e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (!keyControl)
						stations.get(stationSelectedIndexOne).x--;
					else
						stations.get(stationSelectedIndexOne).x -= 3;
				} else if (stationSelectedIndexOne != -1 && e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (!keyControl)
						stations.get(stationSelectedIndexOne).x++;
					else
						stations.get(stationSelectedIndexOne).x += 3;
				} else if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					keyControl = true;
				} else if (keyControl && e.getKeyCode() == KeyEvent.VK_S) {
					dayStarted = true;
					int least = Integer.MAX_VALUE;
					for (int i = 0; i < stations.size(); i++) {
						if (stations.get(i).type == Station.FEEDER)
							if (least > stations.get(i).starttime)
								least = stations.get(i).starttime;
					}
					if (least == Integer.MAX_VALUE)
						least = 0;
					for (int i = 0; i < stations.size(); i++)
						stations.get(i).curtime = least;
					least = 0;
					for (int i = 0; i < stations.size(); i++) 
						if (stations.get(i).type == Station.FEEDER && stations.get(i).endtime > globalEndtime)
							globalEndtime = stations.get(i).endtime;

				} else if (keyControl && e.getKeyCode() == KeyEvent.VK_P) {
					dayStarted = false;
				} else if (e.getKeyCode() == KeyEvent.VK_R) {
					reportActive = true;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
					keyControl = false;
				}
			}
		});
	}

	public void initializeGraphics() {
		f.setResizable(true);
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		f.setMinimumSize(new Dimension(1920 / 2, 1080 / 2));
		p.setBackground(new Color(28, 33, 57));
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(p);
		paintInitial(f.getGraphics());

		Timer trimScanFeed = new Timer();
		trimScanFeed.schedule(new TimerTask() {
			@Override
			public void run() {
				paintInitial(f.getGraphics());
				if (reportActive)
					paintReport(f.getGraphics());
				oLinks = new LinkedList<>();
				for (int i = 0; i < links.size(); i++)
					oLinks.add(new Link(links.get(i)[0], links.get(i)[1]));
				products = new LinkedList<>();
				for (int i = 0; i < stations.size(); i++)
					if (stations.get(i).currentProduct != null)
						products.add(stations.get(i).currentProduct);
			}
		}, 0, 1);
		Timer tick = new Timer();
		tick.schedule(new TimerTask() {
			@Override
			public void run() {
				if (dayStarted) {
					for (int i = 0; i < stations.size(); i++) {
						stations.get(i).tick();
						aQ.addAll(stations.get(i).animationQueue);
						stations.get(i).animationQueue = new LinkedList<Object[]>();
					}
					for (int i = 0; i < stations.size(); i++) {
						if (!stations.get(i).isWorking && stations.get(i).IPKProducts.size() > 0 && stations.get(i).productionTime.containsKey(stations.get(i).IPKProducts.get(0).type)) {
							stations.get(i).IPKcap--;
							stations.get(i).currentProduct = stations.get(i).IPKProducts.remove(0);
							stations.get(i).isWorking = true;
							stations.get(i).progress = -1;
							stations.get(i).animationQueue.add(new Object[] {stations.get(i).currentProduct.prevStation, stations.get(i), stations.get(i).currentProduct, 2, 50});
						}
					}
				}

			}
		}, 0, 10);
	}

	public void paintReport(Graphics g) {
		g.setColor(new Color(100,100,100));
		g.fillRect(100, 100, f.getWidth() - 200, f.getHeight() - 200);
	}
	
	public void paintInitial(Graphics g) {
		try {
			proportionX = ((double)f.getContentPane().getWidth() / 1920.0); proportionY = ((double)f.getContentPane().getHeight() / 1057);
			BufferedImage bi = new BufferedImage((int)f.getSize().getWidth(), (int)f.getSize().getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D a = (Graphics2D)bi.getGraphics();
			a.setColor(new Color(28, 33, 57));
			a.fillRect(0,(int)(250 * proportionY),(int)(1920 * proportionX),(int)((1080 - 100) * proportionY));
			Queue<int[]> q = new LinkedList<int[]>();
			int[] first = {0,f.getContentPane().getHeight() / 2,20,f.getContentPane().getHeight() / 2, 0};
			int[] second = {0,f.getContentPane().getHeight() - (f.getContentPane().getHeight() / 5),20,f.getContentPane().getHeight() - (f.getContentPane().getHeight() / 5), 0};
			int[] third = {0,f.getContentPane().getHeight() / 5,20,f.getContentPane().getHeight() / 5, 0};
			q.add(first);q.add(second);q.add(third);
			a.setColor(new Color(48, 53, 77));
			int c = 0;
			while (!q.isEmpty() && c < 4000) {
				int[] d = q.poll();
				//a.drawLine(d[0], d[1], d[2], d[3]);
				int n = (int)(Math.random() * 3);
				int add = (int)(Math.random() * 30);
				if (n == 0) {
					int[] d1 = {d[2], d[3], d[2] + add, d[3], 0};
					if (d[2] + add > 0 && d[2] + add < f.getContentPane().getWidth())
						q.add(d1);
				} else if (n == 1) {
					int[] d2 = {d[2], d[3], d[2], d[3] + add, 1};
					if (d[3] + add > 0 && d[3] + add < f.getContentPane().getHeight())
						q.add(d2);
				} else {
					int[] d3 = {d[2], d[3], d[2], d[3] - add, 2};
					if (d[3] - add > 0 && d[3] - add < f.getContentPane().getHeight())
						q.add(d3);
				}
				c++;
			}
			a.setColor(new Color(50, 60, 100));
			a.fillRect(0,0,(int)(1920 * proportionX),(int)(250 * proportionY));
			a.setColor(new Color(50, 60, 100));
			a.fillRect(0,(int)((1080 - 100) * proportionY),(int)(1920 * proportionX),(int)(1080 * proportionY));
			a.setColor(new Color(255, 100, 100));
			a.fillOval((int)(50 * proportionX), (int)(70 * proportionY), (int)(100 * proportionX), (int)(100 * proportionX));
			a.setColor(Color.WHITE);
			a.setFont(new Font("Calibri", Font.PLAIN, (int)(40 * proportionX)));
			a.drawString("Station", (int)(44 * proportionX), (int)(210 * proportionY));
			if (selectedType == 0) {
				a.setColor(new Color(0, 128, 255));
				a.fillOval((int)((f.getMousePosition().getX()) - (int)(25 * proportionX)), (int)((f.getMousePosition().getY()) - (int)(25 * proportionX)), (int)(50 * proportionX), (int)(50 * proportionX));
			} else if (selectedType == 1) {
				a.setColor(new Color(0, 128, 255, 100));
				a.fillOval((int)((f.getMousePosition().getX()) - (int)(25 * proportionX)), (int)((f.getMousePosition().getY()) - (int)(25 * proportionX)), (int)(50 * proportionX), (int)(50 * proportionX));
			}
			for (int i = 0; i < links.size(); i++) {
				a.setColor(Color.BLACK);
				a.setStroke(new BasicStroke(Math.max(1, (int)(4 * proportionX))));
				Station one = (links.get(i)[0]);
				Station two = (links.get(i)[1]);
				a.drawLine((int)(one.x * proportionX), (int)(one.y * proportionY), (int)(two.x * proportionX), (int)(two.y * proportionY));
			}
			for (int i = 0; i < stations.size(); i++) {
				Station s = stations.get(i);
				for (int j = 0; j < s.next.size(); j++) {
					a.setColor(Color.BLACK);
					a.setStroke(new BasicStroke(Math.max(1, (int)(16 * proportionX))));
					a.drawLine((int)(s.x * proportionX), (int)(s.y * proportionY), (int)((((s.x * 3) + s.next.get(j).x) / 4) * proportionX), (int)(((s.next.get(j).y + (s.y * 3)) / 4) * proportionY));
				}
				if (s.type == Station.NORMAL)
					a.setColor(new Color(0, 128, 255));
				else if (s.type == Station.FEEDER)
					a.setColor(new Color(0, 255, 64));
				else if (s.type == Station.END)
					a.setColor(new Color(255, 100, 100));
				a.fillOval((int)(stations.get(i).x * proportionX) - (int)(25 * proportionX), (int)(stations.get(i).y * proportionY) - (int)(25 * proportionX), (int)(50 * proportionX), (int)(50 * proportionX));
				a.setStroke(new BasicStroke(Math.max(1, (int)(4 * proportionX))));
				if (s.IPKs > 0) {
					for (int j = 0; j < s.previous.size(); j++) {
						a.setColor(Color.GRAY);
						a.drawRect((int)((int)(s.x + s.previous.get(j).x) / 2 * proportionX) - (int)(10 * proportionX), (int)((int)(s.y + s.previous.get(j).y) / 2 * proportionY) - (int)(10 * proportionX), (int)(20 * proportionX), (int)(20 * proportionX));
						a.setColor(Color.WHITE);
						a.setFont(new Font("Calibri", Font.PLAIN, 15));
						a.drawString(s.IPKcap + "/" + s.IPKs, (int)((int)(s.x + s.previous.get(j).x) / 2 * proportionX) - (int)(10 * proportionX), (int)(((int)(s.y + s.previous.get(j).y) / 2 * proportionY) + (proportionX * 5)));
					}
				}
				if (drawLinkNumbers == true) {
					a.setColor(Color.WHITE);
					a.setFont(new Font("Calibri", Font.PLAIN, (int)(proportionX * 15)));
					for (int j = 0; j < s.next.size(); j++) {
						a.drawString(j + "", (int)((int)((s.x * 1.7) + s.next.get(j).x) / 2.7 * proportionX) - (int)(proportionX * 2), (int)((int)((s.y * 1.7) + s.next.get(j).y) / 2.7 * proportionY) + (int)(proportionX * 4));
					}
				}
			}
			a.setColor(Color.BLACK);
			LinkedList<Product> voidedProductDraw = new LinkedList<>();
			for (int i = 0; i < aQ.size(); i++) 
				voidedProductDraw.add((Product)aQ.get(i)[2]);

			for (int i = 0; i < products.size(); i++) {
				a.setColor(products.get(i).color);
				if (!voidedProductDraw.contains(products.get(i)))
					a.fillRect((int)(products.get(i).x * proportionX) - (int)(10 * proportionX), (int)(products.get(i).y * proportionY) - (int)(10 * proportionX), (int)(proportionX * 20), (int)(20 * proportionX));
			}
			for (int i = aQ.size() - 1; i >= 0; i--)  {
				a.setColor(((Product)aQ.get(i)[2]).color);
				a.fillRect((int)((int)((((Station)aQ.get(i)[0]).x * (Integer)(aQ.get(i)[4])) + ((Station)(aQ.get(i)[1])).x * (100 - (Integer)(aQ.get(i)[4]))) / 100 * proportionX) - (int)(proportionX * 2) - (int)(10 * proportionX), (int)((int)((((Station)(aQ.get(i)[0])).y * (Integer)(aQ.get(i)[4])) + ((Station)aQ.get(i)[1]).y * (100 - (Integer)(aQ.get(i)[4]))) / 100 * proportionY) + (int)(proportionX * 4) - (int)(10 * proportionX), 20, 20);
				aQ.get(i)[4] = (Integer)aQ.get(i)[4] - 10;
				if ((Integer)aQ.get(i)[4] < 50 && (Integer)aQ.get(i)[3] == 1)
					aQ.remove(i);
				else if ((Integer)aQ.get(i)[4] < 0)
					aQ.remove(i);
			}
			a.setFont(new Font("Calibri", Font.PLAIN, (int)(40 * proportionX)));
			if (stationSelectedIndexOne != -1) {
				Station s = stations.get(stationSelectedIndexOne);
				a.setColor(new Color(200, 200, 200));
				a.drawString("Station Index: " + stationSelectedIndexOne, (int)(50 * proportionX), (int)(1030 * proportionY));
				a.drawString("IPKs: " + s.IPKs, (int)(340 * proportionX), (int)(1030 * proportionY));
				a.drawString("Link Outputs by Name", (int)(340 * proportionX), (int)(1070 * proportionY));
				a.drawString("Production Times", (int)(730 * proportionX), (int)(1070 * proportionY));
				if (s.getType().equals("Feeder"))
					a.drawString("Day Customization", (int)(1560 * proportionX), (int)(1030 * proportionY));
				a.setFont(new Font("Calibri", Font.PLAIN, (int)(20 * proportionX)));
				a.drawString("Type: " + s.getType(), (int)(50 * proportionX), (int)(1050 * proportionY));
				a.setColor(new Color(200, 200, 200));
				a.setStroke(new BasicStroke(Math.max(1, (int)(3 * proportionX))));
				a.drawRect((int)((s.x * proportionX) - (25 * proportionX)), (int)((s.y * proportionY) - (25 * proportionX)), (int)(50 * proportionX), (int)(50 * proportionX));
			}
			if (dragX != -1 && dragY != -1) {
				a.setColor(new Color(103, 156, 170, 100));
				a.drawRect(dragX, dragY, (int)f.getMousePosition().getX() - dragX, (int)f.getMousePosition().getY() - dragY);
				a.setColor(new Color(173, 216, 230, 100));
				a.fillRect(dragX, dragY, (int)f.getMousePosition().getX() - dragX, (int)f.getMousePosition().getY() - dragY);
			}
			if (lingerRed > 0) {
				a.setColor(new Color(255, 0, 0, lingerRed * 4));
				a.fillRect(0,0,f.getContentPane().getWidth(),f.getContentPane().getHeight());
				lingerRed--;
			}
			a.setColor(Color.WHITE);
			a.setFont(new Font("Calibri", Font.PLAIN, (int)(40 * proportionX)));
			a.drawString("Product Names", (int)(200 * proportionX), (int)(150 * proportionY));
			if (stations.size() > 0 && stations.get(0) != null)
				a.drawString(stations.get(0).getStringTime(), (int)(1000 * proportionX), (int)(150 * proportionY));
			g.drawImage(bi, 0, 0, (int)f.getSize().getWidth(), (int)f.getSize().getHeight(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void linkStations() {
		proportionX = ((double)f.getContentPane().getWidth() / 1920.0); proportionY = ((double)f.getContentPane().getHeight() / 1057);
		if (stationSelectedIndexOne != -1 && stationSelectedIndexTwo != -1) {
			Station one = stations.get(stationSelectedIndexOne);
			Station two = stations.get(stationSelectedIndexTwo);
			int bx1 = (int)(one.x * proportionX), bx2 = (int)(two.x * proportionX), by1 = (int)(one.x * proportionY), by2 = (int)(two.y * proportionY);

			boolean valid = true;
			if (Math.abs(one.x - two.x) > Math.abs(one.y - two.y)) {
				if (two.x >= one.x) {
					if ((one.x + 100) * proportionX > (1920 * proportionX))
						valid = false;
					for (int i = 0; i < one.next.size(); i++) 
						if (Math.abs((one.x + 100) - one.next.get(i).x) < 25 && Math.abs(one.y - one.next.get(i).y) < 25)
							valid = false;
				}
				if (two.x < one.x) {
					if ((one.x - 100) * proportionX < 0)
						valid = false;
					for (int i = 0; i < one.next.size(); i++) 
						if (Math.abs((one.x - 100) - one.next.get(i).x) < 25 && Math.abs(one.y - one.next.get(i).y) < 25)
							valid = false;
				}
			} else {
				if (two.y >= one.y) {
					if ((one.y + 100) * proportionY > (1080 - 100) * proportionY)
						valid = false;
					for (int i = 0; i < one.next.size(); i++) 
						if (Math.abs((one.x) - one.next.get(i).x) < 25 && Math.abs((one.y + 100) - one.next.get(i).y) < 25)
							valid = false;
				}
				if (two.y < one.y) {
					if ((one.y - 100) * proportionY < 250 * proportionY)
						valid = false;
					for (int i = 0; i < one.next.size(); i++) 
						if (Math.abs((one.x) - one.next.get(i).x) < 25 && Math.abs((one.y - 100) - one.next.get(i).y) < 25)
							valid = false;
				}
			}
			valid = true;
			if (two.type != Station.FEEDER) {
				if (one.type != Station.END) {
					if (!two.containsPrevious(one) && !one.containsNext(two) && !two.containsNext(one) && !one.containsNext(two)) {
						if (!one.equals(two)) {
							if (valid) {
								if (Math.abs(one.x - two.x) >= Math.abs(one.y - two.y)) {
									two.y = one.y;
									if (two.x >= one.x)
										two.x = one.x + 100;
									else 
										two.x = one.x - 100;
									one.next.add(two);
									two.previous.add(one);
								} else {
									two.x = one.x;
									if (two.y >= one.y)
										two.y = one.y + 100;
									else
										two.y = one.y - 100;
									one.next.add(two);
									two.previous.add(one);
								}
								Station[] link = {stations.get(stationSelectedIndexOne), stations.get(stationSelectedIndexTwo)};
								links.add(link);
								stationSelectedIndexOne = -1;
								stationSelectedIndexTwo = -1;
							} else {
								System.out.println("Error: Out of bounds, or station is within another station.");
								lingerRed = 25;
								stationSelectedIndexOne = -1;
								stationSelectedIndexTwo = -1;
							}
						} else {
							System.out.println("Error: Same station selected for linkage.");
						}
					} else {
						System.out.println("Error: Stations already connected.");
						lingerRed = 25;
						stationSelectedIndexOne = -1;
						stationSelectedIndexTwo = -1;
					}
				} else {
					System.out.println("Error: Cannot connect a station of type 'End'.");
					lingerRed = 25;
					stationSelectedIndexOne = -1;
					stationSelectedIndexTwo = -1;
				}
			} else {
				System.out.println("Error: Cannot connect a station of type 'Feeder'.");
				lingerRed = 25;
				stationSelectedIndexOne = -1;
				stationSelectedIndexTwo = -1;
			}
		}
	}

	public Link getLink(Station i, Station o) {
		for (int f = 0; f < oLinks.size(); f++) 
			if (oLinks.get(f).in.equals(i) && oLinks.get(f).out.equals(o))
				return oLinks.get(f);
		return null;
	}

	public static void main(String args[]) {
		new Floor();
	}
}
