/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.game;

import hfk.net.NetState;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class ServerGameController extends GameController {

	private class PacketListener extends Thread{
		ServerGameController ctrl;
		int bufferSize;
		public PacketListener(ServerGameController ctrl, int bufferSize){
			this.ctrl = ctrl;
			this.bufferSize = bufferSize;
		}
		@Override
		public void run() {
			while(true){
				byte[] buffer = new byte[bufferSize];
				DatagramPacket p = new DatagramPacket(buffer, buffer.length);
				try {
					socket.receive(p);
				} catch (IOException ex) {
					throw new RuntimeException("receive packet failed", ex);
				}
				synchronized(ctrl.packets){
					ctrl.packets.add(p);
				}
			}
		}
	}
	
	private static final int SEND_TIME_INTERVAL = 100;
	
	private int port;
	private int sendTimer = SEND_TIME_INTERVAL;
	private DatagramSocket socket;
	private final LinkedList<DatagramPacket> packets = new LinkedList<>();

	public ServerGameController(int port, GameContainer gc, GameSettings s) {
		super(gc, s);
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException ex) {
			throw new RuntimeException("could not open socket!", ex);
		}
		//new PacketListener(this, 1024).start();
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		super.update(gc, sbg, time);
		sendTimer += time;
		if(sendTimer >= SEND_TIME_INTERVAL){
			sendTimer %= SEND_TIME_INTERVAL;
			long t1 = System.currentTimeMillis();
			NetState state = createNetState();
			// TODO: think about level integration into state
			//state.addObject(level);
			long t2 = System.currentTimeMillis();
			byte[] arr = state.toBytes();
			long t3 = System.currentTimeMillis();
			System.out.println("state size: " + arr.length + ", gather time: " + (t2-t1) + ", pack time: " + (t3-t2));
		}
	}
	
	
}
