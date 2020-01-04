package com.nvada.blocklite.frame;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Index_demo {
	private static JButton b1 = new JButton("Start Master");
	private static JButton b2 = new JButton("Start Worker");
	private static JButton b3 = new JButton("Start Hub");
	
    public static boolean RIGHT_TO_LEFT = false;
     
    public static void addComponentsToPane(Container pane) {
         
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }
         
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(
                    java.awt.ComponentOrientation.RIGHT_TO_LEFT);
        }
         
        JButton t = new JButton("Welcome to Cross-Chain Txn");
        pane.add(t, BorderLayout.PAGE_START);
         
        b1.setPreferredSize(new Dimension(200, 100));
        pane.add(b1, BorderLayout.LINE_START);
         
        b2.setPreferredSize(new Dimension(200, 100));
        pane.add(b2, BorderLayout.CENTER);

        b3.setPreferredSize(new Dimension(200, 100));
        pane.add(b3, BorderLayout.LINE_END);
         
//        button = new JButton("5 (LINE_END)");
//        pane.add(button, BorderLayout.LINE_END);
        
        b1.addActionListener( new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				MasterFrame masterFrame = new MasterFrame();
  		    	masterFrame.show();
  			}
          });
          
          b2.addActionListener( new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				WorkerFrame workerFrame = new WorkerFrame();
  		    	workerFrame.show();
  			}
          });
          
          b3.addActionListener( new ActionListener() {
  			public void actionPerformed(ActionEvent e) {
  				HubFrame hub = new HubFrame();
  		    	hub.show();
  			}
          });
    }
     
    private static void createAndShowGUI() {
         
        //Create and set up the window.
    	int width = 2000;
		int height = 2500;
		
		int scrx=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int scry=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int x=(scrx-width)/2;
		int y=(scry-height)/2;
		
		JFrame frame = new JFrame("Index");
		frame.setBounds(x, y, width, height);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());
        //Use the content pane's default BorderLayout. No need for
        //setLayout(new BorderLayout());
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
     
    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
         
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
