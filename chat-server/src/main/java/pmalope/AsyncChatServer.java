package pmalope;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

class ClientReference {
    private String ID;
    private AsynchronousSocketChannel client;
    private Boolean acceptedChat;

    ClientReference() {}

    ClientReference(
            String ID,
            AsynchronousSocketChannel client,
            Boolean acceptedChat
    ) {
        this.ID = ID;
        this.client = client;
        this.acceptedChat = acceptedChat;
    }

    public String getID() {
        return ID;
    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public Boolean hasAcceptedChat() {
        return acceptedChat;
    }

    public void setAcceptedChat(Boolean acceptedChat) {
        this.acceptedChat = acceptedChat;
    }
}

public class AsyncChatServer {
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;
    private static final int PORT = 5000;
    private HashMap<String, ClientReference> clients = new HashMap<>();
    private static Integer clientsIndex = 0;

    public void listenForClient() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", PORT);
            serverChannel.bind(hostAddress);
            while (true) {
                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        System.out.println(":::DEBUG::: Client connected");
                        System.out.println(":::DEBUG::: Accept call back initiated");
                        String newClientID;
                        String targetClientID;
                        String welcomeMessage;
                        ByteBuffer clientInput = ByteBuffer.allocate(10);
                        ByteBuffer buffer = ByteBuffer.allocate(32);
                        Map<String, Object> readInfo = new HashMap<>();
                        ClientReference client;
                        clientChannel = result;

//                        TODO to be moved
                        clientChannel.write( ByteBuffer.wrap( "Hello, I am Chat Server 2020\n".getBytes() ) );

                        if (serverChannel.isOpen()) {
                            // So this here to open another async listener for more clients
                            // in the background, 'this' refers to the completion handler.
                            serverChannel.accept(null, this);
                        }
                        if ((clientChannel != null) && (clientChannel.isOpen())) {
                            newClientID = Integer.toString(++clientsIndex);
                            client = new ClientReference(newClientID, clientChannel, false);
                            clients.put(newClientID, client);
                            System.out.println(":::DEBUG::: Client dump: ");
                            clientsDump();
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
                System.out.println("Waiting for connection on port " + PORT);
                System.out.println(":::DEBUG::: Outside the accept callback");
                try {
                    // This method of "pausing" the while loop only works
                    // if nothing is inserted on stdin on this process,
                    // not ideal.
                    System.in.read();
                } catch (IOException e) {
                    System.out.println("Inner catch");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientsDump() {
        for (Map.Entry<String, ClientReference> entry : clients.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println("Client ID > " + entry.getValue().getID());
            System.out.println("Client Channel > " + entry.getValue().getClient());
            System.out.println(">>>>End<<<<");
        }
    }

    public static void main(String[] args) {
        AsyncChatServer server = new AsyncChatServer();
        server.listenForClient();
        // server.ListenForMarkets();
    }
}
