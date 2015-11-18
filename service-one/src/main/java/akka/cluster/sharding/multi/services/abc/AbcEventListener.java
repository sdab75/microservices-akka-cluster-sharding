package akka.cluster.sharding.multi.services.abc;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.multi.services.common.MyCounter;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by davenkat on 9/28/2015.
 */
public class AbcEventListener extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public AbcEventListener() {
        ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
        // subscribe to the topic named "content"
        mediator.tell(new DistributedPubSubMediator.Subscribe("abcEventContent", "abcEventGrp", getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());

    }

    private SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
        @Override
        public SupervisorStrategy.Directive apply(Throwable t) {
            System.out.println("**********In Subscriber supervisorStrategy SupervisorStrategy.Directive apply " +
                    "*******************");
            if (t instanceof NullPointerException) {
                System.out.println("oneToOne Subscriber: restartOrEsclate strategy, restarting the actor");
                return restart();
            } else {
                System.out.println("$$$$$$$$$$$$$$$$$ Subscriber oneToOne: final else called escalating to oneToAll");
                return escalate();
            }
        }
    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("**********In Subscriber supervisorStrategy *******************");
        return strategy;
    }
    //get supervisor actor from shard
    ActorRef myEntitySupervisor = ClusterSharding.get(getContext().system()).shardRegion("AbcEventStoreSupervisor");

    public void onReceive(Object msg) {
        System.out.println("AbcEventListener Received============>"+msg.toString());
        if (msg instanceof MyCounter) {
            myEntitySupervisor.tell(msg,getSelf());
        } else if (msg instanceof DistributedPubSubMediator.Subscribe)
            log.info("subscribe started !!!!!!!!!!!!");
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("subscribing");
        else
            unhandled(msg);
    }
}