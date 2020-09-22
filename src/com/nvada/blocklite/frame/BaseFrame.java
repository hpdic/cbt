package com.nvada.blocklite.frame;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Random;

public abstract class BaseFrame {
	
	protected int width;
	protected int height;
	
	protected String title;
	
	private boolean initedUI;
	
	private JFrame frame;
	private JPanel contentPanel;
	
	
	public BaseFrame(String title) {
        this(title, new JPanel());
	}
	
	public BaseFrame(String title, JPanel contentPanel) {
		this.initedUI = false;
		this.width = 900;
		this.height = 500;
		
		this.title = title;
		Random random = new Random(System.nanoTime());
		
		int scrx=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int scry=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		
		int x=(scrx-width)/2 ;
		int y=(scry-height)/2;
		
		x=x/2 + random.nextInt(x);
		y=y/2 + random.nextInt(y);
		
		this.frame = new JFrame(this.title);
		this.frame.setBounds(x, y, width, height);
		this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        
        this.contentPanel = contentPanel;    
        
	}
	
	public void setTitle(String title) {
		this.title = title;
		this.frame.setTitle(this.title);
	}
	
	public void setHideOnClose() {
		this.frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	public void setExitOnClose() {
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		this.frame.setSize(width, height);
	}
	
	public void setWidth(int width) {
		this.width = width;
		Rectangle bounds = this.frame.getBounds();
		this.frame.setBounds(bounds.x, bounds.y, this.width, this.height);
	}
	
	public void setHeight(int height) {
		this.height = height;
		Rectangle bounds = this.frame.getBounds();
		this.frame.setBounds(bounds.x, bounds.y, this.width, this.height);
	}
	
	public void show() {
		if(false == this.initedUI) {
			this.initedUI = true;
			initFrame(this.frame);
		}
		
        this.frame.setVisible(true);
	}
	
	public void hide() {
        this.frame.setVisible(false);
	}
	
	protected void initFrame(final JFrame frame) {
		placeComponents(this.contentPanel);
		frame.add(this.contentPanel);;
	}
	
	abstract protected void placeComponents(JPanel panel);
	
	protected boolean isNumeric (String str) {
	    for (int i = str.length(); --i >=0;) {
	          if (!Character.isDigit(str.charAt(i))) {
	                return false;
	          }
	    }
	    return true;
	}
	
	protected void showDialog(String message) {
		JFrame dialogFrame = new JFrame();
		dialogFrame.setTitle("Notice");
		dialogFrame.setSize(300,200);
		java.awt.FlowLayout fl = new java.awt.FlowLayout();
		dialogFrame.setLayout(fl);
		javax.swing.JLabel txt = new javax.swing.JLabel(message);
		dialogFrame.add(txt);
		dialogFrame.setLocationRelativeTo(null);
		dialogFrame.setVisible(true);
		dialogFrame.setDefaultCloseOperation(2);
	}
}
