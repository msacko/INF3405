import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class Client {

	/*
	 * Application client
	 */
	public static void main(String[] args) throws Exception {
		Socket socket = null;
		String serverAddress; // i.e "127.0.0.1"
		int port; // i.e 5000
		InetAddress serverAddressObj;

		User user = new User();

		DataOutputStream out = null;
		DataInputStream in = null;

		Scanner userInput = new Scanner(System.in);
		boolean serverValid = false;
		boolean userValid = false;

		/*
		 * ===================================================================
		 * Informations sur le serveur founies par l utilisateur (IP address, port)
		 * ===================================================================
		 */
		do {

			while (true) {
				System.out.println("Fournissez svp l addresse IP du serveur");
				serverAddress = userInput.nextLine();

				try {
					serverAddressObj = InetAddress.getByName(serverAddress);
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

			try {
				socket = new Socket(serverAddressObj, port);
				//out = new ObjectOutputStream(socket.getOutputStream());
				out = new DataOutputStream(socket.getOutputStream());
				serverValid = true;
			} catch (IOException e) {
				userInput.nextLine();
				System.out.println("Serveur introuvable");
			}
		} while (!serverValid);

		/*
		 * ========================================================== INFORMATIONS SUR
		 * L'UTILISATEUR (username, et mot de passe)
		 * ==========================================================
		 */

		do {
			userInput.nextLine();
			System.out.println("Svp, bien vouloir fournir votre nom d utilisateur :");
			user.username = userInput.next();
			System.out.println("Svp, bien vouloir fournir votre mot de passe :");
			user.password = userInput.next();

			out.writeUTF(user.username + ":" + user.password);
			in = new DataInputStream(socket.getInputStream());

			userValid = in.readBoolean();

			if (!userValid) {
				userInput.nextLine();
				System.out.println("Combinaison du nom d'utilisateur et mot passe incorrecte");
			}

		} while (!userValid);

		/*
		 * ========================================================= CONNEXION -
		 * ECHANGES D'INFORMATIONS NOTAMMENT SUR L'IMAGE
		 * =========================================================
		 */
		try {
			System.out.println(
					"============================================================================================");
			System.out.println(
					"Tout est beau. On peut démarrer la phase des échanges d'information sur les images à traiter");
			System.out.println(
					"============================================================================================");
			System.out.println("Svp, bien vouloir fournir le nom de l image a traiter sous la forme nom_image.jpg) : ");
			
			//Envoi du nom de l'image
			String nomImage = userInput.nextLine();
			out.writeUTF(nomImage);
			
			
			//Envoi de la taille
			File img = new File(nomImage);
			byte[] imgByte = Files.readAllBytes(img.toPath());
			out.writeInt(imgByte.length);
			
			//Envoi de l'image sous forme de byte
			out.write(imgByte, 0, imgByte.length);
			
			
			// Recuper l'image modifié
			int size = in.readInt();
			byte[] imageArray = new byte[size];
			int nRead = 0;
			int off = 0;
			int sizeLeft = size;
			int packetSize = 5000;
			while ((nRead = in.read(imageArray, off, packetSize)) > 0) {
				sizeLeft -= nRead;
				off += nRead;
				System.out.println(nRead + " bytes Recieved....");
				if(sizeLeft <= 5000) {
					packetSize = sizeLeft;
				}
			}
			
			System.out.println("Votre image a ete traitee avec succes, veuillez donner un nom pour l'image");
			String newImageName = userInput.nextLine();
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageArray);
			BufferedImage imageTransformeeSobel = ImageIO.read(byteArrayInputStream);
			
			String extension = "jpg";
			ImageIO.write(imageTransformeeSobel, extension, new File(newImageName + "." + extension));
			System.out.println("Bien vouloir recuperer votre image traitee dans le meme dossier que l image originale ");
			System.out.println("Rappel: le nom de l image traitee est : " + newImageName + "." + extension);
			
			
			socket.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
