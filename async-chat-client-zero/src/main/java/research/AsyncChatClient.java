package research;

import java.nio.channels.ClosedChannelException;
import javax.sound.midi.Soundbank;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncChatClient {
  private AsynchronousSocketChannel client;
  private Future<Void> future;
  private static AsyncChatClient instance;
  private Boolean clientOpenState;
  private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


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
      this.clientOpenState = true;
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  public String readHandler() throws ExecutionException, InterruptedException {
    ByteBuffer buffer = ByteBuffer.allocate(512);
    int bytesRead = client.read(buffer).get();
//    System.out.println("Bytes read: " + bytesRead);
    if (bytesRead == -1) {
      this.stop();
    }
//    try {
//      System.out.println("Open state of client: " + client.getRemoteAddress());
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    buffer.flip();
    return new String(buffer.array());
  }

  public void writeHandler() throws ExecutionException, InterruptedException {
    String input = getNextLine();
    client.write(ByteBuffer.wrap(input.getBytes())).get();
  }

  String getNextLine() {
    String line;
    try {
      line = reader.readLine();
      if (line == null) {
        line = "EXIT";
      }
      return line;
    }
    catch (IOException e) {
      e.printStackTrace();
      return "EXIT";
    }
  }

  public void stop() {
    try {
      client.close();
      // The closing of read here causes the client to break,
      // it seem to be a concurrency issue, ie, stop() is called
      // when the reader get a -1 on bytesRead , however on the other thread
      // the reader is still waiting for input.
//      reader.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
//    finally {
//      System.exit(0);
//    }
//    this.clientOpenState = false;
    System.exit(0);

  }

  public static void main(String[] args) throws Exception {
    final AsyncChatClient client = AsyncChatClient.getInstance();
    client.start();
    Executor pool = Executors.newFixedThreadPool(2);
    pool.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {
          String response = null;
          try {
            response = client.readHandler();
          } catch (ExecutionException e) {
            e.printStackTrace();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println(response);
        }
      }
    });
    pool.execute(new Runnable() {
      @Override
      public void run() {
        while (true) {

          try {
            client.writeHandler();
          }
          catch (ExecutionException e) {
            e.printStackTrace();
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }

        }
      }
    });
  }
}
