package com.nvada.blocklite.frame;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.nvada.blocklite.log.Logger;
import com.nvada.blocklite.net.NetNode;
import com.nvada.blocklite.net.Worker;

public class WorkerFrame extends NodeFrame {
	
	private JTextField nodeNameField;
	private JTextField portFieldField;
	private JTextField nodesNumField;
	
	private String nodeName;
	private int nodesNum;
	private int port;
	private boolean editable;
	
	public WorkerFrame() {
		super("Worker");
		
		this.nodeName = "NodeA";
		this.nodesNum = 2;
		this.port = 4444;
		
		this.editable = true;
	}
	
	public void config(String nodeName, int nodesNum, int port) {
		this.nodeName = nodeName;
		this.nodesNum = nodesNum;
		this.port = port;
		
		if(this.nodeNameField != null) {
			this.nodeNameField.setText(nodeName);
		}
		
		if(this.nodesNumField != null) {
			this.nodesNumField.setText("" + nodesNum);
		}
		
		if(this.portFieldField != null) {
			this.portFieldField.setText("" + port);
		}
	}
	
	public void setEditable(boolean edit) {
		this.editable = edit;
		if(this.nodeNameField != null) {
			this.nodeNameField.setEditable(this.editable);
		}
		
		if(this.nodesNumField != null) {
			this.nodesNumField.setEditable(this.editable);
		}
		
		if(this.portFieldField != null) {
			this.portFieldField.setEditable(this.editable);
		}
	}
	
	@Override
	protected void placeNodeComponents(JPanel panel) {
        panel.setLayout(null);
        JLabel nodeLabel = new JLabel("Chain Name:");
        nodeLabel.setBounds(10,20,120,25);
        panel.add(nodeLabel);

        this.nodeNameField = new JTextField(20);
        this.nodeNameField.setBounds(120,20,165,25);
        this.nodeNameField.setText(this.nodeName);
        this.nodeNameField.setEditable(this.editable);
        panel.add(this.nodeNameField);
        
        JLabel nodeLabel1 = new JLabel("Nodes Number:");
        nodeLabel1.setBounds(10,50,120,25);
        panel.add(nodeLabel1);
        
        this.nodesNumField = new JTextField(20);
        this.nodesNumField.setBounds(120,50,165,25);
        this.nodesNumField.setText(""+ this.nodesNum);
        this.nodesNumField.setEditable(this.editable);
        panel.add(this.nodesNumField);

        JLabel portLabel = new JLabel("Port Number:");
        portLabel.setBounds(10,80,120,25);
        panel.add(portLabel);
        
        this.portFieldField = new JTextField(20);
        this.portFieldField.setBounds(120,80,165,25);
        this.portFieldField.setText("" + this.port);
        this.portFieldField.setEditable(this.editable);
        panel.add(this.portFieldField);
    }
	
	@Override
	protected boolean checkNodeInfo() {
		String nodeName = nodeNameField.getText();
		String port = portFieldField.getText();
		
		if(nodeName == null || nodeName.trim().isEmpty()) {
			nodeNameField.setText("Chain.Worker");
		}
		
		if( port == null || port.isEmpty() || isNumeric(port) == false || port.length() != 4) {
			showDialog("change a new port");
			return false;
		}
		return true;
	}
	
	@Override
	protected NetNode generateNode() {
		this.numPeers = Integer.parseInt(nodesNumField.getText());
		
		String nodeName = nodeNameField.getText();
		int port = Integer.parseInt(portFieldField.getText());
		return new Worker(nodeName, port);
	}

	public static void main(String[] args) {
		Logger.getInstance();

		WorkerFrame workerFrame = new WorkerFrame();
		workerFrame.setExitOnClose();
		workerFrame.show();
	}
}
