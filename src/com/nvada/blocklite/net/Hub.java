// https://wiki.jikexueyuan.com/project/java-socket/tcp.html
package com.nvada.blocklite.net;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Semaphore;

import com.nvada.blocklite.config.Constants;
import com.nvada.blocklite.data.DataCell;
import com.nvada.blocklite.data.MessageDataCell;
import com.nvada.blocklite.log.Logger;


public class Hub extends NetNode {
	
	private int port;
	private MessageDataCell taskCell = null;
	private Semaphore cellSemaphore = null;
	private ServerSocket serverSocket = null;
	
	public static void main(String[] args) {
		Logger.getInstance();
		Hub worker = new Hub(readStringArg(args, 0, "Hub"), readIntArg(args, 1, Constants.DEFAULT_HUB));
		worker.startWork(2);
	}
	
	public Hub(String name, int port) {
		this.port = port;
		this.isWork = false;
		this.nodeName = name;
		this.logFileName = this.nodeName + "_log.txt";
		cellSemaphore = new Semaphore(0, true);
	}
	
	
	private String redirectAction(Action action, String cmd) {
		String result = Action.FAIL_STATUS;
		
		int workerPort = action.getWorkerPort();
		
		handleMessage("redirect: " + cmd, true);
		
		try {
			Socket socket = new Socket(Constants.HOST_NAME, workerPort);
			
			socket.setSoTimeout(Constants.TIMEOUT_MS);
            PrintStream out = new PrintStream(socket.getOutputStream());
            out.println(cmd);
            out.flush();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            result = in.readLine();
            
            in.close();
            out.close();
            socket.close();
		} catch (ConnectException e) {
        	System.out.println("redirect to Worker " + workerPort + " time out.");
        } catch (SocketTimeoutException e) {
            System.out.println("redirect to Worker " + workerPort + " time out.");
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		if (result != null) {
        	if(result.equals(Action.ACK_STATUS)) {
        		action.setStatus(Action.ACK_STATUS);
        	} else {
        		action.setStatus(Action.FAIL_STATUS);
        	}
        } else {
        	action.setStatus(Action.TIMEOUT_STATUS);
        }
		
		recordAction(action.getName(), action.getStatus());
		
		handleMessage("receive: " + action.toString(), false);
		return result;
	}
	
	@Override
	protected void workLoop() {
		String command = null;
		
		try {
			serverSocket = new ServerSocket(this.port);
			serverSocket.setSoTimeout(Constants.TIMEOUT_MS);
			
			while (this.isWork) {
				// wait proposal / commit / rollback
				Socket client = serverSocket.accept();
				
				BufferedReader bi = new BufferedReader(new InputStreamReader(client.getInputStream()));
				command = bi.readLine();
				
				taskCell = handleMessage("received:" + command, false);
		    	
				cellSemaphore.acquire();
				
				Action action = Action.parse(command);
				
				if(action != null) {
					redirectAction(action, command);
					
					PrintStream resp = new PrintStream(client.getOutputStream());
					resp.println(action.getStatus());
					resp.flush();
					
					handleMessage("redirect: " + action.toString(), true);
				}
				
				bi.close();
				client.close();
			}
		} catch (SocketException e) {
			System.out.println("SocketException: " + e.getMessage());
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			closeSocket();
		}
	}
	
	public void closeSocket() {
		if(serverSocket != null) {
			try {
				serverSocket.close();
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
