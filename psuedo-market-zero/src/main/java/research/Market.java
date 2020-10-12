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
  private static Pattern idPattern = Pattern.compile("^Welcome to whisper chat, your ID is (\\d+)$");
  private static String marketId;

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

  void readId() throws ExecutionException, InterruptedException, IOException {
    String msgFromRouter;
    ByteBuffer buffer = ByteBuffer.allocate(64);
    int bytesRead = client.read(buffer).get();
    if (bytesRead == -1) {
      System.out.println("Server has disconnected.");
      // Do other things
      this.client.close();
      System.exit(0);
    }
    buffer.flip();
    msgFromRouter = new String(buffer.array());
    msgFromRouter = msgFromRouter.trim();
    Matcher m = idPattern.matcher(msgFromRouter);
    if (m.find()) {
      this.marketId = m.group(1);
    }
    System.out.println("Market #" + this.marketId + " received");
  }

  void readHandler() throws ExecutionException, InterruptedException, IOException {
    String msgFromRouter;
    String senderId = "";
    String response = "";
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    int bytesRead = client.read(buffer).get();
    if (bytesRead == -1) {
      System.out.println("Server has disconnected.");
      // Do other things
      this.client.close();
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
      market.readId();
      while (true) {
        market.readHandler();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException | IOException e) {
      e.printStackTrace();
    }
  }
}
