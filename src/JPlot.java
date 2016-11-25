import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;


public class JPlot extends JFrame {
	private static final long serialVersionUID = -2946950416181717394L;
	public static final int WIDTH = 800, HEIGHT=600;
	private PlotPanel plotPanel;
	JTextField expression;
	JButton btnEval;
	JCheckBox btnDrawGrid, btnDrawAxisNumber;
	JPanel mainPanel;
	public JPlot(){
		System.out.println(Math.PI);
		this.setLayout(new BorderLayout());
		plotPanel = new PlotPanel();
		this.add(plotPanel, BorderLayout.CENTER);
		JPanel ctrlPanel = new JPanel();
		ctrlPanel.setLayout(new FlowLayout());
		JLabel y_equals = new JLabel("y = ");
		expression = new JTextField("x", 25);
		btnEval = new JButton("Draw");
		btnDrawGrid = new JCheckBox("Grid");
		btnDrawGrid.setSelected(true);
		btnDrawAxisNumber = new JCheckBox("Axis Number");
		btnDrawAxisNumber.setSelected(true);
		btnDrawGrid.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotPanel.setDrawGrid(((JCheckBox)(e.getSource())).isSelected());
			}
		});
		btnDrawAxisNumber.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				plotPanel.setDrawAxisNumber(((JCheckBox)e.getSource()).isSelected());
			}
		});
//		JButton btnScaleUp = new JButton("+");
//		btnScaleUp.addMouseListener(new MouseAdapter(){
//			@Override
//			public void mouseClicked(MouseEvent e){
//				plotPanel.scaleUp();
//			}
//		});
//		JButton btnScaleDown = new JButton("-");
//		btnScaleDown.addMouseListener(new MouseAdapter(){
//			@Override
//			public void mouseClicked(MouseEvent e){
//				plotPanel.scaleDown();
//			}
//		});
		ctrlPanel.add(y_equals);
		ctrlPanel.add(expression);
		ctrlPanel.add(btnEval);
//		ctrlPanel.add(btnScaleUp);
//		ctrlPanel.add(btnScaleDown);
		ctrlPanel.add(btnDrawGrid);
		ctrlPanel.add(btnDrawAxisNumber);
		btnEval.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				plotPanel.setExpression(expression.getText());
			}
		});
		expression.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e){
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					plotPanel.setExpression(expression.getText());
				}
			}
		});
		this.add(ctrlPanel, BorderLayout.NORTH);
		this.setTitle("JMatPlot");
		this.setSize(WIDTH, HEIGHT);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	public static void main(String[] args){
		new JPlot();
	}
}