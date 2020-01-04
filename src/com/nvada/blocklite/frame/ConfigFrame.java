package com.nvada.blocklite.frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
 
public class ConfigFrame extends BaseFrame {
	
	private boolean useHub = false;
	private int workerNum = 2;

	private List<JTextField> nodeNameFields;
	private List<JTextField> portFieldFields;
	private List<JTextField> nodesNumFields;
		  
	public ConfigFrame(int workerChainNum, boolean connect2Hub) {
		super("Participant_Chain Settings");
		this.setExitOnClose();
		
		this.useHub = connect2Hub;
		this.workerNum = workerChainNum;
		
		this.nodeNameFields = new ArrayList<JTextField>();
		this.portFieldFields = new ArrayList<JTextField>();
		this.nodesNumFields = new ArrayList<JTextField>();

	}
	
	@Override
	protected void initFrame(final JFrame frame) {
		JButton submitButton = new JButton("Submit");
		JPanel submitPanel = new JPanel();
		submitPanel.add(submitButton);

		GridLayout gridLayout = new GridLayout(0, 1);
		
		JPanel cardPanel = new JPanel();
		cardPanel.setLayout(gridLayout);
		
		JScrollPane scrollPanel = new JScrollPane(cardPanel);
		//scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		for (int i = 0; i < this.workerNum ; i++) {
			String title = "Participant " + (i + 1);
			String nodeName = "Node" + (char)('B' + i);
			JPanel workerPanel = new JPanel();
			JTextArea area = new JTextArea(10, 30);
			area.setEditable(false);
			placeConfigComponents(area, title, nodeName, 4444+i);
			
			workerPanel.add(area);
			cardPanel.add(workerPanel); 
		}

		frame.add(scrollPanel, BorderLayout.CENTER);
		frame.add(submitPanel, BorderLayout.AFTER_LAST_LINE);
		
		if(useHub) {
			JPanel hubPanel = new JPanel();
			JTextArea area = new JTextArea(10, 30);
			area.setEditable(false);
			placeConfigComponents(area, "Hub", "Hub", 5555);
			hubPanel.add(area);
			frame.add(hubPanel, BorderLayout.EAST);
		}
		
		//frame.setLocation(450, 400);

		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sumbitConfig();
				ConfigFrame.this.setHideOnClose();
				ConfigFrame.this.hide();
			}
		});
	}
	
	@Override
	protected void placeComponents(JPanel panel) {
		// add to do
	}
	
	private void sumbitConfig() {
		int i=0;
    	
    	int ports[] = new int[this.workerNum];
		for (i = 0; i < this.workerNum ; i++) {
			String nodeName= this.nodeNameFields.get(i).getText();
			int nodesNum = Integer.parseInt(this.nodesNumFields.get(i).getText());
			ports[i] = Integer.parseInt(this.portFieldFields.get(i).getText());
			WorkerFrame workerFrame = new WorkerFrame();
			
			workerFrame.setTitle("Participant" + (i+1));
			workerFrame.config(nodeName, nodesNum, ports[i]);
			workerFrame.setEditable(false);
			workerFrame.setHideOnClose();
			workerFrame.show();
			workerFrame.startNode();
		}
		
		savePortConfig("resources/work", ports);
		
		if (useHub) {
			String nodeName= this.nodeNameFields.get(i).getText();
			int nodesNum = Integer.parseInt(this.nodesNumFields.get(i).getText());
			int port = Integer.parseInt(this.portFieldFields.get(i).getText());
			
			savePortConfig("resources/hub", new int[] {port});
			
			HubFrame hubFrame = new HubFrame();
			hubFrame.config(nodeName, nodesNum, port);
			hubFrame.setEditable(false);
			hubFrame.setHideOnClose();
			hubFrame.show();
			hubFrame.startNode();
		}

		MasterFrame masterFrame = new MasterFrame();
		masterFrame.setExitOnClose();
		masterFrame.connected2Hub(this.useHub);
		masterFrame.show();
	}
	
	private void placeConfigComponents(JTextArea area, String workerTitle, String nodeName, int port) {
		JLabel titleLabel = new JLabel(workerTitle);
		titleLabel.setBounds(120, 10, 120, 25);
		area.add(titleLabel);
		
		JLabel nodeLabel = new JLabel("Chain Name:");
		nodeLabel.setBounds(10, 40, 120, 25);
		area.add(nodeLabel);

		JTextField nodeNameField = new JTextField(20);
		nodeNameField.setBounds(120, 40, 165, 25);
		nodeNameField.setText(nodeName);
		area.add(nodeNameField);
		this.nodeNameFields.add(nodeNameField);

		JLabel nodeLabel1 = new JLabel("Nodes Number:");
		nodeLabel1.setBounds(10, 70, 120, 25);
		area.add(nodeLabel1);

		JTextField nodesNumField = new JTextField(20);
		nodesNumField.setBounds(120, 70, 165, 25);
		nodesNumField.setText("2");
		area.add(nodesNumField);
		this.nodesNumFields.add(nodesNumField);

		JLabel portLabel = new JLabel("Port Number:");
		portLabel.setBounds(10, 100, 120, 25);
		area.add(portLabel);

		JTextField portField = new JTextField(20);
		portField.setBounds(120, 100, 165, 25);
		portField.setText("" + port);
		area.add(portField);
		this.portFieldFields.add(portField);
	}
	
	private void savePortConfig(String configPath, int ports[]) {
		if(ports == null ) {
			return;
		}
		
		File logfile = new File(configPath);
		
    	if(logfile.exists()) {
    		logfile.delete();
    	}
		   
    	PrintWriter out = null;
    	try {
			logfile.createNewFile();
			out = new PrintWriter(logfile);
			for(int i = 0; i<ports.length;i++) {
				out.println(ports[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			out = null;
		} finally {
			if(out != null ) {
				out.flush();
				out.close();
			}
		}
	}
	 
	 public static void main(String[] args) {
		 boolean useHub = false;
		 int workerChainNum = 2;
		 ConfigFrame configFrame = new ConfigFrame(workerChainNum, useHub);
		 configFrame.show();
	 }	 
}