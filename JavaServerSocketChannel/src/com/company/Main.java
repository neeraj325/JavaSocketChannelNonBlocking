package com.company;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Main {

    private static ServerSocketChannel serverSocketChannel;
    private static Selector selector;
    private static Set set;
    private static final int PORT = 4432;
    public static void main(String[] args) {
        ServerSocket serverSocket;

        System.out.println("Opening server on port>>" + PORT);

        try{
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocket = serverSocketChannel.socket();

            InetSocketAddress inetSocketAddress = new InetSocketAddress(PORT);

            serverSocket.bind(inetSocketAddress);

            selector = Selector.open();

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException ioException){
            System.out.println("[+]Some exception ocurred" + ioException);
            System.exit(1);
        }
        processConnection();
	
    }

    private static void processConnection(){
        do{
            try{
                int numKeys = selector.select();

                if(numKeys>0){
                    set = selector.selectedKeys();

                    Iterator keyCycler = set.iterator();

                    while(keyCycler.hasNext()){
                        SelectionKey key = (SelectionKey)keyCycler.next();

                        int keyOps = key.readyOps();

                        if((keyOps & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT){
                            acceptConnection(key);
                            continue;
                        }
                        if((keyOps & SelectionKey.OP_READ) == SelectionKey.OP_READ){
                            readData(key);
                            continue;
                        }
                    }
                }
            }
            catch (IOException ioException){
                System.out.println("[+]Some error occured>>" + ioException);
            }
        }while (true);
    }

    private static void acceptConnection(SelectionKey selectionKey){
        SocketChannel socketChannel;
        Socket socket;
        try{
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel = serverSocketChannel.accept();

            socket = socketChannel.socket();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
            System.exit(0);
        }
        selector.selectedKeys().remove(selectionKey);
    }

    private static void readData(SelectionKey key){
        SocketChannel socketChannel;

        try{
            socketChannel = (SocketChannel)key.channel();

            ByteBuffer buffer = ByteBuffer.allocate(2048);

            buffer.clear();

            int numBytes = socketChannel.read(buffer);

            System.out.println("Number of bytes read>" + numBytes);

            Socket socket;

            socket = socketChannel.socket();

            if(numBytes == -1){
                key.cancel();

                System.out.println("Canceling key......\n[+]Closing socket>"+ socket);

                closeSocket(socket);
            }
            else{
                try{
                    while(buffer.remaining()>0){
                        socketChannel.write(buffer);
                    }
                }
                catch (IOException ioException){
                    buffer.flip();

                    System.out.println("Some exception ocurred>" + ioException);
                    ioException.printStackTrace();
                }
            }
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    private static void closeSocket(Socket socket){
        try{
            if(socket!=null){
                socket.close();

                
            }
        }
        catch (IOException ioException){
            System.out.println("[+]Closing socket>>" + ioException);
        }
    }

}
