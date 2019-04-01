package akka;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		String propertiesFile = args.length > 0 ? args[0] : null;
		Map<Integer, ActorRef> actors = null;
		if (propertiesFile != null) {
			try {
				actors = initClusterNodes(propertiesFile);
			} catch (Exception e) {
				System.err.println("ERROR : cannot load properties file. Abort...");
			}

			Thread.sleep(10000);
			UserInteraction interaction = new UserInteraction(actors);
			interaction.interaction();
		} else {
			createSeedNode();
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
	 * @param propertiesFilename
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @return a map of ActorRefs depending on their number.
	 */
	private static Map<Integer, ActorRef> initClusterNodes(String propertiesFilename)
			throws FileNotFoundException, IOException {
		ActorsBuilder builder = new ActorsBuilder(propertiesFilename);
		builder.buildNodes();
		return builder.getActors();
	}
}
