package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

class ClientAttachment {
  ByteBuffer buffer = ByteBuffer.allocate(4096);
  AsynchronousSocketChannel client;

  ClientAttachment(AsynchronousSocketChannel client) {
    this.client = client;
  }
}

public class AsyncWhisperChatServer {

  private static BufferedReader blockerReader = new BufferedReader(new InputStreamReader(System.in));

  private HashMap<String, ClientAttachment> brokers = new HashMap<>();
  private HashMap<String, ClientAttachment> markets = new HashMap<>();
  private static int brokersIndex = 1;
  private static int marketsIndex = 1;

  public void acceptBroker() {
    try (final AsynchronousServerSocketChannel brokerChannel = AsynchronousServerSocketChannel.open()) {
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
      brokerChannel.bind(hostAddress);
      while (true) {
        brokerChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
          @Override
          public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (result.isOpen()) {
              brokerChannel.accept(null, this);
              try {
                registerBroker(result);
              } catch (ExecutionException e) {
                System.out.println("Router Error in acceptBroker(): ");
                e.printStackTrace();
              } catch (InterruptedException e) {
                System.out.println("Router Error in acceptBroker(): ");
                e.printStackTrace();
              }
              System.out.println("Broker Connected");
            }
          }

          private void registerBroker(AsynchronousSocketChannel client) throws ExecutionException, InterruptedException {
            String brokerID = Integer.toString(brokersIndex);
            ++brokersIndex;
            client.write(ByteBuffer.wrap(brokerID.getBytes())).get();
            ClientAttachment clientAttachment = new ClientAttachment(client);
            brokers.put(brokerID, clientAttachment);
            System.out.println(brokers.entrySet());
            client.read(clientAttachment.buffer, clientAttachment, new ReadHandler());
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("Router Error in acceptBroker(): " + exc.getMessage());
          }
        });
        System.out.println("Listening on port 5000");
        blocker();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void acceptMarket() {
    try (final AsynchronousServerSocketChannel marketChannel = AsynchronousServerSocketChannel.open()) {
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5001);
      marketChannel.bind(hostAddress);

      while (true) {
        marketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
          @Override
          public void completed(AsynchronousSocketChannel result, Object attachment) {
            if (result.isOpen()) {
              marketChannel.accept(null, this);
              try {
                registerMarket(result);
              } catch (ExecutionException e) {
                System.out.println("Router Error in acceptMarket(): ");
                e.printStackTrace();
              } catch (InterruptedException e) {
                System.out.println("Router Error in acceptMarket(): ");
                e.printStackTrace();
              }
              System.out.println("Market Connected");
            }
          }

          private void registerMarket(AsynchronousSocketChannel client) throws ExecutionException, InterruptedException {
            String marketID = Integer.toString(marketsIndex);
            ++marketsIndex;
            client.write(ByteBuffer.wrap(marketID.getBytes())).get();
            ClientAttachment clientAttachment = new ClientAttachment(client);
            markets.put(marketID, clientAttachment);
            System.out.println(markets.entrySet());
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("Router Error in acceptMarket(): " + exc.getMessage());
          }
        });
        System.out.println("Listening on port 5001");
        blocker();
      }
    } catch (Exception e) {

    }
    System.out.println("Listening on port 5001");
  }

  class ReadHandler implements CompletionHandler<Integer, ClientAttachment> {

    @Override
    public void completed(Integer result, ClientAttachment attachment) {
      if (result != -1) {
        attachment.buffer.flip();
        int limit = attachment.buffer.limit();
        byte[] bytes = new byte[limit];
        attachment.buffer.clear();
        attachment.client.read(attachment.buffer, attachment, this);
      }
    }

    @Override
    public void failed(Throwable exc, ClientAttachment attachment) {

    }
  }

  public static void blocker() {
    try {
      blockerReader.readLine();
      blocker();
    }
    catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }

}