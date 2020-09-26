package research;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
//  private AsynchronousSocketChannel clientChannel;
  private HashMap<String, Object> clientChannels = new HashMap<>();
  private static Integer clientsIndex = 0;

  public void ListenForBrokers() {
    try {
      serverChannel = AsynchronousServerSocketChannel.open();
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
      serverChannel.bind(hostAddress);
      while (true) {
        System.out.println("While loop started");

        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

          @Override
          public void completed(AsynchronousSocketChannel newClient, Object attachment) {
            System.out.println("Accept call back initiated");
            String newClientID;
            ByteBuffer clientInput = ByteBuffer.allocate(10);
            String targetClientID;
            AsynchronousSocketChannel targetClient;
            String welcomeMessage;
            ByteBuffer buffer = ByteBuffer.allocate(32);
            Map<String, Object> readInfo = new HashMap<>();
            ClientReference newClientRef;

            if (serverChannel.isOpen()) {
              // So this here to open another async listener for more clients
              // in the background, 'this' refers to the completion handler.
              serverChannel.accept(null, this);
            }
            if ((newClient != null) && (newClient.isOpen())) {
              newClientID = Integer.toString(clientsIndex);
              newClientRef = new ClientReference(newClientID, newClient, false);
              clientChannels.put(newClientID, newClientRef);
              System.out.println("Client Channels dump: " + clientChannels.entrySet());
              clientsIndex++;
              readInfo.put("buffer", buffer);
              // So the get() calls on these overloaded write() and read()'s block,
              // they will only return once they're done but they need
              // to block because the client needs to know their ID, and we
              // need to know which target client the current client wants
              // to communicate with.
              try {
                welcomeMessage = "Welcome to the chat server, your ID is: "
                        + newClientID
                        + "\nPlease type the ID of the client you want to chat to:\n";
                newClient.write(ByteBuffer.wrap(welcomeMessage.getBytes())).get();
                newClient.read(clientInput).get();
                targetClientID = new String(clientInput.array());
                targetClientID = targetClientID.trim();
                ClientReference targetClientObject;
                targetClientObject = (ClientReference) clientChannels.get(targetClientID);
                System.out.println("The clients input: " + targetClientID + "#");
                targetClient = targetClientObject.getClient();
                newClientRef.setAcceptedChat(true);
                readInfo.put("targetClient", targetClientObject);
                readInfo.put("currentClient", newClientRef);
                if (targetClient != null) {
//                  readInfo.put("targetClient", targetClient);
                  ReadWriteHandler handler = new ReadWriteHandler(newClient, targetClient);
                  handler.currentClient.read(buffer, readInfo, handler);
                }
                else {
                  String failMessage = "Unfortunately the ID: #"
                          + targetClientID
                          + " doens't seem to be active, please reconnect.";
                  System.out.println("Error: The server couldn't find the target client ID.");
                  newClient.write(ByteBuffer.wrap(failMessage.getBytes())).get();
                }
              } catch (InterruptedException e) {
                e.printStackTrace();
              } catch (ExecutionException e) {
                e.printStackTrace();
              }
            }
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("There was an error in the completion handler.");
            System.out.println(exc.getMessage());
          }
        });
        System.out.println("------------Outside the accept callback.----------------");
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
      System.out.println("Outer catch");
      e.printStackTrace();
    }
  }

  class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {
    AsynchronousSocketChannel currentClient;
    AsynchronousSocketChannel targetClient;

    ReadWriteHandler(
            AsynchronousSocketChannel currentClient,
            AsynchronousSocketChannel targetClient
    ) {
      this.currentClient = currentClient;
      this.targetClient = targetClient;
    }

    @Override
    public void completed(Integer result, Map<String, Object> attachment) {
      System.out.println("RW handler started: " + Thread.currentThread().getName());
      ByteBuffer bufferToTarget = (ByteBuffer) attachment.get("buffer");
//      ByteBuffer freshBuffer
      ClientReference targetClientRef = (ClientReference) attachment.get("targetClient");
      ClientReference currentClientRef = (ClientReference) attachment.get("currentClient");
      System.out.println(targetClientRef.hasAcceptedChat());
      String formattedMessage;

      try {
        System.out.println("Trying to connect to: " + targetClient.getRemoteAddress().toString() +
                " from " + currentClient.getRemoteAddress().toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        String message = new String(bufferToTarget.array());
        System.out.println(message);

        if (!targetClientRef.hasAcceptedChat()) {
          formattedMessage = "client #"
                  + currentClientRef.getID()
                  + ": " + message
                  + "To reply to this message type the clients ID #:\n";
        }
        else {
          formattedMessage = "client #"
                  + currentClientRef.getID()
                  +  ": " + message;
        }

        bufferToTarget.flip();
        targetClient.write(ByteBuffer.wrap(formattedMessage.getBytes())).get();
        bufferToTarget.clear();
        bufferToTarget.put(new byte[32]);
        bufferToTarget.clear();
        attachment.put("buffer", bufferToTarget);
        currentClient.read(bufferToTarget, attachment, this);
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
      System.out.println("Done sending one message from client to target.");
    }

    @Override
    public void failed(Throwable exc, Map<String, Object> attachment) {
      System.out.println("ReadWriteHandler failed() method call:");
      System.out.println(exc.getMessage());
    }

  }

  public static void main(String[] args) {
    AsyncChatServer server = new AsyncChatServer();
    server.ListenForBrokers();
    // server.ListenForMarkets();
  }
}
