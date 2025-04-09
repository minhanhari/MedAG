package com.minhanh.coreg.socket;

import com.minhanh.coreg.transfactor.TransFactorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GraphController {

    @Autowired
    TransFactorService transFactorService;

    @MessageMapping("/transfactor")
    @SendTo("/topic/nodeinfo")
    public TfResult getTfResult(TfMessage node) throws Exception {
        return new TfResult(transFactorService.filterByName(node.getId()));
    }
}