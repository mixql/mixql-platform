package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.client.IWorkerReceiver;

public class WorkerFinished implements IWorkerSender {
    public String Id;

    @Override
    public String workerIdentity() {
        return Id;
    }


    public WorkerFinished(String workersID) {
        this.Id = workersID;
    }

    @Override
    public String toString() {
        try {
            return RemoteMessageConverter.toJson(this);
        } catch (Exception e) {
            System.out.println(
                    String.format("Error while toString of class type %s, exception: %s\nUsing default toString",
                            type(), e.getMessage())
            );
            return super.toString();
        }
    }
}
