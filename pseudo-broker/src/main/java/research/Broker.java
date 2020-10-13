package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Broker {
  private AsynchronousSocketChannel client;
  private Future<Void> future;
  private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private static BufferedReader blockerReader = new BufferedReader(new InputStreamReader(System.in));
  private static Pattern senderPattern = Pattern.compile("^market#(\\d+)");
  private static Pattern idPattern = Pattern.compile("^Welcome to whisper chat, your ID is (\\d+)$");
  private static String brokerId;
  private HashMap<String, Object> attachment = new HashMap<>();

  Broker() {
    try {
      client = AsynchronousSocketChannel.open();
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
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
      this.brokerId = m.group(1);
    }
    System.out.println("Broker id #" + this.brokerId + " received");
  }

  void readWriteHandler() throws ExecutionException, InterruptedException, IOException {
    String msgFromRouter;
    String senderId = "";
    String response = "";
    ByteBuffer buffer = ByteBuffer.allocate(4096);
    String line = reader.readLine();
    if (line != null) {
      // If the server disconnects before this is run an IO exception will be thrown.
      client.write(ByteBuffer.wrap(line.getBytes())).get();
    }
    else {
      System.out.println("Server has disconnected.");
      this.client.close();
      reader.close();
      System.exit(0);
    }
    int bytesRead = client.read(buffer).get();
    if (bytesRead == -1) {
      System.out.println("Server has disconnected.");
      this.client.close();
      reader.close();
      System.exit(0);
    }
    buffer.flip();
    msgFromRouter = new String(buffer.array());
    System.out.println(msgFromRouter);
  }

  private void readHandler() {
    ByteBuffer buffer = ByteBuffer.allocate(512);
    ReadAttachment readAttachment = new ReadAttachment(buffer);
    client.read(readAttachment.buffer, readAttachment, new ReadHandler());
    blocker();
  }

  class ReadHandler implements CompletionHandler<Integer, ReadAttachment> {
    @Override
    public void completed(Integer result, ReadAttachment attachment) {
      if (result != -1) {
        System.out.println("Here bru: " + result);
        attachment.buffer.flip();
        attachment.buffer.clear();
        client.read(attachment.buffer, attachment, this);
      }
    }

    @Override
    public void failed(Throwable exc, ReadAttachment attachment) {

    }
  }

  class ReadAttachment {
    public ByteBuffer buffer;

    ReadAttachment(ByteBuffer buffer) {
      this.buffer = buffer;
    }
  }

  public static void blocker() {
    try {
      blockerReader.readLine();
      blocker();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args) {
    Broker broker = new Broker();
    // Needs error handling for in case the server isn't running.
    try {
      broker.readId();
      while (true) {
        broker.readHandler();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
