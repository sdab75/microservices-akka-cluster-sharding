package akka.cluster.sharding.multi.services.common;

import java.io.Serializable;

/**
 * Created by davenkat on 11/8/2015.
 */
public class MyCounter implements Serializable {

    private String entityId;
    private int count;
    private String msg;

    public MyCounter() {
    }

    public MyCounter(String entityId, int count, String msg) {
        this.entityId = entityId;
        this.count = count;
        this.msg = msg;
    }


    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MyCounter)) return false;

        MyCounter myCounter = (MyCounter) o;

        if (getCount() != myCounter.getCount()) return false;
        if (getEntityId() != null ? !getEntityId().equals(myCounter.getEntityId()) : myCounter.getEntityId() != null)
            return false;
        return !(getMsg() != null ? !getMsg().equals(myCounter.getMsg()) : myCounter.getMsg() != null);

    }

    @Override
    public String toString() {
        return "MyCounter{" +
                "count=" + count +
                ", entityId='" + entityId + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        int result = getEntityId() != null ? getEntityId().hashCode() : 0;
        result = 31 * result + getCount();
        result = 31 * result + (getMsg() != null ? getMsg().hashCode() : 0);
        return result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
