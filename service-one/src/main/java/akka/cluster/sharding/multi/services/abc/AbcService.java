package akka.cluster.sharding.multi.services.abc;

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

public class AbcService {

    public static void main(String[] args) {
        if (args.length == 0)
            startup(new String[]{"2550"});
        else
            startup(args);
    }

    public static void startup(String[] ports) {
        for (String port : ports) {
            // Override the configuration of the port
            Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load());

            ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {

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

            // Create an Akka system
            ActorSystem system = ActorSystem.create("ClusterSystem", config);

            ClusterShardingSettings settings = ClusterShardingSettings.create(system).withRole("one");
            ClusterSharding.get(system).start("AbcEventStoreSupervisor", Props.create(AbcEventStoreSupervisor.class), settings, messageExtractor);
            ClusterSharding.get(system).start("AbcEventStore", Props.create(AbcEventStore.class), settings,messageExtractor);


            ActorRef subscriber1 = system.actorOf(Props.create(AbcEventListener.class), "subscriber1");

            ActorRef pub = system.actorOf(Props.create(AbcPublisher.class).withRouter(new RoundRobinPool(5)), "abcPublisher");

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String entityId="Plan-1234";

            for(int i=0;i<10;i++){
                System.out.println("Abc service sending to def ..."+i);
                MyCounter myCounter =new MyCounter(entityId,i,"From Abc to Def EventStore-Counter -->"+i);
                pub.tell(myCounter,null);
            }

        }
    }
}
