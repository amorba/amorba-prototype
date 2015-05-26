import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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
import java.util.ArrayList;
import java.awt.Window;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Game extends AbstractDraw implements KeyListener {
	private String name;
	private GameWindow window;
	private boolean[] keys;
	private Point position; // mouse position
	private Thread gameThread;

	private Player player;
	private FakeServer s;

	private int camX;
	private int camY;
	private double camSize;

	public static final int BOARD_SIZE = 5000;

	public Game(String theName, GameWindow theWindow) {
		name = theName;
		// this will eventually come from server
		position = new Point(0, 0);
		s = new FakeServer(position, this);
		player = new Player(15, new Point((int) (Math.random() * 5000),
				(int) (Math.random() * 5000)));
		player.setNewLocation(new Point(100, 100));
		// get window this frame is being hosted in
		window = theWindow;

		// key tracker
		keys = new boolean[525];

		// start key listener
		addKeyListener(this);

		// make Game panel focusable
		setFocusable(true);

		// start mouse listener
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				position = e.getPoint();
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
		boolean running = true;
		while (running) {

			// close window
			if (keys[KeyEvent.VK_ESCAPE]) {
				window.closeWindow();
				running = false;
			}
			// send mouse info to server
			Point loc = player.getLocation();
			Point boardPosition = new Point((int) (loc.x + (position.x
					+ player.getRadius() - window.getWidth() / 2) / 40),
					(int) (loc.y + (position.y + player.getRadius() - window
							.getHeight() / 2) / 40));
			// System.out.println("                                                   "
			// + boardPosition);
			s.setPosition(boardPosition);

			// repaint
			repaint();

			// drawing tickrate
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void draw(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.fillRect((int) (this.getBounds().getWidth() * 0.01), (int) (this
				.getBounds().getWidth() * 0.01), (int) (this.getBounds()
				.getWidth() * 0.25),
				(int) ((this.getBounds().getHeight() * 0.06)));
		Rectangle scoreRect = new Rectangle(
				(int) (this.getBounds().getWidth() * 0.01), (int) (this
						.getBounds().getWidth() * 0.01), (int) (this
						.getBounds().getWidth() * 0.25), (int) ((this
						.getBounds().getHeight() * 0.06)));
		g2d.setColor(Color.WHITE);
		g2d.drawString("Score: ", 30, (int) scoreRect.getCenterY());
		g2d.drawString(Integer.toString((int) player.getRadius()), 70,
				(int) scoreRect.getCenterY());

		// center viewport on player
		if (player.getRadius() <= 100) {
			camSize = 3.3 - Math.pow(1.008, player.getRadius());
		} else if (player.getRadius() > 350) {
			camSize = 1.46 - Math.pow(1.0002, (player.getRadius()));
		} else if (player.getRadius() > 140 && player.getRadius() <= 350) {
			camSize = 1.8 - Math.pow(1.001, (player.getRadius()));
		} else if (player.getRadius() > 100 && player.getRadius() <= 140) {
			camSize = 2.7 - Math.pow(1.005, (player.getRadius()));
		}
		if (camSize < .03) {
			camSize = .03;
		}
		camX = (int) (this.getWidth() / 2 / camSize - player.getLocation()
				.getX());
		camY = (int) (this.getHeight() / 2 / camSize - player.getLocation()
				.getY());
		g2d.scale(camSize, camSize);
		g2d.translate(camX, camY);

		drawGrid(g2d);
		drawFood(g2d);
		player.moveAndDraw(g2d);
	}

	public void drawGrid(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(0.3f));

		g2d.drawLine(0, 0, BOARD_SIZE, 0);
		g2d.drawLine(0, 0, 0, BOARD_SIZE);
		g2d.drawLine(0, BOARD_SIZE, BOARD_SIZE, BOARD_SIZE);
		g2d.drawLine(BOARD_SIZE, BOARD_SIZE, BOARD_SIZE, 0);

		g2d.setColor(Color.DARK_GRAY);
		for (int i = 0; i < BOARD_SIZE / 20; i++) {
			g2d.drawLine(i * 20, 0, i * 20, BOARD_SIZE);
		}
		for (int i = 0; i < BOARD_SIZE / 20; i++) {
			g2d.drawLine(0, i * 20, BOARD_SIZE, i * 20);
		}

	}

	public void drawFood(Graphics2D g2d) {
		try {
			for (Cell f : s.getFood()) {
				g2d.setColor(f.getColor());
				g2d.fillOval((int) (f.getLocation().getX()),
						(int) (f.getLocation().getY()), 8, 8);
			}
		} catch (java.util.ConcurrentModificationException e) {
			// you happy now ojas
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

	public void setPlayer(Player p) {
		player = p;
	}

	public static int getBoardSize() {
		return BOARD_SIZE;
	}

	@Override
	public String getName() {
		return name;
	}

}
