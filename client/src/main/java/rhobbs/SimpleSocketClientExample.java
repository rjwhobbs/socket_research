package rhobbs;

/**
 * Hello world!
 *
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class SimpleSocketClientExample
{
    public static void main( String[] args )
    {
        if( args.length == 0 )
        {
            System.out.println( "Usage: <port>" );
            System.exit( 0 );
        }
        String port = args[ 0 ];
//        String path = args[ 0 ];

        System.out.println( "Loading contents of port: " + port );

        try
        {
            // Connect to the server
            Socket socket = new Socket( "localhost", Integer.parseInt(port) );

            // Create input and output streams to read from and write to the server
            PrintStream out = new PrintStream( socket.getOutputStream() );
            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );

            // Follow the HTTP protocol of GET <path> HTTP/1.0 followed by an empty line
            out.println( "I am client." );
            out.println();

            // Read data from the server until we finish reading the document
            String line = in.readLine();
            while( line != null )
            {
                System.out.println( line );
                line = in.readLine();
            }

            // Close our streams
            in.close();
            out.close();
            socket.close();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
