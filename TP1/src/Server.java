import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import javax.imageio.ImageIO;

public class Server {
	/*
	 * Application Serveur
	 */
	public static void main(String[] args) throws Exception {
		
		User user = new User();
		String serverAddress; //i.e "127.0.0.1"
		int port; //i.e  5000
		InetAddress serverAddressObj;		
		Scanner userInput = new Scanner(System.in);

		/*
		 * ===================================================================
		 * Informations sur le serveur founies par l utilisateur (IP address, port)
		 * ===================================================================
		 */
		while(true){
	        System.out.println("Fournissez svp l addresse IP du serveur");
	        serverAddress = userInput.nextLine();
	        
			try{
				 serverAddressObj = InetAddress.getByName(serverAddress); //Verification de conformité
			     System.out.println("Cet IP est valide: Merci");
			     break;
			}
			catch(UnknownHostException ex){
			       System.out.println("Votre IP ne respecte pas le format reconnu. Reessayez une autre");
			}
		}

        while (true){
        	System.out.println("Fournissez svp le port d ecoute");
   
        	try{
	        	port = userInput.nextInt();
	        	if (port <= 5050 && port >= 5000)
	        	{
	        		System.out.println("Ce port est valide: Merci");
	        		break;
	        	}
	        	else System.out.println("Le port doit etre compris entre 5000 et 5050 inclusivement, reessayez");
	        }
        	catch (InputMismatchException e)
        	{
        		System.out.println("Verifiez le format du port que vous avez entre, donnez en un autre: Merci");
        		userInput.next();
        	}
        	
        		
        }
        
        /*
         * ========================================================================================
         * CONNEXION AVEC LES CLIENTS ET GESTION DES THREADS POUR LA TRANSFORMATION SOBEL DE L IMAGE
         * ========================================================================================
         */
        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);
		listener.bind(new InetSocketAddress(serverAddressObj, port));
		System.out.format("Information: Le serveur tourne sur: %s:%d%n", serverAddress, port);
		//userInput.close();
		try {
			 while (true) {
				new ClientHandler(listener.accept()).start();
	        }
        } finally {
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
		private User user; 
		
		public ClientHandler(Socket socket)
		{
			this.socket = socket;
		}
		
		/*
		 * Une thread se charge d envoyer au client un message de bienvenue
		 */
		public void run() 
		{
			try
			{
				ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
				
				int taille = 0;
				BufferedImage image = null;
				
				// Verification du login
				String message = null;
				try {
					message = in.readObject().toString();
				} catch (ClassNotFoundException e2) {
					e2.printStackTrace();
				}
				user.username = message.split(":")[0];
				user.password = message.split(":")[1];
				if (User.findUser(user)) {
					System.out.println("La connexion est validee");
	            	out.writeObject("true");
	            	out.flush();
				}
				else {
					System.out.println("La connexion est refusee");
	            	out.writeObject("false");
	            	out.flush();
				}
				
				// GESTION DE L IMAGE
				try {
	                String nomImage = in.readObject().toString();
					String tailleString;
					
					tailleString = in.readObject().toString();
					taille = Integer.parseInt(tailleString);
					out.writeObject("Nous avons les informations sur la taille");
					out.flush();
					
					byte[] tableauImage = (byte[]) in.readObject();
					InputStream byteArrayInputStream = new ByteArrayInputStream(tableauImage);
					image = ImageIO.read(byteArrayInputStream);
		          
					String ipPort = socket.getRemoteSocketAddress().toString();
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();;
					System.out.println("[" + this.user + " - " + ipPort + " - " + dateFormat.format(date) + "] Image " + nomImage + " bien recue");
					out.writeObject("image recue");
					out.flush();
                 } catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				 }
				
					// TRANSFORMATION SOBET ET RETOUR DE L IMAGE TRAITEE
                
				try {
					BufferedImage imageSobel = Sobel.process(image);
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	           	 	ImageIO.write(imageSobel, "jpg", byteArrayOutputStream);
	           	 	
		           	int sobelSize = byteArrayOutputStream.size();
		        	out.writeObject(sobelSize);
		        	out.flush();
		        	byte tableauSobel[] = byteArrayOutputStream.toByteArray();
		        	out.write(tableauSobel, 0, sobelSize);
		        	out.flush();
				} catch (IOException e) {
		            System.out.println("Gestion erreur client: " + e);
		        }
			} catch (IOException e) {
				System.out.println("Gestion erreur client: " + e);
			} catch (NumberFormatException e) {
				e.printStackTrace();
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
					System.out.println("Ne peut pas fermer la connexion. Qu est ce qui ne va pas?");
				}
				System.out.println("Connexion avec le client fermee");
			}
		}
	}
	      
}


