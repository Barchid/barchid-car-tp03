package akka.actors;

import java.io.Serializable;

/**
 * 
 * @author Sami BARCHID
 * 
 *         Interface that defines every messages the NodeActors can send to each
 *         other.
 */
public interface NodeMessages {
	/**
	 * Role of a node actor
	 */
	public static final String ROLE_NODE = "Node";

	/**
	 * Role of a client actor used to manage
	 */
	public static final String ROLE_CLIENT = "Client";

	/**
	 * 
	 * @author Sami BARCHID
	 *
	 *         Represents the message that will be transferred through the
	 *         tree/graph actor structure of the AKKA application.
	 */
	public static class TransferMessage implements Serializable {
		private static final long serialVersionUID = -7017165434976441773L;
		private final String text;

		/**
		 * 
		 * @param text the text message to transfer through the graph/tree sturcture.
		 */
		public TransferMessage(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

	/**
	 * 
	 * @author Sami BARCHID
	 *
	 *         Class used to indicate to an actor to stop itself.
	 */
	public static class StopMessage implements Serializable {
		private static final long serialVersionUID = 2191159667540126450L;
		public final int number;

		/**
		 * @param number the number of the actor that will stop
		 */
		public StopMessage(int number) {
			super();
			this.number = number;
		}
	}

	/**
	 * 
	 * @author Sami BARCHID
	 *
	 *         Represents a message used to indicate to the ActorNode of number
	 *         "number" to admit the nodes of number "newChild" to be a new next
	 *         nodes.
	 */
	public static class AddChildNode implements Serializable {
		private static final long serialVersionUID = -8836883019385131408L;

		public final int number;

		public final int newChild;

		/**
		 * @param number   the number of the actor that has to add the newChild
		 * @param newChild the number of the newChild
		 */
		public AddChildNode(int number, int newChild) {
			super();
			this.number = number;
			this.newChild = newChild;
		}
	}

	/**
	 * 
	 * @author Sami BARCHID
	 *
	 *         Represents a message used to indicate to the ActorNode of number
	 *         "number" to remove the nodes of number "child" from its next nodes
	 *         list.
	 */
	public static class RemoveChildNode implements Serializable {
		private static final long serialVersionUID = 4563395243815018051L;

		public final int number;

		public final int child;

		/**
		 * @param number the number of the actor that has to remove the child
		 * @param child  the number of the child to be removed
		 */
		public RemoveChildNode(int number, int child) {
			super();
			this.number = number;
			this.child = child;
		}
	}
}
