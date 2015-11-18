package akka.cluster.sharding.multi.services.def;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.multi.services.common.MyCounter;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * Created by davenkat on 9/28/2015.
 */
public class DefPublisher extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    // activate the extension
    ActorRef mediator =DistributedPubSub.get(getContext().system()).mediator();
    public void onReceive(Object msg) {
        if (msg instanceof MyCounter) {
            MyCounter myCounter =(MyCounter) msg;
            //Following is for broadcasting
            mediator.tell(new DistributedPubSubMediator.Publish("abcEventContent", myCounter,true),getSelf());
        }
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log.info("publisher subscribing");
        else {
            unhandled(msg);
        }
    }
}