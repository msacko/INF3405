import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.UUID;
import java.util.HashMap; // import the HashMap class


import javax.imageio.ImageIO;

public class Server {
	/*
	 * Application Serveur
	 */
	public static void main(String[] args) throws Exception {
		String serverAddress; // i.e "127.0.0.1"

		int port; // i.e 5000
		InetAddress serverAddressObj;
		Scanner userInput = new Scanner(System.in);
		
		

		/*
		 * ===================================================================
		 * Informations sur le serveur founies par l utilisateur (IP address, port)
		 * ===================================================================
		 */
		while (true) {
			System.out.println("Fournissez svp l addresse IP du serveur");
			serverAddress = userInput.nextLine();

			try {
				serverAddressObj = InetAddress.getByName(serverAddress); // Verification de conformité
				System.out.println("Cet IP est valide: Merci");
				break;
			} catch (UnknownHostException ex) {
				System.out.println("Votre IP ne respecte pas le format reconnu. Reessayez une autre");
			}
		}

		while (true) {
			System.out.println("Fournissez svp le port d ecoute");

			try {
				port = userInput.nextInt();
				if (port <= 5050 && port >= 5000) {
					System.out.println("Ce port est valide: Merci");
					break;
				} else
					System.out.println("Le port doit etre compris entre 5000 et 5050 inclusivement, reessayez");
			} catch (InputMismatchException e) {
				System.out.println("Verifiez le format du port que vous avez entre, donnez en un autre: Merci");
				userInput.next();
			}
		}

		/*
		 * =============================================================================
		 * =========== CONNEXION AVEC LES CLIENTS ET GESTION DES THREADS POUR LA
		 * TRANSFORMATION SOBEL DE L IMAGE
		 * =============================================================================
		 * ===========
		 */
		ServerSocket listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverAddressObj, port));
		System.out.format("Information: Le serveur tourne sur: %s:%d%n", serverAddress, port);
		// userInput.close();
		try {
			while (true) {
				new ClientHandler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}

	}

	/*
	 * Une thread qui se charge de traiter la demande de chaque client sur un socket
	 * particulier
	 */
	private static class ClientHandler extends Thread {

		private Socket socket;
		User user = new User();

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		/*
		 * Une thread se charge d envoyer au client un message de bienvenue
		 */
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				DataInputStream in = new DataInputStream(socket.getInputStream());
				
				manageAuthentication(out, in);
				manageImage(socket, out, in);

			} catch (IOException e) {
				System.out.println("Gestion erreur client: " + e);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Ne peut pas fermer la connexion. Qu est ce qui ne va pas?");
				}
				System.out.println("Connexion avec le client fermee");
			}
		}

		public void manageAuthentication(DataOutputStream out, DataInputStream in) {
			// Verification du login
			String request = "";
			try {
				request = in.readUTF();
				user.username = request.split(":")[0];
				user.password = request.split(":")[1];
				out.writeBoolean(User.Login(user));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public void manageImage(Socket socket, DataOutputStream out, DataInputStream in) {
			try {
				
				//Reception du nom de l'image
				String imageName = in.readUTF();
				int imageSize = in.readInt();
				byte[] imageArray = new byte[imageSize];
				int nRead = 0;
				int off = 0;
				int sizeLeft = imageSize;
				int packetSize = 5000;
				while ((nRead = in.read(imageArray, off, packetSize)) > 0) {
					sizeLeft -= nRead;
					off += nRead;
					System.out.println(nRead + " bytes Recieved....");
					if(sizeLeft <= 5000) {
						packetSize = sizeLeft;
					}
				}
				
				//Affichage des infos
				String ipPort = socket.getRemoteSocketAddress().toString();
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println("[" + user.username + " - " + ipPort + " - " + dateFormat.format(date) + "] Image " + imageName
						+ " bien recue");

				ByteArrayInputStream imgByteArray = new ByteArrayInputStream(imageArray);
				BufferedImage imgBuff = ImageIO.read(imgByteArray);
				
				//Traitement de l'image
				BufferedImage imageSobel = Sobel.process(imgBuff);
				
				ByteArrayOutputStream imgSobelByteArray = new ByteArrayOutputStream();
				ImageIO.write(imgBuff, "jpg", imgSobelByteArray);
				imgSobelByteArray.flush();
				
				imageArray = imgSobelByteArray.toByteArray();
				imageSize = imageArray.length;
				
				System.out.println("size de Nouveau image : " + imageSize);
				
				out.writeInt(imageSize);
				out.write(imageArray, 0, imageSize);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
