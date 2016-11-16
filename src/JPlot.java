import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;


class Point{
	double x, y;
	public Point(double x, double y){
		this.x = x;
		this.y = y;
	}
	public Point vAdd(Point p){
		return new Point(this.x+p.x,this.y+p.y);
	}
	public Point2D toPoint2D(){
		return new Point2D.Double(this.x, this.y);
	}
	public String toString(){
		return "("+x+", "+y+")";
	}
}
public class JPlot extends JFrame {
	private static final long serialVersionUID = -2946950416181717394L;
	public static final int WIDTH = 600, HEIGHT=400;
	private PlotPanel plotPanel;
	JTextField expression;
	JButton btnEval;
	JPanel mainPanel;
	public JPlot(){
		this.setLayout(new BorderLayout());
		plotPanel = new PlotPanel();
		this.add(plotPanel, BorderLayout.CENTER);
		JPanel ctrlPanel = new JPanel();
		ctrlPanel.setLayout(new FlowLayout());
		JLabel y_equals = new JLabel("y = ");
		expression = new JTextField("x", 25);
		btnEval = new JButton("Draw");
		JButton btnScaleUp = new JButton("+");
		btnScaleUp.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				plotPanel.scaleUp();
			}
		});
		JButton btnScaleDown = new JButton("-");
		btnScaleDown.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
				plotPanel.scaleDown();
			}
		});
		ctrlPanel.add(y_equals);
		ctrlPanel.add(expression);
		ctrlPanel.add(btnEval);
		ctrlPanel.add(btnScaleUp);
		ctrlPanel.add(btnScaleDown);
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