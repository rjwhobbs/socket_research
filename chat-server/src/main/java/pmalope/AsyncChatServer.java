package pmalope;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * NIO Socket Chat Server
 *
 * Reference:
 *
 * @author  Phetho Malope Malope
 * @since   2020-09-28
 * @version 1.0
 */

/*
* TODO
*
* 1. Add debugger
* 2. Establish connection between two clients
* */
public class AsyncChatServer {
    private static final Logger debug = Logger.getLogger("DEBUG").getParent();
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;
    private AsynchronousSocketChannel targetChannel;

    private ClientReference client;

    private static int ID = 0;

    //    private List<ClientReference> clientList;
    private HashMap<Integer, ClientReference> clientTable;
    private Map<String, Object> readInfo;

    private static final StringBuilder availableClients = new StringBuilder();

    private static final int PORT = 5000;
    private static Integer clientsIndex = 0;

    private String clientID;
    private String targetClientID;
    private String welcomeMessage;

    private ByteBuffer clientBuffer;
    private ByteBuffer buffer;


    public static void main(String[] args) throws IOException {
        AsyncChatServer server = new AsyncChatServer();
        server.listenForClient();
        // server.ListenForMarkets();
    }

    public void listenForClient() throws IOException {
        try (AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()){
            InetSocketAddress hostAddress = new InetSocketAddress("localhost", PORT);
            serverChannel.bind(hostAddress);
            while (true) {
                serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @SneakyThrows
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
                        debug.info("Client " + result.getRemoteAddress() + " connected");
                        String availableClientsMessage = "";
                        clientBuffer = ByteBuffer.allocate(10);
                        buffer = ByteBuffer.allocate(1024);
                        clientTable = new HashMap<>();
                        readInfo = new HashMap<>();
                        clientChannel = result;

                        if (serverChannel.isOpen()) {
                            // So this here to open another async listener for more clients
                            // in the background, 'this' refers to the completion handler.
                            serverChannel.accept(null, this);
                        }

                        if (clientChannel != null && clientChannel.isOpen()) {
                            try {
                                client = new ClientReference(clientChannel, false);
                                clientTable.put(++ID, client);
                                clientsDump();

                                welcomeMessage = getWelcomeMessage();
                                availableClientsMessage = getClients();

                                clientChannel.write(ByteBuffer.wrap(welcomeMessage.getBytes())).get();
                                clientChannel.write(ByteBuffer.wrap(availableClientsMessage.getBytes())).get();

                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }


//                            readInfo.put("buffer", buffer);
//                            ReadWriteHandler handler = new ReadWriteHandler(result);
//                            readInfo.put("action", "read");
//                            clientChannel.read(buffer, readInfo, handler);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
                debug.info("Running on " + Thread.currentThread().getName());
                debug.info("Waiting for connection on port " + PORT);
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getClients() {

        for (Map.Entry<Integer, ClientReference> entry : clientTable.entrySet()) {
//            if (ID != entry.getValue().getClientID()) {
                availableClients.append(String.format("\nClient ID: %s\nClient Channel: %s\n",
                        entry.getValue().getClientID(), entry.getValue().getClientChannel()));
//            }
        }
        return availableClients.toString();
    }

    private String getWelcomeMessage() {
        welcomeMessage = "Welcome to the chat server, your ID is: " + ID
                + "\nPlease type the ID of the client you want to chat to:\n";
        return welcomeMessage;
    }

    public void clientsDump() {
        for (Map.Entry<String, ClientReference> entry : clients.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println("Client ID > " + entry.getValue().getID());
            System.out.println("Client Channel > " + entry.getValue().getClient());
            System.out.println(">>>>End<<<<");
        }
    }

    class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {
        AsynchronousSocketChannel currentClient;
        AsynchronousSocketChannel targetClient;

        ReadWriteHandler() {

        }

        ReadWriteHandler(AsynchronousSocketChannel currentClient) {
            this.currentClient = currentClient;
        }

        ReadWriteHandler(AsynchronousSocketChannel currentClient, AsynchronousSocketChannel targetClient) {
            this.currentClient = currentClient;
            this.targetClient = targetClient;
        }

        @SneakyThrows
        @Override
        public void completed(Integer result, Map<String, Object> attachment) {
            System.out.println(":::DEBUG::: Read/Write handler started: " + Thread.currentThread().getName());
            Map<String, Object> actionInfo = attachment;
            String action = (String) actionInfo.get("action");

            if ("read".equals(action)) {
//                ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
//                System.out.println("Message from client " + new String(buffer.array()));
                String response = "Hello " + clientChannel.getRemoteAddress() + " this is " + clientChannel.getLocalAddress() + "\r\n";

                byte[] data = response.getBytes("UTF-8");
                ByteBuffer buffer = ByteBuffer.wrap(data);

//                buffer.flip();
                actionInfo.put("action", "write");
                currentClient.write(buffer, actionInfo, this);
                System.out.println(buffer.hasRemaining());
                buffer.clear();
            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(32);

                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);

                currentClient.read(buffer, actionInfo, this);
            }
        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            System.out.println("ReadWriteHandler failed() method call:");
            System.out.println(exc.getMessage());
        }
    }
}

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
