import java.io.DataInputStream;
import java.net.Socket;
public class Client {
	
	private static Socket socket;
	
	public static void main(String[] args) throws Exception
	{
		String serverAddress = "127.0.0.1";
		int port = 5000;
		
		User user = new User();
		user.username = "Georges";
		user.passord = "Password";
		
		User.createUser(user);
		User.createUser(user);
		User.createUser(user);
		
		socket = new Socket(serverAddress, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessageFromServer = in.readUTF();
		
		socket.close();
	}

}
