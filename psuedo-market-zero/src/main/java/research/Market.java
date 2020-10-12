package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Market {
  private AsynchronousSocketChannel client;
  private Future<Void> future;
//  private static AsyncChatClient instance;
  private Boolean clientOpenState;
  private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

  Market() {
    try {
      client = AsynchronousSocketChannel.open();
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5001);
      future = client.connect(hostAddress);
      future.get();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }

  void readHandler() throws ExecutionException, InterruptedException {
    String msgFromRouter;
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    int bytesRead = client.read(buffer).get();
    if (bytesRead == -1) {
      System.out.println("Server has disconnected.");
      // Do other things
    }
    buffer.flip();
    msgFromRouter = new String(buffer.array());
    System.out.println(msgFromRouter);
  }

  public static void main(String[] args) {
    Market market = new Market();
    try {
      market.readHandler();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
