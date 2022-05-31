/*
 * INF3405 :: TP1 :: PROJET EN RÉSEAUX INFORMATIQUES :: GESTIONNAIRE DE FICHIER
 * PAR 	GENEVIÈVE PELLETIER-MC DUFF (2085742)
 * 		IMANE ZNADI 				(2065443)
 * 		CHARLES DE LAFONTAINE		(2076524)
 * CRÉÉ LE: 				19/05/2021
 * POUR LE:					08/06/2021
 * DERNIÈRE MODIFICATION: 	08/05/2021
 * 
 * DESCRIPTION: Un client qui communique avec le serveur et qui peut parcourir un répertoire sur le serveur distant,
 * 				afficher tous les dossiers et fichiers, créer un dossier et téléverser un fichier sur le serveur.
 * Client.java
 */


//? Librairies importées
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
     * Vérification de l'adresse IP serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entrées de l'utilisateur pour la récupération de l'adresse IP du serveur.
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
			
			//? Est-ce que l'adresse IP dépasse le nombre de caractères utilisés ?
			if (adresseIPServeur.length() < LONGUEUR_MINIMALE_IP || adresseIPServeur.length() > LONGUEUR_MAXIMALE_IP) 
				System.out.println("Votre adresse IP ne possède pas une taille raisonnable.");

			//? L'adresse ne doit pas terminer par un point
			else if (adresseIPServeur.charAt(adresseIPServeur.length() - 1) == SEPARATEUR_OCTET)
				System.out.println("\nVotre adresse IP ne peut pas se terminer par un octet vide.\n");
			
			else
			{
				//? On vérifie si l'adresse IP est valide
				for (char caractere : adresseIPServeur.toCharArray())
				{		
					++nbCaracteres;
					
					//? Compter que le nombre de points est égal à 3 (donc 4 octets envoyés) et que le nombre de caractères maximal est 3 * 4 + 3 = 15
					if (caractere == SEPARATEUR_OCTET)
					{
						++nbPoints;
						
						//? Est-ce que l'adresse IP dépasse le nombre de points utilisés ?
						if (nbPoints > NOMBRE_POINTS_MAXIMAL)
						{
							System.out.println("Votre adresse IP dépasse le nombre de points (.) autorisés.");
							break;
						}
						
						//? Est-ce que l'octet est valide (le tampon est en déçà de 255) ?
						if (tamponOctets.length() >= LONGUEUR_MINIMALE_TAMPON && Integer.parseInt(tamponOctets) > VALEUR_OCTET_MAXIMALE)
						{
							System.out.println("Au moins un des octets de votre adresse IP est invalide (est inexistant ou dépasse 255).");
							break;
						}
						
						//? Le tampon ne peut pas être vide (deux points collés)
						if (tamponOctets.length() == TAMPON_VIDE)
						{
							System.out.println("Votre adresse IP contient au moins deux points (..) collés.");
							break;
						}
						
						else
							tamponOctets = "";
					}
					
					else if (Character.isDigit(caractere))
					{
						//? On ajoute le caractère au tampon
						tamponOctets += caractere;
						
						//? Nous avons un caractère valide, vérifions que l'octet est lui aussi valide (pas trop d'octets)
						if (tamponOctets.length() > LONGUEUR_MAXIMALE_TAMPON)
						{
							System.out.println("Votre adresse IP contient au moins un octet invalide.");
							break;
						}
						
						//? Sommes-nous rendus à la fin de l'adresse IP ?
						//? Elle a su résister à tous les tests, donc elle est valide
						if (nbCaracteres == adresseIPServeur.length())
						{
							adresseIPValide = true;
							break;
						}
					}
					
					else
					{
						System.out.println("Le caractère entré est invalide (n'est pas un chiffre ou un point (.)).");
						break;
					}
				}
			}
			
			//? Nous arrêtons la boucle infinie si l'adresse IP est valide (condition d'arrêt)
			if (adresseIPValide)
				break;
		}
		
		return adresseIPServeur;
    }
    
	
    /*
     * Vérification du port serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entrées de l'utilisateur pour la récupération du port du serveur.
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
				System.out.println("\nLe port entré n'est pas un nombre.");
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
     * Inspiré des notes de cours d'Alejandro Quintero (cours #1, p. 73)
     * @throws	[java.net.ConnectException]		S'il est impossible d'établir la connexion à l'adresse:port serveur voulus.
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
		System.out.println("\nEssayons d'établir la connexion. . . ");
		
		try 
		{
			//? Création d'un socket, d'une nouvelle connexion avec le serveur
			socket = new Socket(ADRESSE_IP_SERVEUR, PORT_SERVEUR);
			System.out.println(socket.getRemoteSocketAddress().toString());
		}

		catch (java.net.ConnectException e)
		{
			System.out.println("La connexion au serveur n'a pas pu être établie. Arrêt.");
			LIGNES_CONSOLE.close();
			return;
		}
		
		DataInputStream 	donneesRecues 	= new DataInputStream(socket.getInputStream());
		DataOutputStream 	donneesEnvoyees = new DataOutputStream(socket.getOutputStream());
		String 				commande 		= "";
		String				reponse 		= "";
		
		while (true)
		{
			//? Attente de la réceptivité de la connexion 
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

			//? Le client souhaite télécharger un fichier
			else if (reponse.equals(CLIENT_TELECHARGEMENT)) // Inspiré de : https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
			{
				//? Nous recevons le nom du fichier téléchargé
				final String 	NOM_FICHIER 	= donneesRecues.readUTF();
				
				//? Nous recevons la taille du fichier téléchargé
				final int 		TAILLE_FICHIER 	= Integer.parseInt(donneesRecues.readUTF());
				byte[] 			octetsRecus 	= new byte[TAILLE_FICHIER];
				int 			octetsLus 		= 0;
				
				//? Tant que nous n'avons pas lu l'entièreté du fichier téléchargé
				while (octetsLus < TAILLE_FICHIER)
				{
					//? Nous lisons le fichier provenant du serveur
					octetsLus += donneesRecues.read(octetsRecus, octetsLus, TAILLE_FICHIER - octetsLus);
				}
				
				System.out.format("\nLe fichier <%s> a bien été téléchargé.\n", NOM_FICHIER);
				
				//? Nous écrivons dans le fichier à partir des octets reçus
				FileOutputStream fos = new FileOutputStream(NOM_FICHIER);
				fos.write(octetsRecus);
				fos.close();
			}
			
			//? Le client souhaite téléverser un fichier
			else if (reponse.equals(CLIENT_TELEVERSEMENT))
			{
				//? Le client reçoit le nom de son fichier à téléverser et vérifie son existence au sein du répertoire
				final String 	NOM_FICHIER_TELEVERSE 	= donneesRecues.readUTF();
				
				File fichierATeleverser = new File(System.getProperty("user.dir") + File.separator + NOM_FICHIER_TELEVERSE);
		    
				if (fichierATeleverser.exists() && fichierATeleverser.isFile() && fichierATeleverser.canRead())
		    	{
					//? Nous envoyons au serveur un message comme quoi nous sommes prêts à passer au téléversement
					donneesEnvoyees.writeUTF(CLIENT_PRET);
					
					//? Nous débutons l'envoi des données du fichier, source: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
					File 	fichierAEnvoyer 	= new File(REPERTOIRE_COURANT + NOM_FICHIER_TELEVERSE);
					byte[] 	octetsFichier 		= new byte[(int)fichierAEnvoyer.length()];
				
					//? Nous envoyons la taille du fichier à créer
					donneesEnvoyees.writeUTF(Long.toString(fichierAEnvoyer.length()));
					
					//? Nous remplissons notre tampon des octets du fichier à transmettre
					Path chemin 	= Paths.get(REPERTOIRE_COURANT + NOM_FICHIER_TELEVERSE);
					octetsFichier 	= Files.readAllBytes(chemin);
					
					//? Nous envoyons les octets au serveur
					donneesEnvoyees.write(octetsFichier, 0, octetsFichier.length);
				}
				
				else
				{
					donneesEnvoyees.writeUTF(CLIENT_PAS_PRET);
					System.out.println(String.format("\nVotre fichier demandé <%s> n'existe pas au sein du répertoire client <%s> (ou n'est pas un fichier téléchargeable).\n", NOM_FICHIER_TELEVERSE, REPERTOIRE_COURANT));
				}
			}
			
			else
				//? Nous affichons les messages reçus du serveur
				System.out.println(reponse);
		}

		System.out.println("\nVous avez été déconnecté avec succès. Fin du programme.");
		LIGNES_CONSOLE.close();
	}
}
