package com.datumize.multimodule;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Server{
	//The value the server keeps
	private long sum = 0L;
	
	private InetSocketAddress isa;
	
	private AsynchronousServerSocketChannel server;
	
	private Future<AsynchronousSocketChannel> future; 

	public Server(InetAddress hostAddress, int port) throws IOException {
		// open a server channel and bind to a free address, then accept a connection
        System.out.println("Open server channel");
        this.isa = new InetSocketAddress(hostAddress, port);
        this.server = AsynchronousServerSocketChannel.open().bind(this.isa);
        System.out.println("Initiate accept");
        this.future = server.accept();
	}

	public void sendLong(long value, AsynchronousSocketChannel readyAsynchronousSocketChannel) throws InterruptedException, ExecutionException {
        //send a message to the client
        ByteBuffer message = ByteBuffer.allocate(Long.BYTES);
        message.putLong(value);
        message.flip();
        // wait for the response
        System.out.println("Sending value to the client...");
        readyAsynchronousSocketChannel.write(message).get();
    }
	
	public synchronized long add(long value){
		System.out.print("Server adds: "+this.sum+" + "+value);
		this.sum=Math.addExact(this.sum, value);//checks overflow
		System.out.println(" = "+this.sum);
		return this.sum;
	}

	public long get(){
		return this.sum;
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
		
		if (args.length != 1) {
			System.err.println("Usage: java Server <port number>");
			System.exit(1);
		}

		int portNumber = Integer.parseInt(args[0]);
		Server server = new Server(null, portNumber);
		while(true){
			AsynchronousSocketChannel readyAsynchronousSocketChannel= server.future.get();
			System.out.println("Accept completed");
			server.receive(readyAsynchronousSocketChannel);
			readyAsynchronousSocketChannel.close();
		}
	}

	private void receive(AsynchronousSocketChannel readyAsynchronousSocketChannel) throws InterruptedException, ExecutionException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(1);
        //wait for a client message
		System.out.println("Waiting for a client message");
        readyAsynchronousSocketChannel.read(byteBuffer).get();
        byteBuffer.flip();
        if(byteBuffer.get()==0){//add
        	ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
        	readyAsynchronousSocketChannel.read(longBuffer).get();
        	longBuffer.flip();
        	long value=longBuffer.getLong();
        	System.out.println("Server received : "+value);
        	this.add(value);
        }
        this.sendLong(this.get(), readyAsynchronousSocketChannel);
	}
}