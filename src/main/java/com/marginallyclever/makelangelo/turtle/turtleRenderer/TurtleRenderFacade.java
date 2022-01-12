package com.marginallyclever.makelangelo.turtle.turtleRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TurtleRenderFacade implements PreviewListener {

	private static final Logger logger = LoggerFactory.getLogger(TurtleRenderFacade.class);

	private TurtleRenderer defaultRenderer = new DefaultTurtleRenderer();

	//private TurtleRenderer barberPole = new BarberPoleTurtleRenderer();

	//private MakelangeloFirmwareVisualizer viz = new MakelangeloFirmwareVisualizer(); 
	//viz.render(gl2, turtleToRender, settings);

	private TurtleRenderer myRenderer=defaultRenderer;
	
	private Turtle myTurtle = new Turtle();
	
	@Override
	public void render(GL2 gl2) {
		if(myTurtle.isLocked()) return;
		try {
			myTurtle.lock();
			
			TurtleMove previousMove = null;
			
			// the first and last command to show (in case we want to isolate part of the drawing)
			int first = 0;
			int last = myTurtle.history.size();
			// where we're at in the drawing (to check if we're between first & last)
			int showCount = 0;
			
			try {
				myRenderer.start(gl2);
				showCount++;

				for (TurtleMove m : myTurtle.history) {
					if(m==null) throw new NullPointerException();
					
					boolean inShow = (showCount >= first && showCount < last);
					switch (m.type) {
					case TurtleMove.TRAVEL:
						if (inShow && previousMove != null) {
							myRenderer.travel(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.DRAW:
						if (inShow && previousMove != null) {
							myRenderer.draw(previousMove, m);
						}
						showCount++;
						previousMove = m;
						break;
					case TurtleMove.TOOL_CHANGE:
						myRenderer.setPenDownColor(m.getColor());
						myRenderer.setPenDiameter(m.getDiameter());
						break;
					}
				}
			}
			catch(Exception e) {
				//Log.error(e.getMessage());
			}
			finally {
				myRenderer.end();
			}
		}
		catch(Exception e) {
			logger.error("Failed to render the turtle", e);
		}
		finally {
			if(myTurtle.isLocked()) {
				myTurtle.unlock();
			}
		}
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public void setTurtle(Turtle turtle) {
		this.myTurtle = turtle;
	}

	public void setRenderer(TurtleRenderer render) {
		this.myRenderer = render;
	}
}
