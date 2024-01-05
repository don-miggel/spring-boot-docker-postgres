package com.kaluzny.demo.listener;

import com.kaluzny.demo.domain.Automobile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Consumer {

    private Map<String, List<Automobile>> autosList = new HashMap<>();

    @JmsListener(destination = "AutoTopic", containerFactory = "automobileJmsContFactory")
    public void getAutomobileListener1(Automobile automobile) {
        log.info("\u001B[32m" + "Automobile Consumer 1: " + automobile + "\u001B[0m");
    }

    @JmsListener(destination = "AutoTopic", containerFactory = "automobileJmsContFactory")
    public void getAutomobileListener2(Automobile automobile) {
        log.info("\u001B[33m" + "Automobile Consumer 2: " + automobile + "\u001B[0m");
    }

    @JmsListener(destination = "AutoTopic", containerFactory = "automobileJmsContFactory")
    public void getAutomobileListener3(Automobile automobile) {
        log.info("\u001B[34m" + "Automobile Consumer 3: " + automobile + "\u001B[0m");
    }

    @JmsListener(destination = "Color", containerFactory = "automobileJmsContFactory")
    public void getAutomobileListenerColor(Automobile automobile) {
        if(!autosList.containsKey(automobile.getColor().toLowerCase())) {
            autosList.put(automobile.getColor().toLowerCase(), new ArrayList<>());
            autosList.get(automobile.getColor().toLowerCase()).add(automobile);
        }else
            autosList.get(automobile.getColor().toLowerCase()).add(automobile);
        log.info("\u001B[34m" + "Number of autos with "+ automobile.getColor().toLowerCase() +" color : "
                + autosList.get(automobile.getColor().toLowerCase()).size() + "\u001B[0m");

    }
}
