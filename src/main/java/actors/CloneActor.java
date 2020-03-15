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
        public final String nameActor;
        public final Map<String, Long> map;

        public Message(int numberOfHopsTravelled, int amountActors, String nameActor, Map<String, Long> map) {
            this.numberOfHopsTravelled = numberOfHopsTravelled;
            this.amountActors = amountActors;
            this.nameActor = nameActor;
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

    private Behavior<Message> onCloneActorMessage(CloneActor.Message message) {
        Map<String, Long> map = new HashMap<>(message.map);
        map.put(message.nameActor, System.nanoTime());
        if (id == message.amountActors) {
            Set<String> keys = map.keySet();
            for (String actorName : keys) {
                System.out.println(actorName + ", message received <" + map.get(actorName) + ">");
            }
            return Behaviors.stopped();
        } else {
            int newId = id + 1;
            ActorRef<Message> messageActorRef = getContext().spawnAnonymous(CloneActor.create(newId));
            int newNumberOfHopsTravelled = message.numberOfHopsTravelled + 1;
            messageActorRef.tell(new CloneActor.Message(newNumberOfHopsTravelled,
                    message.amountActors, "actor " + newNumberOfHopsTravelled, map));
            return this;
        }
    }
}
