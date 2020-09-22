package com.nvada.blocklite.frame;


import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.nvada.blocklite.dataservice.EtcDataService;
import com.nvada.blocklite.dataservice.TpchDataService;
import com.nvada.blocklite.log.Logger;
import com.nvada.blocklite.net.Master;
import com.nvada.blocklite.net.NetNode;

public class MasterFrame extends NodeFrame {
	
	private JTextField nodeField;
	private JTextField nodesNum;
	private JTextField recoverField;
	private JTextField useHubField;
	private JTextField taskField;
	
	private boolean useHub = false;
	
	public MasterFrame() {
		super("Master");
		
		this.useHub = false;
	}
	
	public void connected2Hub(boolean useHub) {
		this.useHub = useHub;
		if(this.useHubField != null) {
			if(this.useHub) {
				this.useHubField.setText("true");
			} else {
				this.useHubField.setText("false");
			}
		}
	}
	
	@Override
	protected void placeNodeComponents(JPanel panel) {
        panel.setLayout(null);
        JLabel nodeLabel = new JLabel("Chain Name:");
        nodeLabel.setBounds(10,20,120,25);
        panel.add(nodeLabel);

        this.nodeField = new JTextField(20);
        this.nodeField.setBounds(120,20,165,25);
        this.nodeField.setText("Node.A");
        panel.add(this.nodeField);
        
        JLabel nodeLabel1 = new JLabel("Nodes Number:");
        nodeLabel1.setBounds(10,50,120,25);
        panel.add(nodeLabel1);
        
        this.nodesNum = new JTextField(20);
        this.nodesNum.setBounds(120,50,165,25);
        this.nodesNum.setText("2");
        panel.add(this.nodesNum);

        JLabel recoverLabel = new JLabel("Auto Recover:");
        recoverLabel.setBounds(10,80,120,25);
        panel.add(recoverLabel);
        
        this.recoverField = new JTextField(20);
        this.recoverField.setBounds(120,80,165,25);
        this.recoverField.setText("true");
        panel.add(this.recoverField);
        
        JLabel useHubLabel = new JLabel("Connect to Hub:");
        useHubLabel.setBounds(10,110,120,25);
        panel.add(useHubLabel);
        
        this.useHubField = new JTextField(20);
        this.useHubField.setBounds(120,110,165,25);
        if(this.useHub) {
			this.useHubField.setText("true");
		} else {
			this.useHubField.setText("false");
		}
        
        panel.add(useHubField);
        
        JLabel taskLabel = new JLabel("Task Count:");
        taskLabel.setBounds(10,140,120,25);
        panel.add(taskLabel);
        
        this.taskField = new JTextField(20);
        this.taskField.setBounds(120,140,165,25);
        this.taskField.setText("5");
        panel.add(this.taskField);
    }
	
	@Override
	protected boolean checkNodeInfo() {
		String nodeName = nodeField.getText();
		String autoRecover = recoverField.getText();
		
		if(nodeName == null || nodeName.trim().isEmpty()) {
			nodeField.setText("Chain.Master");
		}
		
		if( autoRecover == null || autoRecover.isEmpty() || 
			!(autoRecover.equals("true") || autoRecover.equals("false"))) {
			showDialog("'Auto Recover' feild can only be true or false");
			return false;
		}
		
		String useHub = useHubField.getText();
		if( useHub == null || useHub.isEmpty() || 
			!(useHub.equals("true") || useHub.equals("false"))) {
			showDialog("'Connect to Hub' feild can only be true or false");
			return false;
		}
		return true;
	}
	
	@Override
	protected void onStartNode() {
		super.onStartNode();
		EtcDataService.instance().reset();
		TpchDataService.instance().reset();
	}
	
	@Override
	protected NetNode generateNode() {
		this.numPeers = Integer.parseInt(nodesNum.getText());
		int taskCount = Integer.parseInt(taskField.getText());
		
		String nodeName = nodeField.getText();
		boolean autoRecover = Boolean.parseBoolean(recoverField.getText());
		boolean useHub = Boolean.parseBoolean(useHubField.getText());
		
		Master master = new Master(nodeName, autoRecover);
		master.connect2Hub(useHub);
		master.setTaskCount(taskCount);
		return master;
	}

	 public static void main(String[] args) {
    	Logger.getInstance();
    	
    	MasterFrame masterFrame = new MasterFrame();
    	masterFrame.setExitOnClose();
    	masterFrame.show();
    }
}
