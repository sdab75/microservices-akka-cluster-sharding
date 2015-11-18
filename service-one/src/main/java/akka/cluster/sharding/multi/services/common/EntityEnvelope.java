package akka.cluster.sharding.multi.services.common;

import java.io.Serializable;

/**
 * Created by davenkat on 11/15/2015.
 */
public class EntityEnvelope implements Serializable{
    final public long id;
    final public Object payload;

    public EntityEnvelope(long id, Object payload) {
        this.id = id;
        this.payload = payload;
    }
}
