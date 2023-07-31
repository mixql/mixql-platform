package org.mixql.remote.messages.module.worker;

public class WorkerFinished implements IWorkerSender {
    public String Id;

    @Override
    public String sender() {
        return Id;
    }


    public WorkerFinished(String workersID) {
        this.Id = workersID;
    }

    @Override
    public String toString() {
        return "{ type: " + type() + " sender: " + Id + "}";
    }
}
