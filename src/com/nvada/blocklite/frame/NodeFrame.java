package com.nvada.blocklite.frame;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.nvada.blocklite.net.NetNode;
import com.nvada.blocklite.net.NetNode.OutPutMessageListener;

public abstract class NodeFrame extends BaseFrame implements OutPutMessageListener{
	protected int numPeers = 2;
	
	protected JLabel outputLabel;
	protected JTextArea contentArea;
	protected NetNode node;
	
	private JButton startButton;
	private JButton stopButton;
	
	private JPanel nodeContentPanel;
	
	private ChartPanel chartPanel;
	private DefaultCategoryDataset dataset;
	
	public NodeFrame(String title) {
		super(title);
		this.resize(900, 600);
		this.dataset = new DefaultCategoryDataset();
		initDataset();
		this.chartPanel = buildChartPanel();
	}
	
	public void setNumPeers(int numPeers) {
		this.numPeers = numPeers;
	}
	
	@Override
	protected void placeComponents(JPanel panel) {
        panel.setLayout(null);
        
        this.nodeContentPanel = new JPanel();
        this.nodeContentPanel.setBounds(0, 0, 300,180);
        placeNodeComponents(this.nodeContentPanel);
        panel.add(this.nodeContentPanel);;

        this.startButton = new JButton("Start");
        this.startButton.setBounds(90, 200, 80, 25);
        panel.add(this.startButton);
        
        this.stopButton = new JButton("Stop");
        this.stopButton.setBounds(200, 200, 80, 25);
        this.stopButton.setEnabled(false);
        panel.add(this.stopButton);
        
        int chartH = 350; 
        chartPanel.setBounds(300, 0, this.width - 315, chartH);
        panel.add(chartPanel);
        
        this.outputLabel = new JLabel("output:");
        this.outputLabel.setBounds(10,chartH - 25, 280, 25);
        panel.add(this.outputLabel);
        
		this.contentArea=new JTextArea(20,80);
		this.contentArea.setLineWrap(true);
		this.contentArea.setEditable(false);
		
		JScrollPane contentScroll = new JScrollPane(this.contentArea);
		contentScroll.setBounds(10, chartH, this.width - 25, this.height - (chartH + 30));
		panel.add(contentScroll);
        
        this.startButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkNodeInfo()) {
					initDataset();
					outputLabel.setText("output:");
					contentArea.setText("");
					startNode();
				}
			}
        });
        
        this.stopButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopNode();
			}
        });
    }
	
	// http://www.jfree.org/jfreechart/api/javadoc/org/jfree/chart/ChartFactory.html
	private ChartPanel buildChartPanel() {
		boolean legend = true;
		JFreeChart chart = ChartFactory.createBarChart3D("statistics",null, null, this.dataset, PlotOrientation.VERTICAL, legend, false, false);
		CategoryPlot plot = (CategoryPlot) chart.getCategoryPlot();
		CategoryAxis axis = plot.getDomainAxis();
		axis.setLabelFont(new Font("宋体", Font.BOLD, 20));
		axis.setTickLabelFont(new Font("宋体", Font.BOLD, 20));

		ValueAxis rangeAxis = plot.getRangeAxis();
		rangeAxis.setLabelFont(new Font("宋体", Font.BOLD, 20));

		if(legend) {
			chart.getLegend().setItemFont(new Font("宋体", Font.BOLD, 20));
		}
		chart.getTitle().setFont(new Font("黑体", Font.ITALIC, 22));

		return new ChartPanel(chart, true);
	}
	
	 private void initDataset() {
		 dataset.clear();
		 
        //dataset.addValue(1, "send", "ack");
        //dataset.addValue(1, "send", "fail");
        
        //dataset.addValue(1, "receive", "ack");
        //dataset.addValue(1, "receive", "fail");
        
        dataset.addValue(0, "ack", "proposal");
        dataset.addValue(0, "ack", "commit");
        dataset.addValue(0, "ack", "rollback");
        
        dataset.addValue(0, "fail", "proposal");
        dataset.addValue(0, "fail", "commit");
        dataset.addValue(0, "fail", "rollback");
        
        dataset.addValue(0, "timeout", "proposal");
        dataset.addValue(0, "timeout", "commit");
        dataset.addValue(0, "timeout", "rollback");
    }
	
	abstract protected void placeNodeComponents(JPanel panel);
	
	abstract protected NetNode generateNode();
	
	abstract protected boolean checkNodeInfo();
	
	protected void onFinished(long runMills) {
		outputLabel.setText("runing: " + formatMills(runMills));
	}
	
	protected void onStartNode() {
		this.startButton.setEnabled(false);
		this.stopButton.setEnabled(true);
	}
	
	protected void onStopNode() {
		this.startButton.setEnabled(true);
		this.stopButton.setEnabled(false);
	}
	
	protected void startNode() {
		this.node = generateNode();
		this.node.setListener(NodeFrame.this);
		
		new Thread() {
			@Override
			public void run() {
				long startMills = System.currentTimeMillis();
				node.startWork(numPeers);
				long actualMills = System.currentTimeMillis() - startMills;
				onFinished(actualMills);
				onStopNode();
			}			
		}.start();
		
		onStartNode();
	}
	
	protected void stopNode() {
		if(node != null) {
			node.stopLoop();
		}
	}
	
	protected boolean isNumeric (String str) {
	    for (int i = str.length(); --i >=0;) {
	          if (!Character.isDigit(str.charAt(i))) {
	                return false;
	          }
	    }
	    return true;
	}
	
	@Override
	synchronized public void onAction(String actionName, String ack) {
		//dataset.addValue(1, ack, actionName);
		dataset.incrementValue(1, ack, actionName);
		
		if(this.chartPanel != null) {
			chartPanel.updateUI();
		}
	}
	
	@Override
	synchronized public void onOutMessage(String info) {
		
		if(this.contentArea == null || info==null || info.length()<1){
			return;
		}
		
		String str=this.contentArea.getText();
		
		//字符过多则截剪
		if(str!=null && str.length()>10240){
			str=str.substring(5120, str.length());
			
			String lineEnd="\r\n";
			
			int pos=str.indexOf(lineEnd);
			
			if(pos!=-1){
				str=str.substring(pos+lineEnd.length());
			}
		}
		
		if(str==null || str.length()<1){
			str=""+info;
		} else {
			str=str+"\r\n"+info;
		}
		
		if(str!=null && str.length()>0){
			this.contentArea.setText(str);
			this.contentArea.setCaretPosition(str.length());
		}
	}
	
	public static String formatMills(long mills) {
		long one_second = 1000;
		long one_minute = one_second * 60;
		long one_hour = one_minute * 60;
		
		String hour = null;
		String minute = null;
		String second = null;
		
		hour = (mills / one_hour) + "";
		
		mills = mills % one_hour;
		minute = (mills / one_minute) + "";
		
		mills = mills % one_minute;
		second = ( mills / one_second) + "";
		
		mills = mills % one_second;
		
		if (hour.length() < 2) {
			hour = 0 + hour;
		}
		
		if (minute.length() < 2) {
			minute = 0 + minute;
		}

		if (second.length() < 2) {
			second = 0 + second;
		}
		
		return hour + ":" +  minute + ":" + second + " " + mills + " ms";
	}
}
