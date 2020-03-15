package actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CloneActor extends AbstractBehavior<CloneActor.Message> {

    public static final class Message {
        public final int numberOfHopsTravelled;
        public final int amountActors;
        public final Map<CloneActor, Long> map;

        public Message(int numberOfHopsTravelled, int amountActors, Map<CloneActor, Long> map) {
            this.numberOfHopsTravelled = numberOfHopsTravelled;
            this.amountActors = amountActors;
            this.map = map;
        }
    }

    private final int id;

    private CloneActor(ActorContext<Message> context, int id) {
        super(context);
        this.id = id;
    }

    public static Behavior<Message> create(int id) {
        return Behaviors.setup(context -> new CloneActor(context, id));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder().onMessage(CloneActor.Message.class, this::onCloneActorMessage).build();
    }

    @Override
    public String toString() {
        return "actor " + id + " message received ";
    }

    private Behavior<Message> onCloneActorMessage(CloneActor.Message message) {
        Map<CloneActor, Long> map = new HashMap<>(message.map);
        map.put(this, System.nanoTime());
        if (id == message.amountActors) {
            System.out.println(map);
            return Behaviors.stopped();
        } else {
            int newId = id + 1;
            ActorRef<Message> messageActorRef = getContext().spawnAnonymous(CloneActor.create(newId));
            int newNumberOfHopsTravelled = message.numberOfHopsTravelled + 1;
            messageActorRef.tell(new CloneActor.Message(newNumberOfHopsTravelled, message.amountActors, map));
            return this;
        }
    }
}
