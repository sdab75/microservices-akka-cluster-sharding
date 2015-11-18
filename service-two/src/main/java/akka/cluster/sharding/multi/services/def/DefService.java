package akka.cluster.sharding.multi.services.def;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.cluster.sharding.multi.services.common.MyCounter;
import akka.routing.RoundRobinPool;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Option;

import java.util.Optional;

public class DefService {

    public static void main(String[] args) {
        if (args.length == 0)
            startup(new String[]{"2551"});
        else
            startup(args);
    }
    public static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

        @Override
        public String entityId(Object message) {
            MyCounter counter=(MyCounter) message;
            return String.valueOf(counter.getEntityId());
        }

        @Override
        public Object entityMessage(Object message) {
            return message;
        }

        @Override
        public String shardId(Object message) {
            int numberOfShards = 100;
            if (message instanceof MyCounter) {
                MyCounter counter=(MyCounter) message;
                String shardId= String.valueOf(counter.getEntityId().length() % numberOfShards);
                System.out.println("Shard id -------->"+shardId);
                return  shardId;
            } else {
                return null;
            }
        }
    };


    public static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load());

            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);

            ClusterShardingSettings settings = ClusterShardingSettings.create(system).withRole("two");
            ClusterSharding.get(system).start("DefEventStoreSupervisor", Props.create(DefEventStoreSupervisor.class), settings, messageExtractor);
            ClusterSharding.get(system).start("DefEventStore", Props.create(DefEventStore.class), settings, messageExtractor);

            ActorRef subscriber1 = system.actorOf(Props.create(DefEventListener.class), "subscriber1");
            ActorRef pub = system.actorOf(Props.create(DefPublisher.class).withRouter(new RoundRobinPool(5)), "defPublisher");

            try {
                Thread.sleep(22000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String entityId="Plan-1234";

            for(int i=0;i<10;i++){
                System.out.println("Def service sending to abc..."+i);
                MyCounter myCounter =new MyCounter(entityId,i,"From Def To Abc EventStore-Counter -->"+i);
                pub.tell(myCounter,null);
            }


        }
    }
}
