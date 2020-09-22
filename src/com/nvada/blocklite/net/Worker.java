// https://wiki.jikexueyuan.com/project/java-socket/tcp.html
package com.nvada.blocklite.net;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.concurrent.Semaphore;

import com.nvada.blocklite.config.Constants;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.data.MessageDataCell;
import com.nvada.blocklite.log.Logger;

// 服务端处理 TCP 连接请求的代码如下：
public class Worker extends NetNode {
	
	private int port;
	public static int numPeers;
	private boolean ack = false;
	private boolean blocking = false;
	
	private MessageDataCell taskCell = null;
	private Semaphore cellSemaphore = null;
	
	private ServerSocket workerSocket = null;
	
	public static void main(String[] args) {
		Logger.getInstance();
		Worker worker = new Worker(readStringArg(args, 0, "Node.B"), readIntArg(args, 1, Constants.DEFAULT_WORK));
		worker.startWork(2);
	}
	
	public Worker(String name, int port) {
		this.port = port;
		this.isWork = false;
		this.blocking = false;
		this.nodeName = name;
		this.logFileName = this.nodeName + "_log.txt";
		cellSemaphore = new Semaphore(0, true);
	}
	
	private String receiveComand(ServerSocket serverSocket) {
		String actionName = null;
		String command = null;
		Random random = new Random(System.nanoTime());
		try {
			Socket client = serverSocket.accept(); 
	
			// read cmd from master
			BufferedReader bi = new BufferedReader(new InputStreamReader(client.getInputStream()));
			command = bi.readLine();
			
			taskCell = handleMessage("received:" + command, false);
			
			cellSemaphore.acquire();
			
			Action action = Action.parse(command);
			
			if(action != null) {
				actionName = action.getName();
				
				ack = false;
				if(blocking) {
					if( actionName != null && ( actionName.equals(Action.COMMIT_ACTION) || actionName.equals(Action.ROLLBACK_ACTION)) ) {
						blocking = false;
						ack = true;
						outMessage(nodeName + " released");
					} 
				} else {
					if(random.nextInt(100) < 90) {
						ack = true;
					} 
				}
				
				PrintStream resp = new PrintStream(client.getOutputStream());
				
				if(ack) {
					resp.println(Action.ACK_STATUS);
					action.setStatus(Action.ACK_STATUS);
				} else {
					resp.println(Action.FAIL_STATUS);
					action.setStatus(Action.FAIL_STATUS);
				}
				
				recordAction(action.getName(), action.getStatus());
				
				resp.flush();
				handleMessage("send: " + action.toString(), true);
			}
			
			bi.close();
			client.close();
		} catch (SocketException e) {
			System.out.println("SocketException: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return actionName;
	}
	
	@Override
	protected void workLoop() {
		try {
			workerSocket = new ServerSocket(this.port);
			String actionName = null;
			this.blocking = false;
			while (this.isWork) {
				// wait proposal / commit / rollback
				actionName = receiveComand(workerSocket);
				
				if(ack && actionName != null && actionName.equals(Action.PROPOSAL_ACTION)) {
					outMessage(nodeName + " blocking...");
					this.blocking = true;
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeSocket();
		}
	}
	
	public void closeSocket() {
		if(workerSocket != null) {
			try {
				workerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void stopLoop() {
		closeSocket();
		stopChain();
	}
	
	@Override
	protected void onAddCell(DataCell cell) {
		if(cell != null && cell == taskCell) {
			taskCell = null;
			cellSemaphore.release();
			//System.out.println("semaphore release ~ ~");
		}
	}
}
