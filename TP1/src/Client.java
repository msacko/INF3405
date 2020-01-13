import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.imageio.ImageIO;


public class Client {
	
	/*
	 * Application client
	 */
	public static void main(String[] args) throws Exception
	{
		Socket socket;
		String serverAddress; //i.e "127.0.0.1"
		int port; //i.e  5000
		InetAddress serverAddressObj;
		
		User user = new User();
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
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
				 serverAddressObj = InetAddress.getByName(serverAddress); 
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
         * ==========================================================
         * INFORMATIONS SUR L'UTILISATEUR (username, et mot de passe)
         * ==========================================================
         */
        userInput.nextLine();
        System.out.println("Svp, bien vouloir fournir votre nom d utilisateur :");
        user.username = userInput.nextLine();
        System.out.println("Svp, bien vouloir fournir votre mot de passe :");
        user.password = userInput.nextLine();

        /*
         * =========================================================
         * CONNEXION - ECHANGES D'INFORMATIONS NOTAMMENT SUR L'IMAGE
         * =========================================================
         */
        try {
		    socket = new Socket(serverAddressObj, port);	
		    out = new ObjectOutputStream(socket.getOutputStream()); 
            out.writeObject(user.username + ":" + user.password);
		    String messageFournieUtilisateur = null;
		    in = new ObjectInputStream(socket.getInputStream());	
		    
			try {
				messageFournieUtilisateur = in.readObject().toString();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            if (messageFournieUtilisateur.equals("true")) {
            	
            	 System.out.println("============================================================================================");
	           	 System.out.println("Tout est beau. On peut démarrer la phase des échanges d'information sur les images à traiter");
	           	 System.out.println("============================================================================================");
	           	 
	           	 System.out.println("Svp, bien vouloir fournir le nom de l image a traiter sous la forme nom_image.jpg) : ");
	           	 String nomImage = userInput.nextLine();
	           	 out.writeObject(nomImage);
	           	 BufferedImage image = ImageIO.read(new File(nomImage));
	           	 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	           	 ImageIO.write(image, "jpg", byteArrayOutputStream);
	           	 byteArrayOutputStream.flush();
	           	 int taille = byteArrayOutputStream.size();
	           	 out.writeObject(taille); 	
	           	 // Gestion cas d erreur dans l extraction de l information relative a la taille	           	
	           	 try {
					 String tailleExtraite = in.readObject().toString();
				 } catch (ClassNotFoundException e) {
					 e.printStackTrace();
				 }
	           	 byte tableauImage[] = byteArrayOutputStream.toByteArray();
	           	 out.writeObject(tableauImage);
	           	 System.out.println("Bravo: votre image a bel et bien ete envoyee");
	             // Gestion cas d erreur dans l extraction de l information relative a l image
	           	 try {
					 String imageExtraite = in.readObject().toString();
				 } catch (ClassNotFoundException e) {
					 e.printStackTrace();
				 }
	           	 String tailleImage = null;
				 try {
				 	 tailleImage = in.readObject().toString();
				 } catch (ClassNotFoundException e) {
				 	 e.printStackTrace();
				 }
	           	 out.writeObject("Merci: la taille de l image a ete bien recue");
	           	 int sobelSize = Integer.parseInt(tailleImage);
	           	 byte[] tableauSobel = new byte[sobelSize];
	           	 in.readFully(tableauSobel, 0, sobelSize);
				 ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tableauSobel);
				 BufferedImage imageTransformeeSobel = ImageIO.read(byteArrayInputStream);
				 System.out.println("Merci: L image a ete bien recue");
				 System.out.println("Sous quel nom (SANS L EXTENSION SVP) souhaiteriez vous que l image traitee soit enregistree? : ");
				 String nomImageTraiteeParSobel = userInput.nextLine();
				 //userInput.close();
				 ImageIO.write(imageTransformeeSobel, "jpg", new File(nomImageTraiteeParSobel + ".jpg"));
				 System.out.println("Bien vouloir recuperer votre image traitee dans le meme dossier que l image originale "); 
				 System.out.println("Rappel: le nom de l image traitee est : " + nomImageTraiteeParSobel + ".jpg");
				 out.writeObject("Fin des operations: Merci");
				 //out.close();
            } else {
           	 System.out.println("Un probleme est survenu en combinant le username et votre password !");
            }
            
	        socket.close();

		}catch (UnknownHostException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

}
