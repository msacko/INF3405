import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

public class Server {
	private static ServerSocket listener;

	/*
	 * Application Serveur
	 */
	public static void main(String[] args) throws Exception {
		// Compteur incr�ment� � chaque connexion d'un client au serveur
		int clientNumber = 0;

		// Adresse et port du serveur
		String serverAddress = "127.0.0.1";
		int serverPort = 5000;

		// Cr�ation de la connexion pour communiquer avec les clients
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);

		// Association de l'adresse et du port � la connexion
		listener.bind(new InetSocketAddress(serverIP, serverPort));

		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);

		try {
			/*
			 * A chaque fois qu'un nouveau client se connecte, on ex�cute la fonction Run()
			 * de l'objet ClientHandler.
			 */
			while (true) {
				// Important : la fonction accept() est bloquante : on attend qu'un prochain
				// client se connecte
				// Une nouvelle connection : on incr�mente le compteur clientNumber	
				
				Socket socket = listener.accept();
				new ClientHandler(socket, clientNumber++).start();
				
				Server server = new Server();
				ImageUploadSocketRunnable imgUploadServer = server.new ImageUploadSocketRunnable(socket);
				//ImageUploadSocketRunnable imgUploadServer = new ImageUploadSocketRunnable(socket);
			    Thread thread=new Thread(imgUploadServer);
			    thread.start();
			}
		} finally {
			// Fermeture de la connexion
			listener.close();
		}

	}


	
	
	
	/*
	 * Une thread qui se charge de traiter la demande de chaque client
	 * sur un socket particulier
	 */
	private static class ClientHandler extends Thread
	{
		private Socket socket;
		private int clientNumber; 
		
		public ClientHandler(Socket socket, int clientNumber)
		{
			this.socket = socket;
			this.clientNumber = clientNumber;
			System.out.println("New connection with client#" + clientNumber + " at " + socket);
		}
		
		/*
		 * Une thread se charge d'envoyer au client un message de bienvenue
		 */
		public void run()
		{
			try
			{
				// Cr�ation d'un canal sortant pour envoyer des messages au client
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				// Envoie d'un message au client
				out.writeUTF("Hello from server - you are client#" + clientNumber);
				
			} catch (IOException e)
			{
				System.out.println("Error handling client# " + clientNumber + ": " + e);
			}
			finally
			{
				try
				{
					// Fermeture de la connexion avec le client
					socket.close();
				}
				catch (IOException e)
				{
					System.out.println("Couldn't close a socket, what's going on?");
				}
				System.out.println("Connection with client# " + clientNumber + " closed");
			}
		}
	}


	public class ImageUploadSocketRunnable implements Runnable{       
	    public static final String dir="resources/";
	    Socket soc=null;
	   ImageUploadSocketRunnable(Socket soc){
	     this.soc=soc;
	   }
	    @Override
	    public void run() {
	    InputStream inputStream = null;
	       try {
	           inputStream = this.soc.getInputStream();
	           System.out.println("Reading: " + System.currentTimeMillis());
	           byte[] sizeAr = new byte[4];
	           inputStream.read(sizeAr);
	           int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
	           byte[] imageAr = new byte[size];
	           inputStream.read(imageAr);
	           BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageAr));
	           System.out.println("Received " + image.getHeight() + "x" + image.getWidth() + ": " + System.currentTimeMillis());
	           ImageIO.write(image, "jpg", new File(dir+System.currentTimeMillis()+".jpg"));
	           inputStream.close();
	       } catch (IOException ex) {
	           Logger.getLogger(ImageUploadSocketRunnable.class.getName()).log(Level.SEVERE, null, ex);
	       }

	    }

	    
	}
	       
	       
	  

}


