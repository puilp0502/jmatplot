import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

class PlotPanel extends JPanel{
	private static final long serialVersionUID = -4877286922410746436L;
	private Expression exp;
	private int width, height;
	private double scaleX=50, scaleY=50;
	private double resolutionX;
	private Point origin; // Origin point(0,0) in swing coordination system
	private boolean doDrawGrid = false;
	private boolean doDrawAxisNumber = false;
	private boolean isMoved = false;
	
	private Point dragStartedAt = null;
	
	
	public void initialize(){
		resolutionX = 0.1/scaleX;
		
		System.out.println("Width:"+width);
		System.out.println("Height:"+height);
		System.out.println("scaleX:"+scaleX);
		System.out.println("scaleY:"+scaleY);
		System.out.println("resolutionX:"+resolutionX);
		System.out.println("Origin: ("+origin.x+", "+origin.y+")");
	}
	public PlotPanel(){
		super();
		this.addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent e) {
				width = PlotPanel.this.getWidth();
				height = PlotPanel.this.getHeight();
				if(!isMoved){
					origin = new Point(width/2, height/2);
				}
				initialize();
				repaint();
			}
			@Override
			public void componentShown(ComponentEvent e){
				width = PlotPanel.this.getWidth();
				height = PlotPanel.this.getHeight();
				origin = new Point(width/2, height/2);
				initialize();
				repaint();
			}
		});
		this.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseExited(MouseEvent e) {
				dragStartedAt = null;
			}
			@Override
			public void mousePressed(MouseEvent e) {
				dragStartedAt = new Point(e.getX(), e.getY());
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				dragStartedAt = null;
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseDragged(MouseEvent e) {
				if(dragStartedAt != null){
					Point delta = new Point(e.getX()-dragStartedAt.x, 
											e.getY()-dragStartedAt.y);
					System.out.println("Drag delta (swing): "+delta.toString());
					origin = origin.vAdd(delta);
					isMoved = true;
					repaint();
				}
				dragStartedAt = new Point(e.getX(), e.getY());
			}
			
		});
		this.addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int zoomlevel = e.getWheelRotation();
				if (zoomlevel < 0)
					scaleUp(1.1);
				else if (zoomlevel > 0){
					scaleDown(1.1);
				}
			}
		});
		width = this.getWidth();
		height = this.getHeight();
		this.origin = new Point(width/2, height/2);
		this.setExpression("x");
		this.initialize();
	}
	private Point getSwingCoordinate(Point p){
		/* convert mathematical coordinate to swing coordinate */
		return origin.vAdd(new Point(scaleX*p.x, -1*scaleY*p.y));
	}
	private Point getMathematicalCoordinate(Point p){
		/* convert swing coordinate to mathematical coordinate */
		return new Point((p.x-origin.x)/scaleX, (origin.y-p.y)/scaleY);
	}
	public double function(double x){
		return exp.setVariable("x", x).evaluate();
	}
	public void setExpression(String s){
		this.exp = new ExpressionBuilder(s).variables("x").build();
		repaint();
	}
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		drawAxis(g2);

		g2.setColor(Color.GRAY);
		g2.setStroke(new BasicStroke(2f));
		double minX = getMathematicalCoordinate(new Point(0, height)).x;
		double maxX = getMathematicalCoordinate(new Point(width, 0)).x;
		
		g2.setColor(new Color(53, 167, 255, 50));
		long start = System.currentTimeMillis();
		Point2D p0 = getSwingCoordinate(new Point(minX-resolutionX, function(minX-resolutionX))).toPoint2D();
		for(double x = minX; x<=maxX; x+=resolutionX){
			Point2D p1 = getSwingCoordinate(new Point(x, function(x))).toPoint2D();
			Shape l = new Line2D.Double(p0, p1);
			g2.draw(l);
			p0 = p1;
		}
		long taken = System.currentTimeMillis() - start;
		System.out.print(taken+"ms;");
	}
	private void drawAxis(Graphics2D g){
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(new Color(0,0,0));
		g2.setStroke(new BasicStroke(2f));

		Shape Xaxis = new Line2D.Double(0, origin.y, width, origin.y);
		Shape Yaxis = new Line2D.Double(origin.x, 0, origin.x, height);

		g2.draw(Xaxis); g2.draw(Yaxis);
		g2.dispose();

	}
	public void setScale(double scaleX, double scaleY){
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		initialize();
		repaint();
	}
	public double getScaleX(){
		return scaleX;
	}
	public double getScaleY(){
		return scaleY;
	}
	public void setDrawGrid(boolean b){
		this.doDrawGrid = b;
	}
	public void setDrawAxisNumber(boolean b){
		this.doDrawAxisNumber = b;
	}
	public void scaleUp(){
		this.scaleUp(1.2);
	}
	public void scaleUp(double scale){
		this.scaleX*=scale;
		this.scaleY*=scale;
		initialize();
		repaint();
	}
	public void scaleDown(){
		this.scaleDown(1.2);
	}
	public void scaleDown(double scale){
		this.scaleX/=scale;
		this.scaleY/=scale;
		initialize();
		repaint();
	}

}