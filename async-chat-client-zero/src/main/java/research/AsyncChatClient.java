package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncChatClient {
  private AsynchronousSocketChannel client;
  private Future<Void> future;
  private static AsyncChatClient instance;

  private AsyncChatClient() {
    try {
      client = AsynchronousSocketChannel.open();
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
      future = client.connect(hostAddress);
      start();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static AsyncChatClient getInstance() {
    if (instance == null)
      instance = new AsyncChatClient();
    return instance;
  }

  private void start() {
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  public String readHandler() throws ExecutionException, InterruptedException {
    ByteBuffer buffer = ByteBuffer.allocate(512);
    client.read(buffer).get();
    buffer.flip();
    return new String(buffer.array());
  }

  public void stop() {
    try {
      client.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    AsyncChatClient client = AsyncChatClient.getInstance();
    client.start();
//    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//    String line;
//    System.out.println("Message to server:");
    // If the server shuts down this goes on an infinite loop, not that isn't already.
    while (true) {
      String response = client.readHandler();
      System.out.println("response from server: " + response);
//      System.out.println("Message to server:");
    }
  }
}
