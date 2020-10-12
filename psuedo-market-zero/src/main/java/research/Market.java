package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Market {
  private AsynchronousSocketChannel client;
  private Future<Void> future;
//  private static AsyncChatClient instance;
//  private Boolean clientOpenState;
  private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private static Pattern senderPattern = Pattern.compile("^broker#(\\d+)");

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
    String senderId = "";
    String response = "\1 potato";
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    int bytesRead = client.read(buffer).get();
    if (bytesRead == -1) {
      System.out.println("Server has disconnected.");
      // Do other things
      System.exit(0);
    }
    buffer.flip();
    msgFromRouter = new String(buffer.array());
    System.out.println(msgFromRouter);
    Matcher m = senderPattern.matcher(msgFromRouter);
    if (m.find()) {
      senderId = m.group(1);
      response = "\\" + senderId + " acknowledged\n";
      client.write(ByteBuffer.wrap(response.getBytes())).get();
    }
  }

  public static void main(String[] args) {
    Market market = new Market();
    // readId
    try {
      while (true) {
        market.readHandler();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
