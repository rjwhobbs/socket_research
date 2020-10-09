package research;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AsyncWhisperChatServer {

  private static BufferedReader blockerReader = new BufferedReader(new InputStreamReader(System.in));

//  private List<Client> brokerTable;
//  private List<Client> marketTable;

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
              System.out.println("Broker Connected");
            }
          }

          @Override
          public void failed(Throwable exc, Object attachment) {

          }
        });
        System.out.println("Listening on port 5000");
        blocker();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   *
   */
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
              System.out.println("Market Connected");
            }
          }

          @Override
          public void failed(Throwable exc, Object attachment) {
            System.out.println("Something went wrong");
          }
        });
        System.out.println("Listening on port 5001");
        blocker();
      }
    } catch (Exception e) {

    }
    System.out.println("Listening on port 5001");
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
