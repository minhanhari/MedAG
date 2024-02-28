package com.minhanh.coreg.socket;

import com.minhanh.coreg.rbp.RbpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

@Controller
public class GraphController {

    @Autowired
    RbpService rbpService;

    @MessageMapping("/transfactor")
    @SendTo("/topic/nodeinfo")
    public RbpResult getRbpResult(TfMessage node) throws Exception, RemoteException, NotBoundException {
        return new RbpResult(rbpService.filterByName(node.getId()));
    }
}