import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Window;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Game extends AbstractDraw implements KeyListener {
	private String name;
	private GameWindow window;
	private boolean[] keys;
	private Point position; //mouse position
	private Thread gameThread;
	
	private Player player;
	private Point newLocation;
	private FakeServer s;
	
	private int camX;
	private int camY;
	private double camSize;
	
	public static final int BOARD_SIZE = 1500;

	public Game(String theName, GameWindow theWindow) {
		
		//this will eventually come from server
		position = new Point(0, 0);
		s = new FakeServer(position, this);
		
		// get window this frame is being hosted in
		window = theWindow;

		//key tracker
		keys = new boolean[525];
		
		// start key listener
		addKeyListener(this);
		
		//make Game panel focusable
		setFocusable(true);
		
		//start mouse listener
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e)
			{
				position = MouseInfo.getPointerInfo().getLocation();
			}
		});
		
		// start draw thread
		gameThread = new Thread() {
			@Override
			public void run() {
				act();
			}
		};
		gameThread.start();

	}

	// main game tick
	public void act() {
		boolean running  = true;
		while(running)
		{
			//close window
			if (keys[KeyEvent.VK_ESCAPE])
			{
				window.closeWindow();
				running  = false;
			}
			
			//send mouse info to server
			// SwingUtilities.convertPointFromScreen(position,this);
			double deltaX = position.getX() - this.getWidth() / 2 / camSize;
			double deltaY = position.getY() - this.getWidth() / 2 / camSize;
			s.setDeltas(deltaX, deltaY);
				
			//repaint
			repaint();
			
			//drawing tickrate
			try {
				Thread.sleep(4);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void draw(Graphics2D g2d) {

		//center viewport on player
		if(player.getRadius() <= 100){
			camSize = 3.3 - Math.pow(1.008, player.getRadius());
		}
		else if(player.getRadius() > 100){
			camSize = 2.9 - Math.pow(1.006, (player.getRadius()));
		}
		camX = (int) (this.getWidth() / 2 / camSize - player.getLocation().getX());
		camY = (int) (this.getHeight() / 2 / camSize - player.getLocation().getY());
		g2d.scale(camSize, camSize);
		g2d.translate(camX, camY);
		
		
		drawGrid(g2d);
		player.moveAndDraw(g2d);
	}
	
	public void drawGrid(Graphics2D g2d)
	{
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(0.3f));
		
		g2d.drawLine(0, 0, BOARD_SIZE, 0);
		g2d.drawLine(0, 0, 0, BOARD_SIZE);
		g2d.drawLine(0, BOARD_SIZE, BOARD_SIZE, BOARD_SIZE);
		g2d.drawLine(BOARD_SIZE, BOARD_SIZE, BOARD_SIZE, 0);
		
		g2d.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < BOARD_SIZE / 10; i++)
		{
			g2d.drawLine(i * 10, 0, i * 10, BOARD_SIZE);
		}
		for (int i = 0; i < BOARD_SIZE / 10; i++)
		{
			g2d.drawLine(0, i * 10, BOARD_SIZE, i * 10);
		}
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		keys[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		keys[e.getKeyCode()] = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}
	
	public void setNewLocation(Point l)
	{
		newLocation = l;
	}
	
	public void setPlayer(Player p)
	{
		player = p;
	}

	

}
