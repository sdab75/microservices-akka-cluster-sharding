package akka.cluster.sharding.multi.services.def;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.multi.services.common.MyCounter;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.escalate;
import static akka.actor.SupervisorStrategy.restart;

/**
 * Created by rmotapar on 11/12/2015.
 */
public class DefEventStoreSupervisor extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef myEntity = getContext().actorOf(Props.create(DefEventStore.class), "defEventStore");


    private SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
        @Override
        public SupervisorStrategy.Directive apply(Throwable t) {
            System.out.println("**********In supervisorStrategy SupervisorStrategy.Directive apply *******************");
            if (t instanceof NullPointerException) {
                System.out.println("oneToOne: restartOrEsclate strategy, restarting the actor");
                return restart();
            } else {
                System.out.println("$$$$$$$$$$$$$$$$$ oneToOne: final else called escalating to oneToAll");
                return escalate();
            }
        }
    });

    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("**********In supervisorStrategy *******************");
        return strategy;
    }

    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            log.info("MyEntitySupervisor Got: {}", msg);
            myEntity.forward(msg, getContext());

        } else if (msg instanceof DistributedPubSubMediator.Subscribe)
            log.info("subscribe started !!!!!!!!!!!!");
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("subscribing");
        else
            unhandled(msg);
    }

}
