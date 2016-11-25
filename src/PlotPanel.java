import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

class PlotPanel extends JPanel{
	private static final long serialVersionUID = -4877286922410746436L;
	private Expression exp;
	private int width, height;
	private double scaleX=50, scaleY=50;
	private double unitX, unitY; // mathematical distance of 200 pixel (swing coordinate)
    private double label_gap_x, label_gap_y; // 축 숫자 간격
    private double grid_gap_x, grid_gap_y; // 눈금 간격
    private int gridperlabel_x, gridperlabel_y; // 축 숫자 사이 눈금 개수
	private double resolutionX;
	private Point origin; // Origin point(0,0) in swing coordination system
	private boolean doDrawGrid = true;
	private boolean doDrawAxisNumber = true;
	private boolean isMoved = false;
	
	private Point dragStartedAt = null;
	
	
	public void initialize(){
		resolutionX = 0.1/scaleX;
		unitX = 200/scaleX;
		unitY = 200/scaleY;

        if (doDrawAxisNumber || doDrawGrid){
            double bpx_x = Math.pow(10, Math.floor(Math.log10(unitX)));
            double bpx_y = Math.pow(10, Math.floor(Math.log10(unitY)));

            double sgn_x = unitX/bpx_x; int approx_sgn_x=1;
            double sgn_y = unitY/bpx_y; int approx_sgn_y=1; // Significands(가수)

            int[] sgn_candidate = {1, 2, 5, };

            double delta_min_x=10, delta_min_y=10; //delta cannot be bigger than 10
            for(int candidate : sgn_candidate){
                double delta = Math.abs(sgn_x-candidate);
                if (delta < delta_min_x){
                    approx_sgn_x = candidate;
                    delta_min_x = delta;
                }
                delta = Math.abs(sgn_y - candidate);
                if (delta < delta_min_y){
                    approx_sgn_y = candidate;
                    delta_min_y = delta;
                }
            }
            label_gap_x = approx_sgn_x * bpx_x;
            label_gap_y = approx_sgn_y * bpx_y;
            if(approx_sgn_x == 2) {
                gridperlabel_x = 4;
                grid_gap_x = label_gap_x / gridperlabel_x;
            } else {
                gridperlabel_x = 5;
                grid_gap_x = label_gap_x / gridperlabel_x;
            }
            if(approx_sgn_y == 2) {
                gridperlabel_y = 4;
                grid_gap_y = label_gap_y / gridperlabel_y;
            } else {
                gridperlabel_y = 5;
                grid_gap_y = label_gap_y / gridperlabel_y;
            }
            System.out.println("Label gap(x):"+label_gap_x);
            System.out.println("Label gap(y):"+label_gap_y);
        }

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
		g2.setStroke(new BasicStroke(3f));
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

        Point upleft = getMathematicalCoordinate(new Point(0,0));
        Point bottomright = getMathematicalCoordinate(new Point(width, height));

        if(doDrawGrid){
            g2.setColor(Color.GRAY);
            g2.setStroke(new BasicStroke(1f));
            double minx = (int)(upleft.x/grid_gap_x)*grid_gap_x;
            double maxx = ((int)(bottomright.x/grid_gap_x)+1)*grid_gap_x;
            for(double x=minx; x<=maxx; x+=grid_gap_x){
                if (Math.round(x/grid_gap_x)%gridperlabel_x==0) {
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setStroke(new BasicStroke(1f));
                }
                double swingX = getSwingCoordinate(new Point(x, 0)).x;
                g2.draw(new Line2D.Double(swingX, 0, swingX, height));
            }

            double miny = (int)(bottomright.y/grid_gap_y)*grid_gap_y;
            double maxy = (int)(upleft.y/grid_gap_y)*grid_gap_y;
            for(double y=miny; y<=maxy; y+=grid_gap_y){
                if (Math.round(y/grid_gap_y)%gridperlabel_y==0) {
                    g2.setStroke(new BasicStroke(2f));
                } else {
                    g2.setStroke(new BasicStroke(1f));
                }
                double swingY = getSwingCoordinate(new Point(0, y)).y;
                g2.draw(new Line2D.Double(0, swingY, width, swingY));
            }
        }


        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2.4f));

        Shape Xaxis = new Line2D.Double(0, origin.y, width, origin.y);
        Shape Yaxis = new Line2D.Double(origin.x, 0, origin.x, height);

        g2.draw(Xaxis); g2.draw(Yaxis);


        if (doDrawAxisNumber){
            g2.setStroke(new BasicStroke(1f));
            Font f = new Font("Arial", Font.BOLD, 18);
            FontRenderContext frc = g2.getFontRenderContext();
            double minx = (int)(upleft.x/label_gap_x)*label_gap_x;
            double maxx = ((int)(bottomright.x/label_gap_x)+1)*label_gap_x;
            for(double x=minx; x<=maxx; x+=label_gap_x){
                double swingX = getSwingCoordinate(new Point(x, 0)).x;
                TextLayout layout = new TextLayout(new DecimalFormat("####.######").format(x), f, frc);
                if(origin.y <= 0) {
                    layout.draw(g2, (float) swingX + 5, 18);
                } else if (0 < origin.y && origin.y < height-30){
                    layout.draw(g2, (float) swingX + 5, (float)origin.y+18);
                } else {
                    layout.draw(g2, (float) swingX + 5, height - 18);
                }
            }
            double miny = (int)(bottomright.y/label_gap_y)*label_gap_y;
            double maxy = (int)(upleft.y/label_gap_y)*label_gap_y;
            for(double y=miny; y<=maxy; y+=label_gap_y){
                double swingY = getSwingCoordinate(new Point(0, y)).y;
                TextLayout layout = new TextLayout(new DecimalFormat("####.######").format(y), f, frc);
                if(origin.x <= 0) {
                    layout.draw(g2, 5, (float)swingY+18);
                } else if (0 < origin.x && origin.x < width-30){
                    layout.draw(g2, (float) origin.x + 5, (float)swingY+18);
                } else {
                    layout.draw(g2, (float)(width-layout.getBounds().getWidth()-5), (float)swingY+18);
                }
            }
        }
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
        repaint();
	}
	public void setDrawAxisNumber(boolean b){
		this.doDrawAxisNumber = b;
        repaint();
	}
	public void scaleUp(){
		this.scaleUp(1.2);
	}
	public void scaleUp(double scale){
        if(scaleX<1.8E8 && scaleY<1.8E8) {
            this.scaleX *= scale;
            this.scaleY *= scale;
            initialize();
            repaint();
        }
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