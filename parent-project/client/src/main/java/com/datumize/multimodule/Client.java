package com.datumize.multimodule;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;


public class Client {
	//Client socketChannel
    private AsynchronousSocketChannel readyAsynchronousSocketChannel;
    
    //Client constructor method
	public Client(InetAddress hostAddress, int port) throws IOException, InterruptedException, ExecutionException {
		System.out.println("Open client channel");
		this.readyAsynchronousSocketChannel = AsynchronousSocketChannel.open();
		System.out.println("Client connects to server");
		this.readyAsynchronousSocketChannel.connect(new InetSocketAddress(hostAddress, port)).get();
	}
	private long receiveLong() throws InterruptedException, ExecutionException{
		//PRE: client is connected to server
		//POST: client receives long from server
		ByteBuffer message = ByteBuffer.allocate(Long.BYTES);
		this.readyAsynchronousSocketChannel.read(message).get();
		return message.getLong();
	}
	private long add(long value) throws InterruptedException, ExecutionException{
		ByteBuffer message = ByteBuffer.allocate(1+Long.BYTES);
		message.put((byte)0);
		message.putLong(value);
		message.flip();
		System.out.println("Sending add message to the server...");
	    this.readyAsynchronousSocketChannel.write(message).get();
		return this.receiveLong();
	}
	private long get() throws InterruptedException, ExecutionException{
		ByteBuffer message = ByteBuffer.allocate(1);
		message.put((byte)1);
		message.flip();
		System.out.println("Sending get message to the server...");
	    this.readyAsynchronousSocketChannel.write(message).get();
		return this.receiveLong();
	}
	public static void main(String[] args) throws InterruptedException, ExecutionException, UnknownHostException, IOException {
		if (args.length != 2) {
            System.err.println(
                "Usage: java Client <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Boolean blocking = true;
        
        try {
			Scanner in = new Scanner(new BufferedReader(new InputStreamReader(new FileInputStream("client.cfg"))));
			blocking=in.nextBoolean();
			System.err.println(
	                "Async="+!blocking);
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println(
	                "No client.cfg, running in async mode");
		}
        Random randomGenerator = new Random();
        long random = randomGenerator.nextLong(); 
		Client 	client = new Client(InetAddress.getByName(hostName), portNumber);
//        long sum=client.get();
//        System.out.println("Client received value: "+sum);
        long sum=client.add(random);
        System.out.println("Client sent value: "+random);
        System.out.println("Client received sum: "+sum);
        client.readyAsynchronousSocketChannel.close();
        System.out.println("Client closed");
	}
}