import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CheckingServer {
	public InetAddress address;
	public int port;

	public static String IP_PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
	public static CheckingServer getServerInfo() {
		CheckingServer info = new CheckingServer();
		Scanner userInput = new Scanner(System.in);

		boolean serverValid = false;
		/*
		 * ===================================================================
		 * Informations sur le serveur founies par l utilisateur (IP address, port)
		 * ===================================================================
		 */

		while (true) {
			System.out.println("Fournissez svp l addresse IP du serveur");
			String add = userInput.nextLine();
	         if(add.matches(IP_PATTERN)) {
	        	 try {
					info.address = InetAddress.getByName(add);
					break;
				} catch (UnknownHostException e) {
					 System.out.println("Votre IP ne respecte pas le format reconnu. Reessayez une autre");
				}
	        	 
	         }else {
	        	 System.out.println("Votre IP ne respecte pas le format reconnu. Reessayez une autre");
	         }
		}

		while (true) {
			System.out.println("Fournissez svp le port d ecoute");
			try {
				info.port = userInput.nextInt();
				if (info.port <= 5050 && info.port >= 5000) {
					System.out.println("Ce port est valide: Merci");
					break;
				} else
					System.out.println("Le port doit etre compris entre 5000 et 5050 inclusivement, reessayez");
			} catch (InputMismatchException e) {
				System.out.println("Verifiez le format du port que vous avez entre, donnez en un autre: Merci");
				userInput.next();
			}

		}
		return info;
	}
	
	
	public static Socket ValidateSoket() {
		Socket socket = null;
		Scanner userInput = new Scanner(System.in);
		boolean serverValid = false;
		do {			
			CheckingServer serverInfo = CheckingServer.getServerInfo();
			try {
				socket = new Socket(serverInfo.address, serverInfo.port);
				
				serverValid = true;
			} catch (IOException e) {
				System.out.println("Serveur introuvable");
			}
		} while (!serverValid);
		return socket;
	}
	
	public static boolean ValidateUser(DataOutputStream out, DataInputStream in) {
		boolean userValid = false;
		Scanner userInput = new Scanner(System.in);
		User user = new User();
		System.out.println("Svp, bien vouloir fournir votre nom d utilisateur :");
		user.username = userInput.next();
		System.out.println("Svp, bien vouloir fournir votre mot de passe :");
		user.password = userInput.next();

		try {
			out.writeUTF(user.username + ":" + user.password);
			userValid = in.readBoolean();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return userValid;
	}
}
