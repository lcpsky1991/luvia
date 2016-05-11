package examples.custom;


import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;
import static javax.media.opengl.GL.GL_TEXTURE_MIN_FILTER;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import br.com.abby.core.loader.MeshLoader;
import br.com.abby.linear.AimPoint;
import br.com.etyllica.core.event.KeyEvent;
import br.com.etyllica.core.event.MouseButton;
import br.com.etyllica.core.event.PointerEvent;
import br.com.etyllica.core.graphics.Graphic;
import br.com.luvia.core.context.ApplicationGL;
import br.com.luvia.core.video.Graphics3D;
import br.com.luvia.linear.Mesh;
import br.com.luvia.loader.TextureLoader;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.jogamp.opengl.util.texture.Texture;

public class GridCubePerspective extends ApplicationGL {

	private Mesh stone;
	private Mesh tree;
	private Texture floor;

	protected int mx = 0;
	protected int my = 0;

	protected boolean click = false;

	protected double turnSpeed = 1;
	protected double walkSpeed = 0.5;

	private AimPoint aim;

	private boolean forwardPressed = false;
	private boolean rewardPressed = false;
	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean leftPressed = false;
	private boolean rightPressed = false;

	private static final int NONE = -1;
	
	boolean drawRay = false;
	double tileSize = 1;
	int colide = NONE;
	int selected = NONE;

	private List<BoundingBox> cubes;

	public GridCubePerspective(int w, int h) {
		super(w, h);
	}

	@Override
	public void init(Graphics3D drawable) {
		aim = new AimPoint(30, 1.6, 0);
		aim.setAngleY(180);

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

		cubes = new ArrayList<BoundingBox>();
		cubes.add(new BoundingBox(new Vector3(20, 0.5f, 7), new Vector3(21, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(22, 0.5f, 7), new Vector3(23, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(24, 0.5f, 7), new Vector3(25, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(26, 0.5f, 7), new Vector3(27, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(28, 0.5f, 7), new Vector3(29, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(30, 0.5f, 7), new Vector3(31, 1.5f, 8)));
		cubes.add(new BoundingBox(new Vector3(32, 0.5f, 7), new Vector3(33, 1.5f, 8)));

		stone = new Mesh(MeshLoader.getInstance().loadModel("stone/stone.obj"));
		stone.setColor(Color.WHITE);
		stone.offsetX(35);
		stone.offsetY(0.5);
		stone.offsetZ(7);
		stone.setScale(0.4f);

		tree = new Mesh(MeshLoader.getInstance().loadModel("bamboo/bamboo.obj"));
		tree.offsetX(30);
		tree.offsetY(0.5);
		tree.offsetZ(7);
		tree.setColor(Color.WHITE);
		tree.setScale(1.5f);

		floor = TextureLoader.getInstance().loadTexture("mark.png");
	}

	@Override
	public void load() {
		loading = 100;
	}

	protected void drawFloor(GL2 gl) {

		gl.glColor3d(1,1,1);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		drawGrid(gl,200,120);
	}

	protected void drawWall(GL2 gl) {

		gl.glColor3d(1,1,1);

		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		drawGrid(gl,200,120);
	}

	private void drawGrid(GL2 gl, double x, double y) {

		floor.enable(gl);
		floor.bind(gl);

		for(int j=0;j<y;j++) {
			for(int i=0;i<x;i++) {
				drawTile(gl, i, j, tileSize);
			}
		}

		floor.disable(gl);
	}

	private void drawTile(GL2 gl, double x, double y, double tileSize) {

		gl.glBegin(GL2.GL_QUADS);

		//(0,0)
		gl.glTexCoord2d(0, 0);
		gl.glVertex3d(x*tileSize, 0, y*tileSize);

		//(1,0)
		gl.glTexCoord2d(1, 0);
		gl.glVertex3d(x*tileSize+tileSize, 0, y*tileSize);

		//(1,1)
		gl.glTexCoord2d(1, 1);
		gl.glVertex3d(x*tileSize+tileSize, 0, y*tileSize+tileSize);

		//(0,1)
		gl.glTexCoord2d(0, 1);
		gl.glVertex3d(x*tileSize, 0, y*tileSize+tileSize);

		gl.glEnd();
	}

	private void drawAxis(GL2 gl) {

		double axisSize = 100;

		//Draw Axis
		gl.glLineWidth(2.5f);

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
		if (colide >= 0) {
			gl.glColor3d(1.0, 0.0, 0.0);
		} else {
			gl.glColor3d(0.0, 0.0, 1.0);
		}

		gl.glBegin(GL.GL_LINES);
		gl.glVertex3d(aim.getX(), 1, aim.getZ());
		gl.glVertex3d(aim.getX()+v.x, aim.getY()+v.y, aim.getZ()+v.z);
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

		if(event.isKeyDown(KeyEvent.VK_W)) {
			forwardPressed = true;
		} else if(event.isKeyUp(KeyEvent.VK_W)) {
			forwardPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_S)) {
			rewardPressed = true;
		} else if(event.isKeyUp(KeyEvent.VK_S)) {
			rewardPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_UP_ARROW)) {
			upPressed = true;
		} else if(event.isKeyUp(KeyEvent.VK_UP_ARROW)) {
			upPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_DOWN_ARROW)) {
			downPressed = true;
		} else if(event.isKeyUp(KeyEvent.VK_DOWN_ARROW)) {
			downPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_RIGHT_ARROW)) {
			rightPressed = true;
		} else if(event.isKeyUp(KeyEvent.VK_RIGHT_ARROW)) {
			rightPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_LEFT_ARROW)) {
			leftPressed = true;			
		} else if(event.isKeyUp(KeyEvent.VK_LEFT_ARROW)) {
			leftPressed = false;
		}

		if(event.isKeyDown(KeyEvent.VK_SPACE)) {
			drawRay = true;
		} else if(event.isKeyUp(KeyEvent.VK_SPACE)) {
			drawRay = false;
		}
	}

	public void updateMouse(PointerEvent event) {

		mx = event.getX();
		my = event.getY();

		if(event.isButtonDown(MouseButton.MOUSE_BUTTON_LEFT)) {
			click = true;
		} else if(event.isButtonUp(MouseButton.MOUSE_BUTTON_LEFT)) {
			click = false;
		}
		
		if(event.isButtonDown(MouseButton.MOUSE_BUTTON_RIGHT)) {
			drawRay = true;
		} else if(event.isButtonUp(MouseButton.MOUSE_BUTTON_RIGHT)) {
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
		drawable.aimCamera(aim);

		//Draw Scene
		drawAxis(gl);
		drawFloor(gl);
		
		
		Ray ray = drawable.getCameraRay(mx+32, my);
		if(drawRay) {
			drawRay(gl, ray);
		}
		
		colide = -1;
		
		for (int i = 0; i<cubes.size(); i++) {

			BoundingBox cube = cubes.get(i);
			
			Vector3 center = cube.getCenter(new Vector3());
			Vector3 dimensions = new Vector3(1,1,1);
			if(Intersector.intersectRayBoundsFast(ray, center, dimensions)) {
				colide = i;
				if(!click) {
					drawable.setColor(Color.YELLOW);
				} else {
					selected = i;
					drawable.setColor(Color.BLUE);
				}
				
			} else {
				drawable.setColor(Color.GREEN);		
			}
			
			if(selected == i) {
				drawable.setColor(Color.BLUE);
			}
			
			drawable.drawBoundingBox(cube);
		}
		
		//Draw Models
		//Start batch
		gl.glEnable(GL.GL_DEPTH_TEST);

		gl.glPushMatrix();
		stone.texturedRender(gl);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glDisable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		tree.texturedRender(gl);
		gl.glPopMatrix();

		//End batch
		gl.glDisable(GL.GL_DEPTH_TEST);

		gl.glFlush();

	}


	@Override
	public void draw(Graphic g) {

		//Draw Gui
		g.setColor(Color.WHITE);
		g.drawShadow(20,20, "Scene",Color.BLACK);
		g.drawShadow(20,40, Double.toString(aim.getAngleY()),Color.BLACK);

	}

	public void updateControls(long now) {

		if(forwardPressed) {
			aim.moveXZ(-walkSpeed);
		}

		if(rewardPressed) {
			aim.moveXZ(walkSpeed);	
		}

		if(upPressed) {
			aim.offsetAngleX(turnSpeed);
		}

		if(downPressed) {
			aim.offsetAngleX(-turnSpeed);
		}

		if(leftPressed) {
			aim.offsetAngleY(+turnSpeed);			
		}

		if(rightPressed) {
			aim.offsetAngleY(-turnSpeed);			
		}

	}

}