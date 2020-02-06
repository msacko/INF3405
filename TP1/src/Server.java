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
		Scanner userInput = new Scanner(System.in);
		
		CheckingServer serverInfo = CheckingServer.getServerInfo();
		ServerSocket listener = new ServerSocket();
		listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverInfo.address, serverInfo.port));
		System.out.format("Information: Le serveur tourne sur: %s:%d%n", serverInfo.address.getHostAddress(), serverInfo.port);
		try {
			while (true) {
				new ClientHandler(listener.accept()).start();
			}
		} finally {
			listener.close();
		}

	}

	private static class ClientHandler extends Thread {

		private Socket socket;
		User user = new User();

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

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
				}
				System.out.println("Connexion avec le client " + user.username + " fermée");
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
				int dataRead = 0;
				int offset = 0;
				int sizeLeft = imageSize;
				int packetSize = 5000;
				while ((dataRead = in.read(imageArray, offset, packetSize)) > 0) {
					sizeLeft -= dataRead;
					offset += dataRead;
					System.out.println(" Reception d'image en cours, " + offset + "/" + imageSize + " packets recus...");
					if(sizeLeft <= 5000) {
						packetSize = sizeLeft;
					}
				}
				
				//Affichage des infos
				String ipPort = socket.getRemoteSocketAddress().toString();
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date();
				System.out.println("Traitement du client " + user.username + " - [ Port : " + ipPort + " ] - " + dateFormat.format(date) + " - Image " + imageName);

				ByteArrayInputStream imgByteArray = new ByteArrayInputStream(imageArray);
				BufferedImage imgBuff = ImageIO.read(imgByteArray);
				
				//Traitement de l'image
				BufferedImage imageSobel = Sobel.process(imgBuff);
				
				ByteArrayOutputStream imgSobelByteArray = new ByteArrayOutputStream();
				ImageIO.write(imgBuff, "jpg", imgSobelByteArray);
				imgSobelByteArray.flush();
				
				imageArray = imgSobelByteArray.toByteArray();
				imageSize = imageArray.length;
			
				out.writeInt(imageSize);
				out.write(imageArray, 0, imageSize);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
