package research;

// https://www.baeldung.com/java-nio2-async-socket-channel

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class AsyncEchoServer2 {
  private AsynchronousServerSocketChannel serverChannel;
//  private AsynchronousSocketChannel clientChannel;
//  private HashMap<Integer, AsynchronousSocketChannel> clientChannels = new HashMap<>();
//  private static Integer clientsIndex = 0;

  public AsyncEchoServer2() {
    try {
      serverChannel = AsynchronousServerSocketChannel.open();
      InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5000);
      serverChannel.bind(hostAddress);
      while (true) {
        System.out.println("While loop started");

        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

          @Override
          public void completed(AsynchronousSocketChannel result, Object attachment) {
            System.out.println("Accept call back initiated");
            if (serverChannel.isOpen())
              serverChannel.accept(null, this);
//            clientChannel = result;
            if ((result != null) && (result.isOpen())) {
              ReadWriteHandler handler = new ReadWriteHandler(result);
              ByteBuffer buffer = ByteBuffer.allocate(32);
              Map<String, Object> readInfo = new HashMap<>();
              readInfo.put("action", "read");
              readInfo.put("buffer", buffer);
              handler.clientChannel.read(buffer, readInfo, handler);
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
          // if noting is inserted on stdin on this process,
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
    AsynchronousSocketChannel clientChannel;

    ReadWriteHandler(AsynchronousSocketChannel clientChannel) {
      this.clientChannel = clientChannel;
    }

    @Override
    public void completed(Integer result, Map<String, Object> attachment) {
      // So this method loops when I kill all sockets, why?
      System.out.println("RW handler started");
      Map<String, Object> actionInfo = attachment;
      String action = (String) actionInfo.get("action");
      if ("read".equals(action)) {
        System.out.println("Doing a read.");
        ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
        System.out.println("Read ByteBuffer pos: " + buffer.position());
        System.out.println("Num of bytes read: " + result);
        // Result from read operation
        if (result == -1) {
          try {
            System.out.println("Closing client.");
            clientChannel.close();
          } catch (IOException e) {
            System.out.println("Error in client close().");
            e.printStackTrace();
          }
        }
        buffer.flip();
        actionInfo.put("action", "write");
        clientChannel.write(buffer, actionInfo, this);
        buffer.clear();
      } else if ("write".equals(action)) {
        System.out.println("Doing a write.");
        ByteBuffer buffer = ByteBuffer.allocate(32);
        actionInfo.put("action", "read");
        actionInfo.put("buffer", buffer);
        clientChannel.read(buffer, actionInfo, this);
      } else {
        System.out.println("***************End of the RW Handler*************************");
      }
      if (clientChannel.isOpen()) {
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<Client socket is open >>>>>>>>>>>>>>>>>>>>>>");
      } else {
        System.out.println("<<<<<<!!!!!!!<<<<<<<<<<<<<<Client socket is Closed >>>>>>>>!!!!!!>>>>>>>>>");
      }
    }

    @Override
    public void failed(Throwable exc, Map<String, Object> attachment) {
      System.out.println("ReadWriteHandler failed() method call:");
      System.out.println(exc.getMessage());
    }

  }

  public static void main(String[] args) {
    new AsyncEchoServer2();
  }

//  public static Process start() throws IOException, InterruptedException {
//    String javaHome = System.getProperty("java.home");
//    String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
//    String classpath = System.getProperty("java.class.path");
//    String className = AsyncEchoServer2.class.getCanonicalName();
//
//    ProcessBuilder builder = new ProcessBuilder(javaBin, "-cp", classpath, className);
//
//    return builder.start();
//  }
}
