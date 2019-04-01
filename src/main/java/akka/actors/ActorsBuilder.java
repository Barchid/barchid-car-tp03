package akka.actors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Properties;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

/**
 * @author Sami BARCHID
 * 
 *         Loads the configuration file ("actors.properties") to build and link
 *         all the actors between them
 */
public class ActorsBuilder {
	/**
	 * The properties of the file.properties
	 */
	private Properties properties;

	private Map<Integer, ActorRef> actors = new HashMap<>();

	public ActorsBuilder(String filename) throws FileNotFoundException, IOException {
		System.out.println("Loading the properties file...");
		try (InputStream file = new FileInputStream(filename)) {
			this.properties = new Properties();
			this.properties.load(file);
		}
	}

	/**
	 * Creates an actor system in a random port and creates the related NodeActor in
	 * the actor system.
	 */
	private void createClusterNode(int number, ArrayList<Integer> nextNumbers) {
		Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 0)
				.withFallback(ConfigFactory.parseString("akka.cluster.roles = [Node]"))
				.withFallback(ConfigFactory.load());

		ActorSystem system = ActorSystem.create("ClusterSystem", config);
		ActorRef actor = system.actorOf(NodeActor.props(number, nextNumbers), "NodeActor");
		this.actors.put(number, actor);
	}

	/**
	 * Builds all the nodes related to the properties file of the current
	 * ActorsBuilder.
	 */
	public void buildNodes() {
		for (Object key : properties.keySet()) {
			String keyProp = (String) key;
			int number = Integer.parseInt(keyProp);
			ArrayList<Integer> nextNumbers = new ArrayList<Integer>();

			String values = properties.getProperty(keyProp);

			if (!values.equals("NO")) {
				String[] nexts = values.split(",");
				for (String val : nexts) {
					int nextNum = Integer.parseInt(val);
					nextNumbers.add(nextNum);
				}
			}
			createClusterNode(number, nextNumbers);
		}
	}

	public Map<Integer, ActorRef> getActors() {
		return this.actors;
	}
}
