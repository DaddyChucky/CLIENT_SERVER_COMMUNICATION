/*
 * INF3405 :: TP1 :: PROJET EN R�SEAUX INFORMATIQUES :: GESTIONNAIRE DE FICHIER
 * PAR 	GENEVI�VE PELLETIER-MC DUFF (2085742)
 * 		IMANE ZNADI 				(2065443)
 * 		CHARLES DE LAFONTAINE		(2076524)
 * CR�� LE: 				19/05/2021
 * POUR LE:					08/06/2021
 * DERNI�RE MODIFICATION: 	08/05/2021
 * 
 * DESCRIPTION: Un serveur qui communique avec un ou plusieurs clients qui permet la cr�ation et la recherche de dossiers,
 * 				le parcours des r�pertoires sur le serveur et le t�l�chargement (upload) d'un fichier.
 * Server.java
 */


//? Librairies import�es
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
     * V�rification de l'adresse IP serveur.
     * @param 	[Scanner]		lignesConsole		Permet de prendre les entr�es de l'utilisateur pour la r�cup�ration de l'adresse IP du serveur.
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
			
			//? Est-ce que l'adresse IP d�passe le nombre de caract�res utilis�s ?
			if (adresseIPServeur.length() < LONGUEUR_MINIMALE_IP || adresseIPServeur.length() > LONGUEUR_MAXIMALE_IP) 
				System.out.println("\nVotre adresse IP ne poss�de pas une taille raisonnable.\n");
			
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
							System.out.println("\nVotre adresse IP d�passe le nombre de points (.) autoris�s.\n");
							break;
						}
						
						//? Est-ce que l'octet est valide (le tampon est en d��� de 255) ?
						if (tamponOctets.length() >= LONGUEUR_MINIMALE_TAMPON && Integer.parseInt(tamponOctets) > VALEUR_OCTET_MAXIMALE)
						{
							System.out.println("\nAu moins un des octets de votre adresse IP est invalide (est inexistant ou d�passe 255).\n");
							break;
						}
						
						//? Le tampon ne peut pas �tre vide (deux points coll�s)
						if (tamponOctets.length() == TAMPON_VIDE)
						{
							System.out.println("\nVotre adresse IP contient au moins deux points (..) coll�s.\n");
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
							System.out.println("\nVotre adresse IP contient au moins un octet invalide.\n");
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
						System.out.println("\nLe caract�re entr� est invalide (n'est pas un chiffre ou un point (.)).\n");
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
     * Permet d'essayer d'�tablir une connexion avec un ou plusieurs clients.
     * Inspir� des notes de cours d'Alejandro Quintero (cours #1, p. 73)
     * @throws [IllegalArgumentException] 	Argument invalide lors de l'�tablissement du socket.
     * @throws [java.net.BindException]		S'il est impossible d'�tablir la connexion � l'adresse:port serveur voulus.
     */
    public static void main(String[] args) throws Exception 
    {
    	final Scanner	LIGNES_CONSOLE 		= new Scanner(System.in);
		final String 	ADRESSE_IP_SERVEUR 	= verificationIP(LIGNES_CONSOLE);
		final int 		PORT_SERVEUR 		= verificationPort(LIGNES_CONSOLE);
		
		//? Nous n'avons plus besoin d'int�ragir avec l'utilisateur du c�t� serveur pour l'instant
		LIGNES_CONSOLE.close();
		
		int idClient = 0;
		
		System.out.format("\nIP:port du serveur -> %s:%s", ADRESSE_IP_SERVEUR, PORT_SERVEUR);
		System.out.println("\nEssayons d'ouvrir le serveur. . . ");
		
        try 
        {   
            auditeur = new ServerSocket();
            auditeur.setReuseAddress(true);
            
            auditeur.bind(new InetSocketAddress(ADRESSE_IP_SERVEUR, PORT_SERVEUR));

            System.out.format("Serveur instanci� � l'adresse:port -> %s:%d%n", ADRESSE_IP_SERVEUR, PORT_SERVEUR);
            
            while (true)
            	new ClientHandler(auditeur.accept(), ++idClient).start();
        }
        
        catch (IllegalArgumentException e)
        {
        	System.out.println("\nArgument invalide dans la saisie de l'adresse IP du serveur (ou du port). Arr�t.\n");
        }
        
        catch (java.net.BindException e)
        {
        	System.out.println("\nImpossible d'instancier le serveur � l'adresse et au port voulus. Arr�t.\n");
        }

        finally
        {
        	auditeur.close();
        }
    }
    
    
    /*
     * Permet au serveur d'�tablir plusieurs connexions simultan�es avec diff�rents clients.
     */
	private static class ClientHandler extends Thread
	{
		private static final String REPERTOIRE_VIDE = "";
	    private Socket 				socket;
	    private int 				numeroClient;
	    
	    
	    /*
	     * Imprime � l'�cran le num�ro du client et les d�tails relatifs au socket.
	     */
	    public ClientHandler(Socket socket, int numeroClient)
	    {
	        this.socket 		= socket;
	        this.numeroClient 	= numeroClient;
	        System.out.println("\nConnexion �tablie avec succ�s pour le client #" + numeroClient + " (" + socket + ")\n");
	    }
	
	    
	    /*
	     * Permet de retourner le chemin du r�pertoire courant sans barre oblique.
	     * @param 	[String] 	repertoireCourant	Le r�pertoire courant.
	     * @return 	[String] 						Le chemin du r�pertoire courant sans barre oblique.
	     */
		public static String getCheminSansBarreOblique(String repertoireCourant)
		{
			String cheminSansBarreOblique = "";
			
			//? Si le r�pertoire courant n'est pas vide ("")
			if (!repertoireCourant.equals(REPERTOIRE_VIDE))
				cheminSansBarreOblique = repertoireCourant.substring(1);
		
			else
				cheminSansBarreOblique = repertoireCourant;
			
			return cheminSansBarreOblique;
		}
		
		
	    /*
	     * Code principal qui roule pour tous les clients lorsque la connexion est �tablie correctement.
	     * @throws 	[IOException] 	S'il y a eu un probl�me dans l'ouverture ou la fermeture du socket avec le client.
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
	        	
	    		//? Canal entrant qui re�oit les messages envoy�s par le serveur
	            DataOutputStream donneesEnvoyees = new DataOutputStream(socket.getOutputStream());
	            donneesEnvoyees.writeUTF("Salutations, client #" + numeroClient + " !\n"
	            		+ "La connexion est bel et bien �tablie."
	            		+ "\n\nVeuillez choisir une commande entre:\n\n"
	    				+ "- cd <Nom d'un r�pertoire sur le serveur>\n		Commande permettant � l'utilisateur de se d�placer vers un r�pertoire enfant ou parent.\n"
	    				+ "- ls\n		Commande permettant d�afficher � l�utilisateur tous les dossiers et fichiers dans le r�pertoire courant de l�utilisateur au niveau du serveur.\n"
	    				+ "- mkdir <Nom du nouveau dossier>\n		Commande permettant la cr�ation d�un dossier au niveau du serveur de stockage.\n"
	    				+ "- upload <Nom du fichier>\n		Commande permettant le t�l�versement d�un fichier, se trouvant dans le r�pertoire locale du client, vers le serveur de stockage.\n"
	    				+ "- download <Nom du fichier>\n		Commande permettant le t�l�chargement d�un fichier, se trouvant dans le r�pertoire courant de l�utilisateur au niveau du serveur de stockage, vers le r�pertoire local du client.\n"
	    				+ "- exit\n		Commande permettant au client de se d�connecter du serveur de stockage.\n");
	      
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
	    			
	    			//? Notifier le client que le serveur est pr�t � recevoir la commande
	    			donneesEnvoyees.writeUTF(SERVEUR_PRET);
	    			
	    			reponseClient = donneesRecues.readUTF();
	    			
	    			//? Nous envoyons les commandes des clients via la console
	    			System.out.format("\n\n[%s - %s] : %s", socket.getRemoteSocketAddress().toString().substring(1), new SimpleDateFormat(FORMAT_DATE_ET_HEURE).format(Calendar.getInstance().getTime()), reponseClient);
	    			
	    			//? Nous s�parons la cat�gorie de la commande et la pr�cision du nom pour les commandes qui en requi�rent
	    			commandesClientSeparees = reponseClient.split(" ");
	    			
	    			//? La commande doit �tre de longueur 1 ou 2 Strings
	    			if (commandesClientSeparees.length > COMMANDE_CLIENT_TAILLE_MINIMALE && !(commandesClientSeparees.length > COMMANDE_CLIENT_TAILLE_MAXIMALE))
	    			{
	    				//? Nous poss�dons une commande de longueur valide
	    				switch (commandesClientSeparees[0]) 
	    				{
	    				case "cd":
	    					//? Nous nous assurons que la commande <cd> poss�de un nom de r�pertoire
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
	    						//? Si le nom du r�pertoire correspond � <..>, nous reculons d'un r�pertoire
	    						if (commandesClientSeparees[1].equals(repertoireParent))
	    						{
	    							//? Nous regardons si nous sommes � la racine du serveur
	    							int positionDerniereOccurenceBarreOblique = repertoireCourant.lastIndexOf(File.separator);
	    							
	    							if (positionDerniereOccurenceBarreOblique != INDEX_ELEMENT_NON_TROUVE)
	    							{
	    								//? Si nous ne sommes pas � la racine, nous reculons d'un r�pertoire
	    								repertoireCourant = repertoireCourant.substring(0, positionDerniereOccurenceBarreOblique);
	    								
	    								if (repertoireCourant.equals(REPERTOIRE_VIDE))
	    									donneesEnvoyees.writeUTF(String.format("\nVous vous situez � la racine du serveur.\n", repertoireCourant));
	    								
	    								else
	    									donneesEnvoyees.writeUTF(String.format("\nR�pertoire courant: <%s>\n", repertoireCourant));
	    							}
	    								
	    							else
	    								donneesEnvoyees.writeUTF("\nVous �tes d�j� � la racine du serveur.\n");
	    						}
	    						
	    						else
	    						{
	    							//? Nous devons concat�ner un r�pertoire
	    							boolean dossierPresent = false;
	    							
	    							cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
	    							
	    	    					try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(cheminSansBarreOblique), GLOBE))
	    	    					{
	    	    					    for (Path fichier : stream) 
	    	    					    	//? Nous v�rifions que le fichier est un dossier et qu'il correspond � la commande du client
	    	    					    	if (fichier.getFileName().toString().equals(commandesClientSeparees[1]) && Files.isDirectory(fichier))
	    	    					    	{
	    	    					    		dossierPresent = true;
	    	    					    		break;
	    	    					    	}
	    	    					}
	    	    					
	    	    					finally
	    	    					{
	    	    						if (!dossierPresent)
	    	    							//? Le fichier demand� n'est pas pr�sent dans le r�pertoire courant
	    	    							donneesEnvoyees.writeUTF(String.format("\nLe dossier <%s> n'est pas pr�sent au sein du r�pertoire <%s>.\n", commandesClientSeparees[1], repertoireCourant));
	    	    						
	    	    						else
	    	    						{
	    	    							//? Le fichier demand� existe, concat�nation
	    	    							repertoireCourant += (File.separator + commandesClientSeparees[1]);
	    	    							donneesEnvoyees.writeUTF(String.format("\nR�pertoire courant: <%s>\n", repertoireCourant));
	    	    						}
	    	    					}
	    						}
	    					}
	    					
	    					else
	    						donneesEnvoyees.writeUTF("\nCommande <cd> prend obligatoirement un chemin.\n");
	    					
	    					break;
	    				
	    				case "ls": //? Inspir� de https://stackoverflow.com/questions/15598657/how-to-use-ls-c-command-in-java
	    					donneesEnvoyees.writeUTF("\nFichiers pr�sents au sein du r�pertoire:\n");
	    					
	    					cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
	    					
	    					//? Nous parcourons tous les fichiers pr�sents au sein du r�pertoire courant
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
	    						//? V�rifions que nous n'avons que des chiffres, des lettres, des tirets (- et _)
	    						for (char caractere : commandesClientSeparees[1].toCharArray())
	    						{
	    							//? Nous acceptons les lettres (maj, min), les chiffres (0-9), de m�me que '-' et '_'
	    							if (!Character.isLetterOrDigit(caractere) && !(Character.compare(caractere, '-') == 0) && !(Character.compare(caractere, '_') == 0))
	    							{
	    								donneesEnvoyees.writeUTF("\nCommande <mkdir> invalide; caract�re invalide dans la saisie du nom du dossier � cr�er.\n");
	    								commandeValide = false;
	    								break;
	    							}	
	    						}
	    					}
	    					
	    					if (commandeValide)
	    					{
	    						cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
		    				
	    						File dossier;
	    						
	    						//? Nous cr�ons le dossier avec le bon chemin
    							if (!cheminSansBarreOblique.equals(REPERTOIRE_VIDE))
    								dossier = new File(cheminSansBarreOblique + File.separator + commandesClientSeparees[1]);
    							
    							else
    								dossier = new File(commandesClientSeparees[1]);
	    						
    							//? Nous nous assurons que le dossier n'existe pas d�j�
	    						if (!dossier.exists()) 
	    						{
	    							dossier.mkdir();
	    							donneesEnvoyees.writeUTF(String.format("\nDossier <%s> cr�� avec succ�s au sein du r�pertoire.\n", commandesClientSeparees[1]));
	    						}
	    						
	    						else
	    							donneesEnvoyees.writeUTF(String.format("\nVotre dossier <%s> est d�j� pr�sent au sein du r�pertoire.\n", commandesClientSeparees[1]));
	    					}
	    					
	    					break;
	    					
	    				case "upload":
	    					//? upload requiert obligatoirement un nom de t�l�versement
	    					
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
		    					//? On informe le client que le serveur est pr�t � t�l�verser son fichier
		    					donneesEnvoyees.writeUTF(CLIENT_PRET_TELEVERSEMENT);
		    					
		    					//? On envoie le nom du fichier trait� au client pour qu'il puisse v�rifier de son c�t� qu'il existe au sein de son r�pertoire local
		    					donneesEnvoyees.writeUTF(commandesClientSeparees[1]);
		    					
		    					//? On v�rifie que le client t�l�verse un fichier qui existe
		    					final String VERIFICATION_FICHIER_CLIENT = donneesRecues.readUTF();
		    					
		    					if (SERVEUR_PRET.equals(VERIFICATION_FICHIER_CLIENT))
		    					{
		    						//? On re�oit la taille du fichier du client
		    						final int 	TAILLE_FICHIER 	= Integer.parseInt(donneesRecues.readUTF());
		    						
		    						//? Nous cr�ons notre tampon d'octets
			    					byte[] 		octetsRecus 	= new byte[TAILLE_FICHIER];
			    					int 		octetsLus 		= 0;
			    					
			    					//? Nous lisons les octets re�us
			    					while (octetsLus < TAILLE_FICHIER)
			    						octetsLus += donneesRecues.read(octetsRecus, octetsLus, TAILLE_FICHIER - octetsLus);
			    					
			    					cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
			    					
			    					//? Nous �crivons dans le fichier � partir des octets re�us
			    					FileOutputStream fos;
			    					
	    							if (!cheminSansBarreOblique.equals(REPERTOIRE_VIDE))
	    								fos = new FileOutputStream(cheminSansBarreOblique + File.separator + commandesClientSeparees[1]);
	    							
	    							else
	    								fos = new FileOutputStream(commandesClientSeparees[1]);

			    					fos.write(octetsRecus);
			    					fos.close();
			    					
			    					donneesEnvoyees.writeUTF(String.format("\nLe fichier <%s> a bien �t� t�l�vers�.\n", commandesClientSeparees[1]));
		    					}
	    					}
	    					
	    					else
	    						donneesEnvoyees.writeUTF("\nLa commande <upload> prend obligatoirement un nom de fichier.\n");
		    				
	    					break;
	    				
	    				case "download":
	    					//? upload requiert obligatoirement un nom de t�l�chargement
	    					
	    					if (commandesClientSeparees.length == LONGUEUR_COMMANDE_ET_NOM)
	    					{
	    						//? Nous v�rifions que le fichier demand� existe
								boolean fichierPresent = false;
								
								cheminSansBarreOblique = getCheminSansBarreOblique(repertoireCourant);
								
		    					try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(cheminSansBarreOblique), GLOBE))
		    					{
		    					    for (Path fichier : stream) 
		    					    	//? Nous v�rifions que le fichier n'est pas un dossier et que nous avons les permissions n�cessaires pour le lire
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
		    							//? Le client est fin pr�t au t�l�chargement
		    							donneesEnvoyees.writeUTF(CLIENT_PRET_TELECHARGEMENT);
		    							
		    							//? Nous envoyons le nom du fichier � cr�er
		    							donneesEnvoyees.writeUTF(commandesClientSeparees[1]);
		    						
		    							//? Nous d�butons l'envoi des donn�es du fichier, source: https://stackoverflow.com/questions/9520911/java-sending-and-receiving-file-byte-over-sockets
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
		    							
		    							//? Nous envoyons la taille du fichier � cr�er
		    							donneesEnvoyees.writeUTF(Long.toString(fichierAEnvoyer.length()));
		    					
		    							//? Nous remplissons notre tampon des octets du fichier � transmettre
	    								octetsFichier = Files.readAllBytes(chemin);
		    							
		    							//? Nous envoyons les octets au client
		    							donneesEnvoyees.write(octetsFichier, 0, octetsFichier.length);
		    						}
		    						
		    						else
		    						{
		    							donneesEnvoyees.writeUTF(String.format("\nVotre fichier demand� <%s> n'existe pas au sein du r�pertoire serveur <%s> (ou n'est pas un fichier t�l�chargeable).\n", commandesClientSeparees[1], repertoireCourant));
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
	    		                System.out.println("\nConnexion avec le client #" + numeroClient + " ferm�e avec succ�s.\n");
	    		            } 
	    		            
	    		            catch (IOException e)
	    		            {
	    		                System.out.println("\nERREUR ! Nous n'avons pas pu fermer la connexion avec le client #" + numeroClient + "\n");
	    		            }
	    		            
		    				return;
	    		           
	    					
	    				default:
	    					donneesEnvoyees.writeUTF("\nVotre commande n'existe pas. Veuillez vous r�f�rer � la liste de commande cit�e plus haut.");
	    				}
	    			}
	    			
	    			else 
	    				donneesEnvoyees.writeUTF("\nVotre commande est de longueur invalide. Veuillez vous r�f�rer � la liste de commande cit�e plus haut.");
	    		}
	        } 
	        
	        catch (IOException e)
	        {
	            System.out.println("\nERREUR ! Impossible d'�tablir la connexion avec le client #" + numeroClient + ". (" + e + ")");
	        }	        
	    }
	}
}
