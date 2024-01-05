package com.kaluzny.demo.service;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.web.JMSPublisher;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface AutomobileService extends JMSPublisher {

    Automobile createAuto(Automobile auto);
    List<Automobile> getAllAutos();
    Automobile getAutoById(long id);
    List<String> getAutosByName();
    Collection<Automobile> getAutosByColor(String color);
    Collection<Automobile> getAutosByNameAndColor(String color, String name);
    Collection<Automobile> getByColorStartsWith(String colorStart, Pageable page);
    List<Automobile> getAllAutosByName(String name);
    Automobile updAuto(long id, Automobile auto);
    void deleteAutoById(long id);
    void deleteAllAutos();

}
