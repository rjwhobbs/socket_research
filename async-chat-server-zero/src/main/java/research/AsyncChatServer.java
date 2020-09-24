package research;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AsyncChatServer {
  private AsynchronousServerSocketChannel serverChannel;
//  private AsynchronousSocketChannel clientChannel;
  private HashMap<String, AsynchronousSocketChannel> clientChannels = new HashMap<>();
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
            if (serverChannel.isOpen()) {
              // So this here to open another async listener for more clients
              // in the background, 'this' refers to the completion handler.
              serverChannel.accept(null, this);
            }
            if ((newClient != null) && (newClient.isOpen())) {
              newClientID = Integer.toString(clientsIndex);
              clientChannels.put(newClientID, newClient);
              System.out.println("Client Channels dump: " + clientChannels.entrySet());
              clientsIndex++;
              ReadWriteHandler handler = new ReadWriteHandler(newClientID);
              ByteBuffer buffer = ByteBuffer.allocate(32);
              Map<String, Object> readInfo = new HashMap<>();
              readInfo.put("action", "read");
              readInfo.put("buffer", buffer);
              // So the get() calls on these overloaded write() and read()'s block,
              // they will only return once they're done but they need
              // to block because the client needs to know their ID, and we
              // need to know which target client the current client wants
              // to communicate with.
              try {
                newClient.write(ByteBuffer.wrap(newClientID.getBytes())).get();
                newClient.read(clientInput).get();
                targetClientID = new String(clientInput.array());
                targetClientID = targetClientID.trim();
                targetClient = clientChannels.get(targetClientID);
                System.out.println("The clients input: " + targetClientID + "#");
                if (targetClient != null) {
                  readInfo.put("targetClient", targetClient);
                  handler.currentClient.read(buffer, readInfo, handler);
                }
                else {
                  System.out.println("Error: The server couldn't find the target client ID.");
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
    String currentClientID;

    ReadWriteHandler(String clientID) {
      this.currentClientID = clientID;
      this.currentClient = clientChannels.get(clientID);
    }

    @Override
    public void completed(Integer result, Map<String, Object> attachment) {
      System.out.println("RW handler started");
      Map<String, Object> actionInfo = attachment;
      AsynchronousSocketChannel targetClient = (AsynchronousSocketChannel) attachment.get("targetClient");
      ByteBuffer messageToTarget = (ByteBuffer) actionInfo.get("buffer");
      try {
        System.out.println("Trying to connect to: " + targetClient.getRemoteAddress().toString() +
                " from " + currentClient.getRemoteAddress().toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        String debug = new String(messageToTarget.array());
        System.out.println(debug);
        messageToTarget.flip();
        targetClient.write(messageToTarget).get();
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
  }
}
