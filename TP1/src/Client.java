import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;


public class Client {
	
	private static Socket socket;
	
	public static void main(String[] args) throws Exception
	{
		String serverAddress = "127.0.0.1";
		int port = 5000;
		
		User user = new User();
		user.username = "Georges";
		user.password = "Password";
		
		for(int i = 0; i<10; i++) {
			User.createUser(user);
		}

		socket = new Socket(serverAddress, port);
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessageFromServer = in.readUTF();
		
		/*
		 * image to send from the side client
		 */
		//==================================================		
	    OutputStream outputStream = socket.getOutputStream();
	    //File file = new File("inputImage.jpg");
	    URL url  = Client.class.getResource("/resource/inputImage.jpg");
	    if (url == null)
	        System.out.println( "Could not find image!" );
	    else
	    	System.out.println( "Image Found!" );
	    BufferedImage image = ImageIO.read(url);	    
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ImageIO.write(image, "jpg", byteArrayOutputStream);
	    byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
	    outputStream.write(size);
	    outputStream.write(byteArrayOutputStream.toByteArray());
	    outputStream.flush();
	    //===================================================
		
	    socket.close();
	}

}
