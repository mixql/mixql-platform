package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.Message;
import org.mixql.remote.messages.module.IModuleSendToClient;

public class SendMsgToPlatform implements IWorkerSender {
    public IModuleSendToClient msg;

    @Override
    public String workerIdentity() {
        return _sender;
    }

    private String _sender;

    public SendMsgToPlatform(IModuleSendToClient msg, String workerID) {
        this.msg = msg;
        _sender = workerID;
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
