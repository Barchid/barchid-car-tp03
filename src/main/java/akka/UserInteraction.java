package akka;

import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actors.NodeActor;
import akka.actors.NodeMessages;

/**
 * @author Sami BARCHID
 *
 *         Class used to manipulate the User's interaction with the cluster (to
 *         send messages for example)
 */
public class UserInteraction {
	private Map<Integer, ActorRef> actors;

	private Scanner scanner;

	/**
	 * @param actors
	 */
	public UserInteraction(Map<Integer, ActorRef> actors) {
		super();
		this.actors = actors;
		this.scanner = new Scanner(System.in);
	}

	/**
	 * Handles the interaction with the user for the cluster management.
	 */
	public void interaction() {
		boolean goOn = true;
		while (goOn) {
			System.out.println("######################################################");
			System.out.println("######################################################");
			System.out.println("Bienvenue dans l'interaction avec le cluster. Choisissez ce que vous voulez faire.");
			System.out.println("######################################################");
			System.out.println("######################################################");

			System.out.println("1 -> Envoyer un message.");
			System.out.println("2 -> Créer un nouveau noeud");
			System.out.println("3 -> Ajouter un noeud enfant à un noeud existant.");
			System.out.println("4 -> Tuer un noeud existant");
			System.out.println("5 -> Retirer un lien entre un noeud et son enfant.");
			int choix = scanner.nextInt();
			if (choix == 1) {
				this.sendMessage();
			}

			if (choix == 2) {
				this.createNode();
			}

			if (choix == 3) {
				this.addChildNode();
			}

			if (choix == 4) {
				this.killNode();
			}

			if (choix == 5) {
				this.removeChildNode();
			}

			System.out.println("\n\nContinuer ? '1' pour Oui, autre pour non");
			int continueChoice = this.scanner.nextInt();
			goOn = continueChoice == 1;
		}
	}

	private void sendMessage() {
		System.out.println("Quel est le message ?");
		String message = this.scanner.next();
		System.out.println("Sur quel numéro de noeud envoyer le message ?");
		int number = this.scanner.nextInt();

		System.out.println("Le message est envoyé sur le noeud : " + number);
		this.actors.get(number).tell(new NodeMessages.TransferMessage(message), this.actors.get(number));
	}

	private void createNode() {
		System.out.println("Quel est le numéro du noeud ?");
		int number = this.scanner.nextInt();
		ArrayList<Integer> nextNumbers = new ArrayList<>();
		boolean onChoice = true;
		while (onChoice) {
			System.out.println("Choisissez un noeud enfant au noeud à créer");
			int child = this.scanner.nextInt();
			nextNumbers.add(child);
			System.out.println("Continuer à choisir des noeuds enfants ? 1 pour Oui, autre pour non.");
			int choix = this.scanner.nextInt();
			onChoice = choix == 1;
		}

		Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 0)
				.withFallback(ConfigFactory.parseString("akka.cluster.roles = [Node]"))
				.withFallback(ConfigFactory.load());

		ActorSystem system = ActorSystem.create("ClusterSystem", config);
		ActorRef actor = system.actorOf(NodeActor.props(number, nextNumbers), "NodeActor");
		this.actors.put(number, actor);
	}

	private void addChildNode() {
		System.out.println("Quel est le numéro du noeud existant ?");
		int number = this.scanner.nextInt();

		System.out.println("Quel est le numéro à ajouter aux enfants du noeud existant ?");
		int child = this.scanner.nextInt();
		this.actors.get(number).tell(new NodeMessages.AddChildNode(number, child), this.actors.get(number));
	}

	private void killNode() {
		System.out.println("Quel est le numéro du noeud à tuer ?");
		int number = this.scanner.nextInt();
		this.actors.get(number).tell(new NodeMessages.StopMessage(number), this.actors.get(number));
	}

	private void removeChildNode() {
		System.out.println("Quel est le numéro du noeud existant ?");
		int number = this.scanner.nextInt();

		System.out.println("Quel est le numéro du noeud enfant à retirer ?");
		int child = this.scanner.nextInt();
		this.actors.get(number).tell(new NodeMessages.RemoveChildNode(number, child), this.actors.get(number));
	}
}
