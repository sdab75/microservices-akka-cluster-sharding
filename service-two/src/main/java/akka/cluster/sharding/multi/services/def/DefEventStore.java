package akka.cluster.sharding.multi.services.def;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.cluster.sharding.multi.services.common.MyCounter;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 9/28/2015.
 */
public class DefEventStore extends UntypedPersistentActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Override
    public void preStart() throws Exception {
        System.out.println("DefEventStore Startup ###########################");
        super.preStart();
        context().setReceiveTimeout(Duration.create(120, TimeUnit.SECONDS));
    }

    @Override
    public String persistenceId() {
        return "DefEventStore-" + getContext().parent().path().name();
    }

    @Override
    public void onReceiveRecover(Object msg) {
        System.out.println("****************************============>"+msg.toString());
        if (msg instanceof MyCounter) {
            System.out.println("DefEventStore :  Recovered Event -->" + ((MyCounter) msg).getMsg());
            processEvent(((MyCounter) msg));
        } else {
            unhandled(msg);
        }
    }

    @Override
    public boolean recoveryRunning() {
        log.info("DefEventStore: Recovery running @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryRunning();
    }

    @Override
    public boolean recoveryFinished() {
        System.out.println("DefEventStore: Recovery finished @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        System.out.println("DefEventStore: Recovery failed @@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.onRecoveryFailure(cause, event);
    }

    private void processEvent(MyCounter evt){
/*
        if(evt.getCount()==190){
            throw  new RuntimeException("Failed processing counter --->" +  evt.toString());
        }
*/
        System.out.println("DefEventStore: Successfully processed persisted event-->" + evt.getMsg());
        saveSnapshot(evt);
    }
    @Override
    public void onReceiveCommand(Object msg) {
        if (msg instanceof MyCounter) {
            log.info("DefEventStore: onReceiveCommand #######", ((MyCounter) msg).getMsg());
            MyCounter evt = ((MyCounter) msg);
            evt.setMsg(evt.getMsg() + "-->Validated");
            persist(evt, new Procedure<MyCounter>() {
                public void apply(MyCounter evt) throws Exception {
                    processEvent(evt);
                }
            });

        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("DefEventStore subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(msg);
    }
}
