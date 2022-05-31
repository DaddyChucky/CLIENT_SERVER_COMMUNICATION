/*
 * INF3405 :: TP1 :: PROJET EN R�SEAUX INFORMATIQUES :: GESTIONNAIRE DE FICHIER
 * PAR 	GENEVI�VE PELLETIER-MC DUFF (2085742)
 * 		IMANE ZNADI 				(2065443)
 * 		CHARLES DE LAFONTAINE		(2076524)
 * CR�� LE: 				19/05/2021
 * POUR LE:					08/06/2021
 * DERNI�RE MODIFICATION: 	08/05/2021
 * 
 * DESCRIPTION: Un client qui communique avec le serveur et qui peut parcourir un r�pertoire sur le serveur distant,
 * 				afficher tous les dossiers et fichiers, cr�er un dossier et t�l�verser un fichier sur le serveur.
 * Client.java
 */


//? Librairies import�es
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileOutputStream;

public class Client {

	private static Socket socket;
	
	
	/*
     * V�rification de l'adresse IP serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entr�es de l'utilisateur pour la r�cup�ration de l'adresse IP du serveur.
     * @return 	[String]							L'adresse IP valide du serveur.
     */
    public static String verificationIP(Scanner lignesConsole)
    {
    	final char 		SEPARATEUR_OCTET			= '.';
    	final int 		LONGUEUR_MINIMALE_IP 		= 7;
    	final int 		LONGUEUR_MAXIMALE_IP 		= 15;
    	final int 		NOMBRE_POINTS_MAXIMAL		= 3;
    	final int 		LONGUEUR_MINIMALE_TAMPON 	= 1;
    	final int 		LONGUEUR_MAXIMALE_TAMPON 	= 3;
    	final int		VALEUR_OCTET_MAXIMALE		= 255;
    	final int 		TAMPON_VIDE					= 0;

    	String 			adresseIPServeur 			= "";
		int 			nbPoints	 				= 0;
		int 			nbCaracteres 				= 0;
		boolean	 		adresseIPValide				= false;
		String			tamponOctets 				= "";
	
    	//? Tant que l'utilisateur entre une adresse IP invalide
		while (true)
		{
			nbPoints	 	= 0;
			nbCaracteres 	= 0;
			adresseIPValide = false;
			tamponOctets 	= "";
			
			//? On demande l'adresse IP du serveur
			System.out.println("Veuillez entrer l'adresse IP du serveur: ");
			adresseIPServeur = lignesConsole.nextLine();
			
			//? Est-ce que l'adresse IP d�passe le nombre de caract�res utilis�s ?
			if (adresseIPServeur.length() < LONGUEUR_MINIMALE_IP || adresseIPServeur.length() > LONGUEUR_MAXIMALE_IP) 
				System.out.println("Votre adresse IP ne poss�de pas une taille raisonnable.");

			//? L'adresse ne doit pas terminer par un point
			else if (adresseIPServeur.charAt(adresseIPServeur.length() - 1) == SEPARATEUR_OCTET)
				System.out.println("\nVotre adresse IP ne peut pas se terminer par un octet vide.\n");
			
			else
			{
				//? On v�rifie si l'adresse IP est valide
				for (char caractere : adresseIPServeur.toCharArray())
				{		
					++nbCaracteres;
					
					//? Compter que le nombre de points est �gal � 3 (donc 4 octets envoy�s) et que le nombre de caract�res maximal est 3 * 4 + 3 = 15
					if (caractere == SEPARATEUR_OCTET)
					{
						++nbPoints;
						
						//? Est-ce que l'adresse IP d�passe le nombre de points utilis�s ?
						if (nbPoints > NOMBRE_POINTS_MAXIMAL)
						{
							System.out.println("Votre adresse IP d�passe le nombre de points (.) autoris�s.");
							break;
						}
						
						//? Est-ce que l'octet est valide (le tampon est en d��� de 255) ?
						if (tamponOctets.length() >= LONGUEUR_MINIMALE_TAMPON && Integer.parseInt(tamponOctets) > VALEUR_OCTET_MAXIMALE)
						{
							System.out.println("Au moins un des octets de votre adresse IP est invalide (est inexistant ou d�passe 255).");
							break;
						}
						
						//? Le tampon ne peut pas �tre vide (deux points coll�s)
						if (tamponOctets.length() == TAMPON_VIDE)
						{
							System.out.println("Votre adresse IP contient au moins deux points (..) coll�s.");
							break;
						}
						
						else
							tamponOctets = "";
					}
					
					else if (Character.isDigit(caractere))
					{
						//? On ajoute le caract�re au tampon
						tamponOctets += caractere;
						
						//? Nous avons un caract�re valide, v�rifions que l'octet est lui aussi valide (pas trop d'octets)
						if (tamponOctets.length() > LONGUEUR_MAXIMALE_TAMPON)
						{
							System.out.println("Votre adresse IP contient au moins un octet invalide.");
							break;
						}
						
						//? Sommes-nous rendus � la fin de l'adresse IP ?
						//? Elle a su r�sister � tous les tests, donc elle est valide
						if (nbCaracteres == adresseIPServeur.length())
						{
							adresseIPValide = true;
							break;
						}
					}
					
					else
					{
						System.out.println("Le caract�re entr� est invalide (n'est pas un chiffre ou un point (.)).");
						break;
					}
				}
			}
			
			//? Nous arr�tons la boucle infinie si l'adresse IP est valide (condition d'arr�t)
			if (adresseIPValide)
				break;
		}
		
		return adresseIPServeur;
    }
    
	
    /*
     * V�rification du port serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entr�es de l'utilisateur pour la r�cup�ration du port du serveur.
     * @return 	[int]								Le port du serveur.
     * @throws	[NumberFormatException]				Si le port serveur n'est pas convertible en nombre entier.
     */
    public static int verificationPort(Scanner lignesConsole)
    {
		final int 		PORT_MINIMAL 				= 5000;
		final int 		PORT_MAXIMAL 				= 5050;
		
		boolean 		portValide 					= false;
		int 			portServeur 				= 0;
		
    	//? Tant que l'utilisateur entre un port invalide
		while (true) 
		{
			System.out.println("\nVeuillez entrer le port du serveur: ");
			
			try 
			{
				//? Est-ce que le port se transforme en un nombre entier ?
				portServeur = Integer.parseInt(lignesConsole.nextLine());
			}
			
			catch (NumberFormatException e)
			{
				System.out.println("\nLe port entr� n'est pas un nombre.");
			}
			
			finally
			{
				//? Est-ce que le port se situe entre 5000 et 5050 ?
				if (portServeur >= PORT_MINIMAL && portServeur <= PORT_MAXIMAL)
					portValide = true;

				else 
					System.out.println("\nLe port ne se situe pas entre 5000 et 5050.");
			}
			
			if (portValide)
				break;
		}
		
		return portServeur;
    }
    
    
    /*
     * Permet d'envoyer les commandes du client au serveur pour traitement.
     * Inspir� des notes de cours d'Alejandro Quintero (cours #1, p. 73)
     * @throws	[java.net.ConnectException]		S'il est impossible d'�tablir la connexion � l'adresse:port serveur voulus.
     */
	public static void main(String[] args) throws Exception
	{
		final Scanner 	LIGNES_CONSOLE 				= new Scanner(System.in);
		final String	CLIENT_PRET 				= "ready";
		final String	CLIENT_TELECHARGEMENT 		= "download";
		final String	CLIENT_TELEVERSEMENT 		= "upload";
		final String	FERMETURE_APPLICATION 		= "exit";
		final String 	REPERTOIRE_COURANT			= "";
		final String	CLIENT_PAS_PRET 			= "not_ready";
		final String 	ADRESSE_IP_SERVEUR 			= verificationIP(LIGNES_CONSOLE);
		final int 		PORT_SERVEUR 				= verificationPort(LIGNES_CONSOLE);
		

		
		System.out.format("\nIP:port du serveur -> %s:%s", ADRESSE_IP_SERVEUR, PORT_SERVEUR);
		System.out.println("\nEssayons d'�tablir la connexion. . . ");
		
		try 
		{
			//? Cr�ation d'un socket, d'une nouvelle connexion avec le serveur
			socket = new Socket(ADRESSE_IP_SERVEUR, PORT_SERVEUR);
			System.out.println(socket.getRemoteSocketAddress().toString());
		}

		catch (java.net.ConnectException e)
		{
			System.out.println("La connexion au serveur n'a pas pu �tre �tablie. Arr�t.");
			LIGNES_CONSOLE.close();
			return;
		}
		
		DataInputStream 	donneesRecues 	= new DataInputStream(socket.getInputStream());
		DataOutputStream 	donneesEnvoyees = new DataOutputStream(socket.getOutputStream());
		String 				commande 		= "";
		String				reponse 		= "";
		
		while (true)
		{
			//? Attente de la r�ceptivit� de la connexion 
			reponse = donneesRecues.readUTF();
			
			//? Si le serveur est en mesure de recevoir la commande du client
			if (reponse.equals(CLIENT_PRET)) 
			{
				//? L'utilisateur envoie sa commande au serveur
				commande = LIGNES_CONSOLE.nextLine();
				donneesEnvoyees.writeUTF(commande);

				//? L'application se termine si l'utilisateur entre "exit"
				if (commande.split(" ")[0].equals(FERMETURE_APPLICATION))
					break;
			}

			//? Le client souhaite t�l�charger un fichier
			else if (reponse.equals(CLIENT_TELECHARGEMENT)) // Inspir� de : https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
			{
				//? Nous recevons le nom du fichier t�l�charg�
				final String 	NOM_FICHIER 	= donneesRecues.readUTF();
				
				//? Nous recevons la taille du fichier t�l�charg�
				final int 		TAILLE_FICHIER 	= Integer.parseInt(donneesRecues.readUTF());
				byte[] 			octetsRecus 	= new byte[TAILLE_FICHIER];
				int 			octetsLus 		= 0;
				
				//? Tant que nous n'avons pas lu l'enti�ret� du fichier t�l�charg�
				while (octetsLus < TAILLE_FICHIER)
				{
					//? Nous lisons le fichier provenant du serveur
					octetsLus += donneesRecues.read(octetsRecus, octetsLus, TAILLE_FICHIER - octetsLus);
				}
				
				System.out.format("\nLe fichier <%s> a bien �t� t�l�charg�.\n", NOM_FICHIER);
				
				//? Nous �crivons dans le fichier � partir des octets re�us
				FileOutputStream fos = new FileOutputStream(NOM_FICHIER);
				fos.write(octetsRecus);
				fos.close();
			}
			
			//? Le client souhaite t�l�verser un fichier
			else if (reponse.equals(CLIENT_TELEVERSEMENT))
			{
				//? Le client re�oit le nom de son fichier � t�l�verser et v�rifie son existence au sein du r�pertoire
				final String 	NOM_FICHIER_TELEVERSE 	= donneesRecues.readUTF();
				
				File fichierATeleverser = new File(System.getProperty("user.dir") + File.separator + NOM_FICHIER_TELEVERSE);
		    
				if (fichierATeleverser.exists() && fichierATeleverser.isFile() && fichierATeleverser.canRead())
		    	{
					//? Nous envoyons au serveur un message comme quoi nous sommes pr�ts � passer au t�l�versement
					donneesEnvoyees.writeUTF(CLIENT_PRET);
					
					//? Nous d�butons l'envoi des donn�es du fichier, source: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
					File 	fichierAEnvoyer 	= new File(REPERTOIRE_COURANT + NOM_FICHIER_TELEVERSE);
					byte[] 	octetsFichier 		= new byte[(int)fichierAEnvoyer.length()];
				
					//? Nous envoyons la taille du fichier � cr�er
					donneesEnvoyees.writeUTF(Long.toString(fichierAEnvoyer.length()));
					
					//? Nous remplissons notre tampon des octets du fichier � transmettre
					Path chemin 	= Paths.get(REPERTOIRE_COURANT + NOM_FICHIER_TELEVERSE);
					octetsFichier 	= Files.readAllBytes(chemin);
					
					//? Nous envoyons les octets au serveur
					donneesEnvoyees.write(octetsFichier, 0, octetsFichier.length);
				}
				
				else
				{
					donneesEnvoyees.writeUTF(CLIENT_PAS_PRET);
					System.out.println(String.format("\nVotre fichier demand� <%s> n'existe pas au sein du r�pertoire client <%s> (ou n'est pas un fichier t�l�chargeable).\n", NOM_FICHIER_TELEVERSE, REPERTOIRE_COURANT));
				}
			}
			
			else
				//? Nous affichons les messages re�us du serveur
				System.out.println(reponse);
		}

		System.out.println("\nVous avez �t� d�connect� avec succ�s. Fin du programme.");
		LIGNES_CONSOLE.close();
	}
}
