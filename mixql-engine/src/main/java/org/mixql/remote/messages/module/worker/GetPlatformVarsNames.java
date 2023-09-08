package org.mixql.remote.messages.module.worker;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.module.IModuleSendToClient;

public class GetPlatformVarsNames implements IWorkerSendToClient {

    @Override
    public String workerIdentity() {
        return _sender;
    }

    private String _sender;

    private String _clientAddress;

    @Override
    public String clientIdentity() {
        return _clientAddress;
    }

    public GetPlatformVarsNames(String sender, String clientAddress) {
        _sender = sender;
        _clientAddress = clientAddress;
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
