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
	
		Scanner userInput = new Scanner(System.in);

		Socket socket = CheckingServer.ValidateSoket();
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		if(!CheckingServer.ValidateUser(out, in)) {
			System.out.println("Combinaison du nom d'utilisateur et mot passe incorrecte, bye");
			socket.close();
			return;
		}

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
			
			
			boolean imageCorrect = false;
			String nomImage = null;
			byte[] imgByte = null;
			do {
				try {
					nomImage = userInput.nextLine();
					File img = new File(nomImage);
					imgByte = Files.readAllBytes(img.toPath());
					imageCorrect = true;
				} catch (Exception e) {
					System.out.println(" Image introuvable, bien vouloir fournir le nom de l image a traiter sous la forme nom_image.jpg");
				}
			} while (!imageCorrect);
			
			out.writeUTF(nomImage);
			out.writeInt(imgByte.length);
			out.write(imgByte, 0, imgByte.length);
			
			
			// Get Image
			int size = in.readInt();
			byte[] imageArray = new byte[size];
			int dataRead = 0;
			int offset = 0;
			int sizeLeft = size;
			int packetSize = 5000;
			while ((dataRead = in.read(imageArray, offset, packetSize)) > 0) {
				sizeLeft -= dataRead;
				offset += dataRead;
				System.out.println(" Reception d'image en cours, " + offset + "/" + size + " packets recus...");
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
			System.out.println("Une erreur est survenue lors du traitement d'image, veuillez re-essayer plus tard, bye");
			socket.close();
		}
	}
	
	

}
