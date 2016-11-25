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
