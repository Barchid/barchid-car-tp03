package akka.actors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actors.NodeMessages.AddChildNode;
import akka.actors.NodeMessages.RemoveChildNode;
import akka.actors.NodeMessages.StopMessage;
import akka.actors.NodeMessages.TransferMessage;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent.CurrentClusterState;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.cluster.Member;
import akka.cluster.MemberStatus;

/**
 * 
 * @author Sami BARCHID
 *
 *         Represents a node used in the tree/graph structure of actors in the
 *         AKKA application. The main goal of a node actor is to forward the
 *         messages from the previous nodes to the succeeding nodes.
 */
public class NodeActor extends AbstractActor {
	/**
	 * The number of the node actor used to identify it.
	 */
	private int number;

	/**
	 * The list of numbers of the nodes that succeed the current node
	 */
	private ArrayList<Integer> nextNumbers;

	/**
	 * The list of nodes that succeed the current node
	 */
	private ArrayList<ActorRef> nextNodes;

	/**
	 * Logger used to print the messages received by the application
	 */
	private LoggingAdapter logger;

	/**
	 * The cluster instance
	 */
	private Cluster cluster;

	/**
	 * Map that links every actor reference to its number
	 */
	private Map<ActorRef, Integer> actorNumber;

	/**
	 * @param number
	 * @param previousNumbers
	 * @param nextNumbers
	 */
	public NodeActor(int number, ArrayList<Integer> nextNumbers) {
		super();
		this.number = number;
		this.nextNumbers = nextNumbers;
		this.nextNodes = new ArrayList<ActorRef>();
		this.logger = Logging.getLogger(this.getContext().getSystem(), this);
		this.cluster = Cluster.get(this.getContext().system());
		this.actorNumber = new HashMap<ActorRef, Integer>();
	}

	public static Props props(int number, ArrayList<Integer> nextNumbers) {
		return Props.create(NodeActor.class, () -> new NodeActor(number, nextNumbers));
	}

	@Override
	public void preStart() {
		this.cluster.subscribe(this.self(), MemberEvent.class, UnreachableMember.class);
	}

	@Override
	public void postStop() {
		this.cluster.unsubscribe(this.self());
	}

	@Override
	public Receive createReceive() {
		return this.receiveBuilder().match(MemberUp.class, memberUp -> this.onMemberUp(memberUp))
				.match(Integer.class, number -> this.onNumberMessage(number))
				.match(UnreachableMember.class, mUnreachable -> {
					this.logger.info("Member detected as unreachable: {}", mUnreachable.member());
				}).match(MemberRemoved.class, mRemoved -> {
					this.logger.info("Member is Removed: {}", mRemoved.member());
				}).match(CurrentClusterState.class, state -> this.onCurrentClusterState(state))
				.match(TransferMessage.class, transfer -> this.onTransferMessage(transfer))
				.match(RemoveChildNode.class, rm -> this.onRemoveChildNode(rm))
				.match(StopMessage.class, stop -> this.onStopMessage(stop))
				.match(AddChildNode.class, addChild -> this.onAddChildNode(addChild)).build();

	}

	/**
	 * Callback used when a MemberUp notification has been received by the current
	 * node. It will send the number of
	 * 
	 * @param memberUp
	 */
	private void onMemberUp(MemberUp memberUp) {
		this.logger.info("\n\nMember is Up: {}", memberUp.member());
		if (memberUp.member().hasRole(NodeMessages.ROLE_NODE)) {
			this.sendNumber(memberUp.member());
		}
	}

	/**
	 * 
	 * Callback to a transfer message to log the messages and forward to the next
	 * nodes.
	 * 
	 * @param transfer the transfer message to handle.
	 */
	private void onTransferMessage(TransferMessage transfer) {
		this.logger.info("\n\n" + this.number + " - message received : {}", transfer.getText());

		for (ActorRef actorRef : this.nextNodes) {
			actorRef.tell(transfer, this.sender());
		}
	}

	/**
	 * Callback used to handle a number oof node received by the current actor. It
	 * will register the number inside the next nodes to build the graph if the
	 * number is in the previous numbers list of the current node
	 * 
	 * @param number the number received by the current node.
	 */
	private void onNumberMessage(int number) {
		this.logger.info("New number message received from {} : {}", this.sender(), number);

		if (this.nextNumbers.contains(number) && !this.nextNodes.contains(this.sender())) {
			this.nextNodes.add(this.sender());
			this.actorNumber.put(this.sender(), number);
			return;
		}

	}

	/**
	 * Callback to retrieve the current cluster state and sends the number of the
	 * current actor to everyone in the cluster.
	 * 
	 * @param state the current state of the cluster
	 */
	private void onCurrentClusterState(CurrentClusterState state) {
		this.logger.info("\n\nonCurrentClusterState triggered.");
		for (Member member : state.getMembers()) {
			if (member.status().equals(MemberStatus.up())) {
				this.logger.info("Sending member number state to other : {}", member);
				this.sendNumber(member);
			}
		}
	}

	private void onAddChildNode(AddChildNode addChild) {
		this.logger.info("\n\nCurrent node {} received addChildNode", this.number);
		if (this.number == addChild.number) {
			this.logger.info("Current node {} will add {} as part of its next nodes.", this.number, addChild.newChild);
			this.nextNumbers.add(addChild.newChild);
		}
	}

	private void onRemoveChildNode(RemoveChildNode removeChild) {
		this.logger.info("\n\nCurrent node {} received removeChildNode", this.number);
		if (this.number == removeChild.number) {
			this.logger.info("\n\nRemoving node {} from child nodes of current node {}", removeChild.child,
					this.number);

			ActorRef toRemove = null;
			for (Entry<ActorRef, Integer> entry : this.actorNumber.entrySet()) {
				if (entry.getValue() == removeChild.child) {
					this.nextNodes.remove(entry.getKey());
					toRemove = entry.getKey();
				}
			}

			if (toRemove != null) {
				this.actorNumber.remove(toRemove);
			}
			this.nextNumbers.remove(Integer.valueOf(removeChild.child)); // mandatory to remove value and not index
		}
	}

	private void onStopMessage(StopMessage stop) {
		this.logger.info("\n\ncurrent node {} has received stop message for node of number {}", this.number,
				stop.number);
		if (this.number == stop.number) {
			this.logger.info("Current node {} is stopping forever...", this.number);
			this.context().stop(this.self());
		} else {
			ActorRef toRemove = null;
			for (Entry<ActorRef, Integer> entry : this.actorNumber.entrySet()) {
				if (entry.getValue() == stop.number) {
					this.logger.info("\n\nCurrent node {} will remove stopped node {}", this.number, stop.number);
					this.nextNodes.remove(entry.getKey());
					toRemove = entry.getKey();
				}
			}

			if (toRemove != null) {
				this.actorNumber.remove(toRemove);
			}
		}
	}

	/**
	 * Sends the number of the current node to the specified member of the cluster;
	 * 
	 * @param member the member of the cluster that will receive the number of the
	 *               current node.
	 */
	private void sendNumber(Member member) {
		this.context().actorSelection(member.address() + "/user/NodeActor").tell(this.number, this.self());
	}
}
