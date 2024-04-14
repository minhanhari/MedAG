package com.minhanh.coreg.socket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

@Controller
public class GraphController {

    @MessageMapping("/transfactor")
    @SendTo("/topic/nodeinfo")
    public RbpResult getRbpResult(TfMessage node) throws Exception, RemoteException, NotBoundException {
        return null;
    }
}