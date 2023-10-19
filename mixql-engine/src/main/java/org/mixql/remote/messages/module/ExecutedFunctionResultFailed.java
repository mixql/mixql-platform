package org.mixql.remote.messages.module;

import org.mixql.remote.RemoteMessageConverter;
import org.mixql.remote.messages.rtype.Error;

public class ExecutedFunctionResultFailed extends Error implements IModuleSendToClient {
    private String _clientAddress;

    public ExecutedFunctionResultFailed(String errorMsg, String clientAddress) {
        super(errorMsg);
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

    @Override
    public String clientIdentity() {
        return _clientAddress;
    }
}
