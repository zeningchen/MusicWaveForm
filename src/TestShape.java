import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.ArrayList;

public class TestShape implements Shape
{
	Ellipse2D.Float templateEllipse;
	Line2D.Float templateLine;
	double coordinates[];
	ArrayList<double[]> coordArray;
	GeneralPath gp;
	
	private void debugPathCoords()
	{
		for(int i = 0; i < coordArray.size(); i++)
		{
			if(coordArray.get(i) != null)
			{
				double [] coord_t = coordArray.get(i);
				System.out.println("Type= " + coord_t[0] + " x=" + coord_t[1] + " y=" + coord_t[2]);
			}
		}
	}
	
	private void setPath()
	{
		PathIterator pi;
		System.out.format("%d,%d,%d,%d,%d\n", PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO, PathIterator.SEG_QUADTO, 
				PathIterator.SEG_CUBICTO, 
					PathIterator.SEG_CLOSE);
		pi = templateEllipse.getPathIterator(null);
		gp.append(pi, true);
		pi = templateLine.getPathIterator(null);
		gp.append(pi, true);
	}
	
	public TestShape(float x, float y, float w, float h)
	{
		templateEllipse = new Ellipse2D.Float(x, y, w, h);
		Point2D.Float mp1 = new Point2D.Float(x,y + h/2);
		Point2D.Float mp2 = new Point2D.Float(x + w, y + h/2);
		templateLine = new Line2D.Float(mp1, mp2);
		coordinates = new double[6];
		coordArray = new ArrayList<double[]>();
		gp = new GeneralPath();
		setPathCoords();
	}
	
	public void setPathCoords()
	{
		if(templateEllipse != null)
		{
			setPath();
		}
		debugPathCoords();
	}
	
	public void fillGeneralShape()
	{
		
	}

	@Override
	public boolean contains(Point2D p) {
		// TODO Auto-generated method stub
		return templateEllipse.contains(p);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		// TODO Auto-generated method stub
		return templateEllipse.contains(r);
	}

	@Override
	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return templateEllipse.contains(x, y);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return templateEllipse.contains(x,y,w,h);
	}

	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return templateEllipse.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		// TODO Auto-generated method stub
		return templateEllipse.getBounds2D();
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		return gp.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		// TODO Auto-generated method stub
		return gp.getPathIterator(at, flatness);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return false;
	}
}

