package examples.manipulation;


import java.awt.Color;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import br.com.abby.core.view.FlyView;
import br.com.etyllica.commons.event.KeyEvent;
import br.com.etyllica.commons.event.MouseEvent;
import br.com.etyllica.commons.event.PointerEvent;
import br.com.etyllica.core.graphics.Graphics;
import br.com.luvia.core.context.ApplicationGL;
import br.com.luvia.core.graphics.Graphics3D;

public class PositionAxis extends ApplicationGL {

	protected int mx = 0;
	protected int my = 0;
	
	protected boolean click = false;

	protected FlyView view;

	private static final int NONE = -1;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;

	boolean drawRay = false;
	boolean drawBoundingBoxes = true;

	double tileSize = 1;
	int selected = NONE;
	int sx, sy;
	float value;

	float axisSize = 100f;
	float axisWidth = 2.5f;
	float speed = 0.1f;

	BoundingBox xAxis;
	BoundingBox yAxis;
	BoundingBox zAxis;
	private static final BoundingBox NO_AXIS = new BoundingBox();
	BoundingBox collisionAxis;

	Vector3 position = new Vector3();

	public PositionAxis(int w, int h) {
		super(w, h);
	}

	@Override
	public void init(Graphics3D drawable) {
		view = new FlyView(30, 3.6f, 0);
		view.getAim().setAngleY(190);

		GL2 gl = drawable.getGL2(); // get the OpenGL graphics context

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
		gl.glClearDepth(1.0f);      // set clear depth value to farthest
		gl.glEnable(GL.GL_DEPTH_TEST); // enables depth testing
		gl.glDepthFunc(GL.GL_LEQUAL);  // the type of depth test to do
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // best perspective correction
		gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);

		xAxis = new BoundingBox(new Vector3(0, -axisWidth/2, -axisWidth/2), new Vector3(axisSize, axisWidth/2, axisWidth/2));
		yAxis = new BoundingBox(new Vector3(-axisWidth/2, 0, -axisWidth/2), new Vector3(axisWidth/2, axisSize, axisWidth/2));
		zAxis = new BoundingBox(new Vector3(-axisWidth/2, -axisWidth/2, 0), new Vector3(axisWidth/2, axisWidth/2, axisSize));
	}

	private void drawAxis(GL2 gl) {

		//Draw Axis
		gl.glLineWidth(axisWidth);

		//Draw X Axis
		gl.glColor3d(1.0, 0.0, 0.0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3d(axisSize, 0, 0);
		gl.glEnd();

		//Draw Y Axis
		gl.glColor3d(0.0, 1.0, 0.0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3d(0, axisSize, 0);
		gl.glEnd();

		//Draw Z Axis
		gl.glColor3d(0.0, 0.0, 1.0);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(0.0, 0.0, 0.0);
		gl.glVertex3d(0, 0, axisSize);
		gl.glEnd();
	}

	private void drawRay(GL2 gl, Ray ray) {

		float axisSize = 50;

		Vector3 v = new Vector3(ray.direction);
		v.scl(axisSize);

		//Draw Camera Axis
		gl.glColor3d(0.0, 0.0, 1.0);

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(view.getX(), 1, view.getZ());
		gl.glVertex3d(view.getX()+v.x, view.getY()+v.y, view.getZ()+v.z);
		gl.glEnd();
	}

	@Override
	public void reshape(Graphics3D drawable, int x, int y, int width, int height) {

		GL2 gl = drawable.getGL2();
		GLU glu = drawable.getGLU();

		gl.glViewport((int)x, (int)y, (int)w, (int)h);

		gl.glMatrixMode(GL2.GL_PROJECTION);

		gl.glLoadIdentity();

		glu.gluPerspective(60.0, (double) w / (double) h, 0.1, 500.0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);

		gl.glLoadIdentity();
	}

	@Override
	public void updateKeyboard(KeyEvent event) {
		view.updateKeyboard(event);

		if(event.isKeyDown(KeyEvent.VK_R)) {
			drawRay = true;
		} else if(event.isKeyUp(KeyEvent.VK_R)) {
			drawRay = false;
		}
	}

	public void updateMouse(PointerEvent event) {

		mx = event.getX();
		my = event.getY();

		if(event.isButtonDown(MouseEvent.MOUSE_BUTTON_LEFT)) {

			if(!click) {
				click = true;

				if (collisionAxis == xAxis) {
					selected = X;
					sx = mx;
					sy = my;
					value = position.x;
				} else if(collisionAxis == yAxis) {
					selected = Y;
					sx = mx;
					sy = my;
					value = position.y;
				} else if(collisionAxis == zAxis) {
					selected = Z;
					sx = mx;
					sy = my;
					value = position.z;
				}
			}

		} else if(event.isButtonUp(MouseEvent.MOUSE_BUTTON_LEFT)) {
			click = false;
			selected = NONE;
		}

		if (selected != NONE) {

			int deltaX = mx-sx;
			int deltaY = my-sy;
			
			if (selected == X) {
				float offset = speed*deltaX;
				position.x = value-offset;
			} else if (selected == Y) {
				float offset = speed*deltaY;
				position.y = value-offset;
			} else if (selected == Z) {
				float offset = speed*(deltaX+deltaY)/2;
				position.z = value-offset;
			}

		}

		if(event.isButtonDown(MouseEvent.MOUSE_BUTTON_RIGHT)) {
			drawRay = true;
		} else if(event.isButtonUp(MouseEvent.MOUSE_BUTTON_RIGHT)) {
			drawRay = false;
		}
	}

	@Override
	public void display(Graphics3D drawable) {

		updateControls(0);

		GL2 gl = drawable.getGL().getGL2();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(1f, 1f, 1f, 1);

		//Transform by Aim
		drawable.aimCamera(view.getAim());
		
		//Draw Scene
		drawable.setColor(Color.BLACK);
		drawable.drawGrid(1, 150, 150);
		
		gl.glTranslatef(position.x, position.y, position.z);
		drawAxis(gl);

		if (selected == X || collisionAxis == xAxis) {
			gl.glColor3d(1.0, 1.0, 0.0);
		} else {
			gl.glColor3d(1.0, 0.0, 0.0);
		}
		drawable.drawBoundingBox(xAxis);

		if (selected == Y || collisionAxis == yAxis) {
			gl.glColor3d(1.0, 1.0, 0.0);
		} else {
			gl.glColor3d(0.0, 1.0, 0.0);
		}
		drawable.drawBoundingBox(yAxis);

		if (selected == Z || collisionAxis == zAxis) {
			gl.glColor3d(1.0, 1.0, 0.0);
		} else {
			gl.glColor3d(0.0, 0.0, 1.0);
		}
		drawable.drawBoundingBox(zAxis);

		Ray ray = drawable.getCameraRay(mx, my);
		if(drawRay) {
			drawRay(gl, ray);
		}

		if (Intersector.intersectRayBoundsFast(ray, xAxis)) {
			collisionAxis = xAxis;
		} else if (Intersector.intersectRayBoundsFast(ray, yAxis)) {
			collisionAxis = yAxis;
		} else if (Intersector.intersectRayBoundsFast(ray, zAxis)) {
			collisionAxis = zAxis;
		} else if(selected == NONE) {
			collisionAxis = NO_AXIS;
		}

		gl.glFlush();

	}


	@Override
	public void draw(Graphics g) {

		//Draw Gui
		g.setColor(Color.WHITE);
		g.drawStringShadow("Scene", 20, 60, Color.BLACK);
		g.drawStringShadow(Double.toString(view.getAim().getAngleY()), 20, 80, Color.BLACK);

		//orangeAim.simpleDraw(g, mx-orangeAim.getW()/2, my-orangeAim.getH()/2);
	}

	public void updateControls(long now) {		
		view.update(now);
	}

}