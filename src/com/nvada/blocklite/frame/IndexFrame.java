package com.nvada.blocklite.frame;
import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;


public class IndexFrame extends BaseFrame {
	String welcome = "Welcome to Cross-Chain TXN~";
//	String s2 = "How many worker chains do you want to simulate?";
//	private JLabel jlabel = new JLabel("<html>" + s1 + "<br>" + s2 + "</html>");
	private JLabel welcomelabel;
	
	private JButton startButton;
	private JRadioButton yesRadio;
	private JRadioButton noRadio;
	
	private JTextField workerNumField;
	
	private int workerChainNum = 2;
	private boolean useHub = false;
	
	public IndexFrame() {
		super("Index", new ImagePanel());
		this.setExitOnClose();
		
		this.welcomelabel = new JLabel(welcome);
        this.startButton = new JButton("Start");
        this.yesRadio = new JRadioButton("yes");
        this.noRadio = new JRadioButton("no");
	}

	@Override
	protected void placeComponents(JPanel panel) {
        panel.setLayout(null);
        
        this.welcomelabel.setBounds(10, 10, 900, 25);
        this.welcomelabel.setForeground(Color.BLACK);
        this.welcomelabel.setFont(new Font("Serif", Font.BOLD, 28)); // Serif, SansSerif, Monospaced, Dialog, and DialogInput.
        panel.add(this.welcomelabel);
        
        JLabel jlabel1 = new JLabel("Numbers of Worker_Chain:");
        jlabel1.setBounds(10,50,360,25);
        jlabel1.setForeground(Color.BLACK);
        jlabel1.setFont(new Font("Serif", Font.BOLD, 28));
        panel.add(jlabel1);
        
        this.workerNumField = new JTextField(20);
        this.workerNumField.setBounds(350,50,50,25);
        this.workerNumField.setForeground(Color.red);
        this.workerNumField.setFont(new Font("Serif", Font.BOLD, 20));
        this.workerNumField.setText("" + workerChainNum);
        panel.add(this.workerNumField);
        
        JLabel jlabel2 = new JLabel("Want to connect to a Hub?");
        jlabel2.setBounds(10,100,350,25);
        jlabel2.setForeground(Color.BLACK);
        jlabel2.setFont(new Font("Serif", Font.BOLD, 28));
        panel.add(jlabel2);

        this.yesRadio.setBounds(350, 90, 100, 40);
        this.yesRadio.setForeground(Color.BLACK);
        this.yesRadio.setFont(new Font("Serif", Font.BOLD, 28));
        panel.add(this.yesRadio);
        
        this.noRadio.setForeground(Color.BLACK);
        this.noRadio.setFont(new Font("Serif", Font.BOLD, 28));
        this.noRadio.setBounds(450, 90, 100, 40);
        panel.add(this.noRadio);
        
        ButtonGroup group=new ButtonGroup();
		group.add(this.yesRadio);
		group.add(this.noRadio);
		
		if(useHub) {
			yesRadio.setSelected(true);
		} else {
			noRadio.setSelected(true);
		}
        
        this.startButton.setBounds(750, 350, 100, 50);
        this.startButton.setForeground(Color.RED);
        this.startButton.setFont(new Font("Monospaced", Font.BOLD, 20));
        panel.add(this.startButton);
        
        this.startButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				workerChainNum = Integer.parseInt(workerNumField.getText());
				useHub = yesRadio.isSelected(); 
				ConfigFrame configFrame = new ConfigFrame(workerChainNum, useHub);
				configFrame.show();
				IndexFrame.this.setHideOnClose();
				IndexFrame.this.hide();
			}
        });
    }
	 
	private static class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			ImageIcon icon = new ImageIcon("./bg1.png");
			icon.setImage(icon.getImage().getScaledInstance(900,450,Image.SCALE_DEFAULT));
			g.drawImage(icon.getImage(), 0, 0, null);
		}
	}
	 
	 public static void main(String[] args) {
		 	IndexFrame index = new IndexFrame();
		 	index.show();
	 }
		
}
