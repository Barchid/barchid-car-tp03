package akka;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMergeable;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actors.ActorsBuilder;
import akka.actors.NodeActor;

/**
 * @author Sami BARCHID
 *
 */
public class Main {

	/**
	 * @param args the arguments of the main program
	 * @throws InterruptedException if the akka actors have a problem
	 */
	public static void main(String[] args) throws InterruptedException {
		String propertiesFile = args.length > 0 ? args[0] : "actors-full.properties";
		Map<Integer, ActorRef> actors = null;
		System.out.println("Voulez-vous activer le seed-node ? 1 pour 'Oui', autre pour 'Non'");
		int choix = new Scanner(System.in).nextInt();
		if (choix == 1) {
			createSeedNode();
		} else {
			try {
				actors = initClusterNodes(propertiesFile);
			} catch (Exception e) {
				System.err.println("ERROR : cannot load properties file. Abort...");
			}

			Thread.sleep(10000);
			UserInteraction interaction = new UserInteraction(actors);
			interaction.interaction();

		}
	}

	/**
	 * Creates the seed node that will be used to build the cluster
	 */
	private static void createSeedNode() {
		final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2551)
				.withFallback(ConfigFactory.parseString("akka.cluster.roles = [Bute]"))
				.withFallback(ConfigFactory.load());

		ActorSystem system = ActorSystem.create("ClusterSystem", config);
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		system.actorOf(NodeActor.props(100, numbers), "NodeActor");
	}

	/**
	 * Initializes the cluster nodes with an Actors builder
	 * 
	 * @param propertiesFilename the properties filename
	 * @throws FileNotFoundException if the properties file is not found
	 * @throws IOException           if the properties file has a problem
	 * @return a map of ActorRefs depending on their number.
	 */
	private static Map<Integer, ActorRef> initClusterNodes(String propertiesFilename)
			throws FileNotFoundException, IOException {
		ActorsBuilder builder = new ActorsBuilder(propertiesFilename);
		builder.buildNodes();
		return builder.getActors();
	}
}
