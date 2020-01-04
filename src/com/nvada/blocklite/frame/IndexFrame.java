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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;


public class IndexFrame extends BaseFrame {
	String welcome = "Welcome to CBT~";
	String myLab = "Nevada HPDIC Lab";
//	String s2 = "How many worker chains do you want to simulate?";
//	private JLabel jlabel = new JLabel("<html>" + s1 + "<br>" + s2 + "</html>");
	private JLabel welcomelabel;
	private JLabel myLabLabel;
	
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
		this.myLabLabel = new JLabel(myLab);
        this.startButton = new JButton("Start");
        this.yesRadio = new JRadioButton("yes");
        this.noRadio = new JRadioButton("no");
	}

	@Override
	protected void placeComponents(JPanel panel) {
        panel.setLayout(null);
        
        this.welcomelabel.setBounds(10, 10, 600, 30);
        this.welcomelabel.setForeground(Color.BLACK);
        this.welcomelabel.setFont(new Font("Courier New", Font.BOLD , 28)); // Serif, SansSerif, Monospaced, Dialog, and DialogInput.
        panel.add(this.welcomelabel);
        
        
//        this.myLabLabel.setHorizontalAlignment(JLabel.RIGHT);
        this.myLabLabel.setForeground(new Color(225, 20, 147));
        this.myLabLabel.setBounds(600, 10, 300, 30);
        this.myLabLabel.setFont(new Font("Courier New", Font.BOLD + Font.ITALIC, 28));
        panel.add(this.myLabLabel);
//        panel.add(this.myLabLabel,BorderLayout.NORTH);

        
        JLabel jlabel1 = new JLabel("Numbers of Participant_Chain:");
        jlabel1.setBounds(10,50,500,30);
        jlabel1.setForeground(Color.BLACK); 
        jlabel1.setFont(new Font("Courier New", Font.BOLD, 28));
        panel.add(jlabel1);
        
        this.workerNumField = new JTextField(20);
        this.workerNumField.setBounds(500,50,50,30);
        this.workerNumField.setForeground(Color.BLACK);
        this.workerNumField.setFont(new Font("Courier New", Font.BOLD, 20));  // Serif
        this.workerNumField.setText("" + workerChainNum);
        panel.add(this.workerNumField);
        
        JLabel jlabel2 = new JLabel("Want to connect to a Hub?");
        jlabel2.setBounds(10,100,430,30);
        jlabel2.setForeground(Color.BLACK);
        jlabel2.setFont(new Font("Courier New", Font.BOLD, 28));
        panel.add(jlabel2);

        this.yesRadio.setBounds(435, 95, 100, 40);
        this.yesRadio.setForeground(Color.BLACK);
        this.yesRadio.setFont(new Font("Courier New", Font.BOLD, 28));
        panel.add(this.yesRadio);
        
        this.noRadio.setForeground(Color.BLACK);
        this.noRadio.setFont(new Font("Courier New", Font.BOLD, 28));
        this.noRadio.setBounds(530, 95, 100, 40);
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
        this.startButton.setForeground(new Color(225, 20, 147));
        this.startButton.setFont(new Font("Courier New", Font.BOLD, 26));
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
