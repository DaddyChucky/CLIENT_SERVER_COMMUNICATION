/*
 * INF3405 :: TP1 :: PROJET EN RÉSEAUX INFORMATIQUES :: GESTIONNAIRE DE FICHIER
 * PAR 	GENEVIÈVE PELLETIER-MC DUFF (2085742)
 * 		IMANE ZNADI 				(2065443)
 * 		CHARLES DE LAFONTAINE		(2076524)
 * CRÉÉ LE: 				19/05/2021
 * POUR LE:					08/06/2021
 * DERNIÈRE MODIFICATION: 	08/05/2021
 * 
 * DESCRIPTION: Un serveur qui communique avec un ou plusieurs clients qui permet la création et la recherche de dossiers,
 * 				le parcours des répertoires sur le serveur et le téléchargement (upload) d'un fichier.
 * Server.java
 */


//? Librairies importées
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;


/*
 * Serveur qui communique avec un ou plusieurs clients et qui permet et traite les commandes ls, cd, mkdir, upload & download.
 */
public class Server 
{
    private static ServerSocket auditeur;
    
    /*
     * Vérification de l'adresse IP serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entrées de l'utilisateur pour la récupération de l'adresse IP du serveur.
     * @return 	[String]							L'adresse IP valide du serveur.
     */
    public static String verificationIP(Scanner lignesConsole)
    {
    	final char 	SEPARATEUR_OCTET			= '.';
    	final int 	LONGUEUR_MINIMALE_IP 		= 7;
    	final int 	LONGUEUR_MAXIMALE_IP 		= 15;
    	final int 	NOMBRE_POINTS_MAXIMAL		= 3;
    	final int 	LONGUEUR_MINIMALE_TAMPON 	= 1;
    	final int 	LONGUEUR_MAXIMALE_TAMPON 	= 3;
    	final int	VALEUR_OCTET_MAXIMALE		= 255;
    	final int 	TAMPON_VIDE					= 0;
    	
    	String 		adresseIPServeur 			= "";
		int 		nbPoints	 				= 0;
		int 		nbCaracteres 				= 0;
		boolean 	adresseIPValide 			= false;
		String		tamponOctets 				= "";
    	
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
				System.out.println("\nVotre adresse IP ne possède pas une taille raisonnable.\n");
			
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
							System.out.println("\nVotre adresse IP dépasse le nombre de points (.) autorisés.\n");
							break;
						}
						
						//? Est-ce que l'octet est valide (le tampon est en déçà de 255) ?
						if (tamponOctets.length() >= LONGUEUR_MINIMALE_TAMPON && Integer.parseInt(tamponOctets) > VALEUR_OCTET_MAXIMALE)
						{
							System.out.println("\nAu moins un des octets de votre adresse IP est invalide (est inexistant ou dépasse 255).\n");
							break;
						}
						
						//? Le tampon ne peut pas être vide (deux points collés)
						if (tamponOctets.length() == TAMPON_VIDE)
						{
							System.out.println("\nVotre adresse IP contient au moins deux points (..) collés.\n");
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
							System.out.println("\nVotre adresse IP contient au moins un octet invalide.\n");
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
						System.out.println("\nLe caractère entré est invalide (n'est pas un chiffre ou un point (.)).\n");
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
		final int 	PORT_MINIMAL 	= 5000;
		final int 	PORT_MAXIMAL 	= 5050;
		
		boolean 	portValide 		= false;
		int 		portServeur 	= 0;
    	
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
     * Permet d'essayer d'établir une connexion avec un ou plusieurs clients.
     * Inspiré des notes de cours d'Alejandro Quintero (cours #1, p. 73)
     * @throws [IllegalArgumentException] 	Argument invalide lors de l'établissement du socket.
     * @throws [java.net.BindException]		S'il est impossible d'établir la connexion à l'adresse:port serveur voulus.
     */
    public static void main(String[] args) throws Exception 
    {
    	final Scanner	LIGNES_CONSOLE 		= new Scanner(System.in);
		final String 	ADRESSE_IP_SERVEUR 	= verificationIP(LIGNES_CONSOLE);
		final int 		PORT_SERVEUR 		= verificationPort(LIGNES_CONSOLE);
		
		//? Nous n'avons plus besoin d'intéragir avec l'utilisateur du côté serveur pour l'instant
		LIGNES_CONSOLE.close();
		
		int idClient = 0;
		
		System.out.format("\nIP:port du serveur -> %s:%s", ADRESSE_IP_SERVEUR, PORT_SERVEUR);
		System.out.println("\nEssayons d'ouvrir le serveur. . . ");
		
        try 
        {   
            auditeur = new ServerSocket();
            auditeur.setReuseAddress(true);
            
            auditeur.bind(new InetSocketAddress(ADRESSE_IP_SERVEUR, PORT_SERVEUR));

            System.out.format("Serveur instancié à l'adresse:port -> %s:%d%n", ADRESSE_IP_SERVEUR, PORT_SERVEUR);
            
            while (true)
            	new ClientHandler(auditeur.accept(), ++idClient).start();
        }
        
        catch (IllegalArgumentException e)
        {
        	System.out.println("\nArgument invalide dans la saisie de l'adresse IP du serveur (ou du port). Arrêt.\n");
        }
        
        catch (java.net.BindException e)
        {
        	System.out.println("\nImpossible d'instancier le serveur à l'adresse et au port voulus. Arrêt.\n");
        }

        finally
        {
        	auditeur.close();
        }
    }
    
    
    /*
     * Permet au serveur d'établir plusieurs connexions simultanées avec différents clients.
     */
	private static class ClientHandler extends Thread
	{
		private static final String REPERTOIRE_VIDE = "";
	    private Socket 				socket;
	    private int 				numeroClient;
	    
	    
	    /*
	     * Imprime à l'écran le numéro du client et les détails relatifs au socket.
	     */
	    public ClientHandler(Socket socket, int numeroClient)
	    {
	        this.socket 		= socket;
	        this.numeroClient 	= numeroClient;
	        System.out.println("\nConnexion établie avec succès pour le client #" + numeroClient + " (" + socket + ")\n");
	    }
	
	    
	    /*
	     * Permet de retourner le chemin du répertoire courant sans barre oblique.
	     * @param 	[String] 	repertoireCourant	Le répertoire courant.
	     * @return 	[String] 						Le chemin du répertoire courant sans barre oblique.
	     */
		public static String getCheminSansBarreOblique(String repertoireCourant)
		{
			String cheminSansBarreOblique = "";
			
			//? Si le répertoire courant n'est pas vide ("")
			if (!repertoireCourant.equals(REPERTOIRE_VIDE))
				cheminSansBarreOblique = repertoireCourant.substring(1);
		
			else
				cheminSansBarreOblique = repertoireCourant;
			
			return cheminSansBarreOblique;
		}
		
		
	    /*
	     * Code principal qui roule pour tous les clients lorsque la connexion est établie correctement.
	     * @throws 	[IOException] 	S'il y a eu un problème dans l'ouverture ou la fermeture du socket avec le client.
	     */
	    public void run()
	    {
	        try
	        {
	        	final String	SERVEUR_PRET 					= "ready";
	        	final String 	FORMAT_DATE_ET_HEURE			= "yyyy-MM-dd@HH:mm:ss";
	        	final String 	GLOBE 							= "*";
				final String 	CLIENT_PRET_TELEVERSEMENT 		= "upload";
				final String	CLIENT_PRET_TELECHARGEMENT 		= "download";
	        	final int		LONGUEUR_COMMANDE_ET_NOM		= 2;
	        	final int 		COMMANDE_CLIENT_TAILLE_MINIMALE = 0;
	        	final int 		COMMANDE_CLIENT_TAILLE_MAXIMALE = 2;
	        	final int 		INDEX_ELEMENT_NON_TROUVE 		= -1;
	        	
	    		//? Canal entrant qui reçoit les messages envoyés par le serveur
	            DataOutputStream donneesEnvoyees = new DataOutputStream(socket.getOutputStream());
	            donneesEnvoyees.writeUTF("Salutations, client #" + numeroClient + " !\n"
	            		+ "La connexion est bel et bien établie."
	            		+ "\n\nVeuillez choisir une commande entre:\n\n"
	    				+ "- cd <Nom d'un répertoire sur le serveur>\n		Commande permettant à l'utilisateur de se déplacer vers un répertoire enfant ou parent.\n"
	    				+ "- ls\n		Commande permettant d’afficher à l’utilisateur tous les dossiers et fichiers dans le répertoire courant de l’utilisateur au niveau du serveur.\n"
	    				+ "- mkdir <Nom du nouveau dossier>\n		Commande permettant la création d’un dossier au niveau du serveur de stockage.\n"
	    				+ "- upload <Nom du fichier>\n		Commande permettant le téléversement d’un fichier, se trouvant dans le répertoire locale du client, vers le serveur de stockage.\n"
	    				+ "- download <Nom du fichier>\n		Commande permettant le téléchargement d’un fichier, se trouvant dans le répertoire courant de l’utilisateur au niveau du serveur de stockage, vers le répertoire local du client.\n"
	    				+ "- exit\n		Commande permettant au client de se déconnecter du serveur de stockage.\n");
	      
	            DataInputStream donneesRecues 				= new DataInputStream(socket.getInputStream());
	        	String			repertoireParent 			= "..";
	        	String			cheminSansBarreOblique 		= "";
	        	String			repertoireCourant 			= "";
	        	String			reponseClient;
	        	String[] 		commandesClientSeparees;

	    		while (true)
	    		{
	    			donneesEnvoyees.writeUTF("\nVotre commande: ");
	    			donneesEnvoyees.flush();
	    			
	    			//? Notifier le client que le serveur est prêt à recevoir la commande
	    			donneesEnvoyees.writeUTF(SERVEUR_PRET);
	    			
	    			reponseClient = donneesRecues.readUTF();
	    			
	    			//? Nous envoyons les commandes des clients via la console
	    			System.out.format("\n\n[%s - %s] : %s", socket.getRemoteSocketAddress().toString().substring(1), new SimpleDateFormat(FORMAT_DATE_ET_HEURE).format(Calendar.getInstance().getTime()), reponseClient);
	    			
	    			//? Nous séparons la catégorie de la commande et la précision du nom pour les commandes qui en requièrent
	    			commandesClientSeparees = reponseClient.split(" ");
	    			
	    			//? La commande doit être de longueur 1 ou 2 Strings
	    			if (commandesClientSeparees.length > COMMANDE_CLIENT_TAILLE_MINIMALE && !(commandesClientSeparees.length > COMMANDE_CLIENT_TAILLE_MAXIMALE))
	    			{
	    				//? Nous possédons une commande de longueur valide
	    				switch (commandesClientSeparees[0]) 
	    				{
	    				case "cd":
	    					//? Nous nous assurons que la commande <cd> possède un nom de répertoire
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
	    						//? Si le nom du répertoire correspond à <..>, nous reculons d'un répertoire
	    						if (commandesClientSeparees[1].equals(repertoireParent))
	    						{
	    							//? Nous regardons si nous sommes à la racine du serveur
	    							int positionDerniereOccurenceBarreOblique = repertoireCourant.lastIndexOf(File.separator);
	    							
	    							if (positionDerniereOccurenceBarreOblique != INDEX_ELEMENT_NON_TROUVE)
	    							{
	    								//? Si nous ne sommes pas à la racine, nous reculons d'un répertoire
	    								repertoireCourant = repertoireCourant.substring(0, positionDerniereOccurenceBarreOblique);
	    								
	    								if (repertoireCourant.equals(REPERTOIRE_VIDE))
	    									donneesEnvoyees.writeUTF(String.format("\nVous vous situez à la racine du serveur.\n", repertoireCourant));
	    								
	    								else
	    									donneesEnvoyees.writeUTF(String.format("\nRépertoire courant: <%s>\n", repertoireCourant));
	    							}
	    								
	    							else
	    								donneesEnvoyees.writeUTF("\nVous êtes déjà à la racine du serveur.\n");
	    						}
	    						
	    						else
	    						{
	    							//? Nous devons concaténer un répertoire
	    							boolean dossierPresent = false;
	    							
	    							cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
	    							
	    	    					try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(cheminSansBarreOblique), GLOBE))
	    	    					{
	    	    					    for (Path fichier : stream) 
	    	    					    	//? Nous vérifions que le fichier est un dossier et qu'il correspond à la commande du client
	    	    					    	if (fichier.getFileName().toString().equals(commandesClientSeparees[1]) && Files.isDirectory(fichier))
	    	    					    	{
	    	    					    		dossierPresent = true;
	    	    					    		break;
	    	    					    	}
	    	    					}
	    	    					
	    	    					finally
	    	    					{
	    	    						if (!dossierPresent)
	    	    							//? Le fichier demandé n'est pas présent dans le répertoire courant
	    	    							donneesEnvoyees.writeUTF(String.format("\nLe dossier <%s> n'est pas présent au sein du répertoire <%s>.\n", commandesClientSeparees[1], repertoireCourant));
	    	    						
	    	    						else
	    	    						{
	    	    							//? Le fichier demandé existe, concaténation
	    	    							repertoireCourant += (File.separator + commandesClientSeparees[1]);
	    	    							donneesEnvoyees.writeUTF(String.format("\nRépertoire courant: <%s>\n", repertoireCourant));
	    	    						}
	    	    					}
	    						}
	    					}
	    					
	    					else
	    						donneesEnvoyees.writeUTF("\nCommande <cd> prend obligatoirement un chemin.\n");
	    					
	    					break;
	    				
	    				case "ls": //? Inspiré de https://stackoverflow.com/questions/15598657/how-to-use-ls-c-command-in-java
	    					donneesEnvoyees.writeUTF("\nFichiers présents au sein du répertoire:\n");
	    					
	    					cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
	    					
	    					//? Nous parcourons tous les fichiers présents au sein du répertoire courant
	    					try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(cheminSansBarreOblique), GLOBE))
	    					{
	    					    for (Path fichier : stream) 
	    					    {
	    					    	//? Si le fichier est un dossier
	    					    	if (fichier.toFile().isDirectory()) 
	    					    		donneesEnvoyees.writeUTF(String.format("[Folder] %s", fichier.toString()));
	    					    	
	    					    	//? Pour les autres fichiers
	    					    	else
	    					    		donneesEnvoyees.writeUTF(String.format("[File] %s", fichier.toString()));
	    					    }
	    					}

	    					break;
	    					
	    				case "mkdir":
	    					boolean commandeValide = true;
	    					
	    					//? mkdir prend obligatoirement un nom de dossier
	    					if (commandesClientSeparees.length != LONGUEUR_COMMANDE_ET_NOM) 
	    					{
	    						donneesEnvoyees.writeUTF("\nCommande <mkdir> invalide; nom du fichier manquant.\n");
	    						break;
	    					}
	    					
	    					else
	    					{
	    						//? Vérifions que nous n'avons que des chiffres, des lettres, des tirets (- et _)
	    						for (char caractere : commandesClientSeparees[1].toCharArray())
	    						{
	    							//? Nous acceptons les lettres (maj, min), les chiffres (0-9), de même que '-' et '_'
	    							if (!Character.isLetterOrDigit(caractere) && !(Character.compare(caractere, '-') == 0) && !(Character.compare(caractere, '_') == 0))
	    							{
	    								donneesEnvoyees.writeUTF("\nCommande <mkdir> invalide; caractère invalide dans la saisie du nom du dossier à créer.\n");
	    								commandeValide = false;
	    								break;
	    							}	
	    						}
	    					}
	    					
	    					if (commandeValide)
	    					{
	    						cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
		    				
	    						File dossier;
	    						
	    						//? Nous créons le dossier avec le bon chemin
    							if (!cheminSansBarreOblique.equals(REPERTOIRE_VIDE))
    								dossier = new File(cheminSansBarreOblique + File.separator + commandesClientSeparees[1]);
    							
    							else
    								dossier = new File(commandesClientSeparees[1]);
	    						
    							//? Nous nous assurons que le dossier n'existe pas déjà
	    						if (!dossier.exists()) 
	    						{
	    							dossier.mkdir();
	    							donneesEnvoyees.writeUTF(String.format("\nDossier <%s> créé avec succès au sein du répertoire.\n", commandesClientSeparees[1]));
	    						}
	    						
	    						else
	    							donneesEnvoyees.writeUTF(String.format("\nVotre dossier <%s> est déjà présent au sein du répertoire.\n", commandesClientSeparees[1]));
	    					}
	    					
	    					break;
	    					
	    				case "upload":
	    					//? upload requiert obligatoirement un nom de téléversement
	    					
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
		    					//? On informe le client que le serveur est prêt à téléverser son fichier
		    					donneesEnvoyees.writeUTF(CLIENT_PRET_TELEVERSEMENT);
		    					
		    					//? On envoie le nom du fichier traité au client pour qu'il puisse vérifier de son côté qu'il existe au sein de son répertoire local
		    					donneesEnvoyees.writeUTF(commandesClientSeparees[1]);
		    					
		    					//? On vérifie que le client téléverse un fichier qui existe
		    					final String VERIFICATION_FICHIER_CLIENT = donneesRecues.readUTF();
		    					
		    					if (SERVEUR_PRET.equals(VERIFICATION_FICHIER_CLIENT))
		    					{
		    						//? On reçoit la taille du fichier du client
		    						final int 	TAILLE_FICHIER 	= Integer.parseInt(donneesRecues.readUTF());
		    						
		    						//? Nous créons notre tampon d'octets
			    					byte[] 		octetsRecus 	= new byte[TAILLE_FICHIER];
			    					int 		octetsLus 		= 0;
			    					
			    					//? Nous lisons les octets reçus
			    					while (octetsLus < TAILLE_FICHIER)
			    						octetsLus += donneesRecues.read(octetsRecus, octetsLus, TAILLE_FICHIER - octetsLus);
			    					
			    					cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
			    					
			    					//? Nous écrivons dans le fichier à partir des octets reçus
			    					FileOutputStream fos;
			    					
	    							if (!cheminSansBarreOblique.equals(REPERTOIRE_VIDE))
	    								fos = new FileOutputStream(cheminSansBarreOblique + File.separator + commandesClientSeparees[1]);
	    							
	    							else
	    								fos = new FileOutputStream(commandesClientSeparees[1]);

			    					fos.write(octetsRecus);
			    					fos.close();
			    					
			    					donneesEnvoyees.writeUTF(String.format("\nLe fichier <%s> a bien été téléversé.\n", commandesClientSeparees[1]));
		    					}
	    					}
	    					
	    					else
	    						donneesEnvoyees.writeUTF("\nLa commande <upload> prend obligatoirement un nom de fichier.\n");
		    				
	    					break;
	    				
	    				case "download":
	    					//? upload requiert obligatoirement un nom de téléchargement
	    					
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
	    						//? Nous vérifions que le fichier demandé existe
								boolean fichierPresent = false;
								
								cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
								
		    					try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(cheminSansBarreOblique), GLOBE))
		    					{
		    					    for (Path fichier : stream) 
		    					    	//? Nous vérifions que le fichier n'est pas un dossier et que nous avons les permissions nécessaires pour le lire
		    					    	if (fichier.getFileName().toString().equals(commandesClientSeparees[1]) && !Files.isDirectory(fichier) && Files.isReadable(fichier))
		    					    	{
		    					    		fichierPresent = true;
		    					    		break;
		    					    	}
		    					}
		    					
		    					finally
		    					{
		    						//? Si le fichier existe
		    						if (fichierPresent)
		    						{
		    							//? Le client est fin prêt au téléchargement
		    							donneesEnvoyees.writeUTF(CLIENT_PRET_TELECHARGEMENT);
		    							
		    							//? Nous envoyons le nom du fichier à créer
		    							donneesEnvoyees.writeUTF(commandesClientSeparees[1]);
		    						
		    							//? Nous débutons l'envoi des données du fichier, source: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
		    							File fichierAEnvoyer;
		    							Path chemin;
		    							
		    							if (cheminSansBarreOblique.equals(REPERTOIRE_VIDE))
		    							{
		    								fichierAEnvoyer = new File(commandesClientSeparees[1]);
		    								chemin 			= Paths.get(commandesClientSeparees[1]);
		    							}
		    							
		    							else
		    							{
		    								fichierAEnvoyer = new File(cheminSansBarreOblique 	+ File.separator + commandesClientSeparees[1]);
		    								chemin 			= Paths.get(cheminSansBarreOblique 	+ File.separator + commandesClientSeparees[1]);
		    							}				
		    							
		    							byte[] 	octetsFichier = new byte[(int)fichierAEnvoyer.length()];
		    						
		    							System.out.println(repertoireCourant);
		    							
		    							//? Nous envoyons la taille du fichier à créer
		    							donneesEnvoyees.writeUTF(Long.toString(fichierAEnvoyer.length()));
		    					
		    							//? Nous remplissons notre tampon des octets du fichier à transmettre
	    								octetsFichier = Files.readAllBytes(chemin);
		    							
		    							//? Nous envoyons les octets au client
		    							donneesEnvoyees.write(octetsFichier, 0, octetsFichier.length);
		    						}
		    						
		    						else
		    						{
		    							donneesEnvoyees.writeUTF(String.format("\nVotre fichier demandé <%s> n'existe pas au sein du répertoire serveur <%s> (ou n'est pas un fichier téléchargeable).\n", commandesClientSeparees[1], repertoireCourant));
		    						}
		    					}
	    					}
	    					
	    					else
	    					{
	    						donneesEnvoyees.writeUTF("\nLa commande <download> prend obligatoirement un nom de fichier.\n");
	    					}
	    					
	    					break;
	    				
	    				case "exit":
	    					//? Nous essayons de terminer la connexion serveur-client
	    		            try
	    		            {
	    		                socket.close();
	    		                System.out.println("\nConnexion avec le client #" + numeroClient + " fermée avec succès.\n");
	    		            } 
	    		            
	    		            catch (IOException e)
	    		            {
	    		                System.out.println("\nERREUR ! Nous n'avons pas pu fermer la connexion avec le client #" + numeroClient + "\n");
	    		            }
	    		            
		    				return;
	    		           
	    					
	    				default:
	    					donneesEnvoyees.writeUTF("\nVotre commande n'existe pas. Veuillez vous référer à la liste de commande citée plus haut.");
	    				}
	    			}
	    			
	    			else 
	    				donneesEnvoyees.writeUTF("\nVotre commande est de longueur invalide. Veuillez vous référer à la liste de commande citée plus haut.");
	    		}
	        } 
	        
	        catch (IOException e)
	        {
	            System.out.println("\nERREUR ! Impossible d'établir la connexion avec le client #" + numeroClient + ". (" + e + ")");
	        }	        
	    }
	}
}
