package com.kaluzny.demo.service;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.domain.AutomobileRepository;
import com.kaluzny.demo.exception.AutoWasDeletedException;
import com.kaluzny.demo.exception.ThereIsNoSuchAutoException;
import jakarta.jms.Topic;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AutomobileServiceImpl implements AutomobileService {


    private AutomobileRepository automobileRepository;
    private JmsTemplate jmsTemplate;

    public AutomobileServiceImpl(AutomobileRepository automobileRepository, JmsTemplate jmsTemplate){
        this.automobileRepository = automobileRepository;
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public ResponseEntity<Automobile> pushMessage(Automobile automobile, String topicName) {
        try {
            Topic autoTopic = Objects.requireNonNull(jmsTemplate
                    .getConnectionFactory()).createConnection().createSession().createTopic(topicName);
  //          Automobile savedAutomobile = createAuto(automobile);
                      jmsTemplate.convertAndSend(autoTopic, automobile);
            return new ResponseEntity<>(automobile, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Automobile createAuto(Automobile auto) {
        return automobileRepository.save(auto);
    }

    @Override
    public List<Automobile> getAllAutos() {
        return automobileRepository.findAll();
    }

    @Override
    public Automobile getAutoById(long id) {
        return automobileRepository.findById(id).orElseThrow(()-> new EntityNotFoundException());
    }

    @Override
    public List<String> getAutosByName() {
        List<Automobile> collection = automobileRepository.findAll();
        List<String> foundAutos = collection.stream()
                .map(Automobile::getName)
                .sorted()
                .collect(Collectors.toList());
        return foundAutos;
    }

    @Override
    public Collection<Automobile> getAutosByColor(String color) {
        return automobileRepository.findByColor(color);
    }

    @Override
    public Collection<Automobile> getAutosByNameAndColor(String color, String name) {
        return automobileRepository.findByNameAndColor(name, color);
    }

    @Override
    public Collection<Automobile> getByColorStartsWith(String colorStart, Pageable page) {
        return automobileRepository.findByColorStartsWith(colorStart, page);
    }

    @Override
    public List<Automobile> getAllAutosByName(String name) {
        return automobileRepository.findByName(name);
    }

    @Override
    public Automobile updAuto(long id, Automobile auto) {
        Automobile updatedAutomobile = automobileRepository.findById(id)
                .map(entity -> {
                    entity.checkColor(auto);
                    entity.setName(auto.getName());
                    entity.setColor(auto.getColor());
                    entity.setUpdateDate(auto.getUpdateDate());
                    if (entity.getDeleted()) {
                        throw new AutoWasDeletedException();
                    }
                    return automobileRepository.save(entity);
                })
                //.orElseThrow(() -> new EntityNotFoundException("Automobile not found with id = " + id));
                .orElseThrow(ThereIsNoSuchAutoException::new);
        return updatedAutomobile;
    }

    @Override
    public void deleteAutoById(long id) {
        Automobile deletedAutomobile = automobileRepository.findById(id)
                .orElseThrow(ThereIsNoSuchAutoException::new);
        deletedAutomobile.setDeleted(Boolean.TRUE);
        automobileRepository.save(deletedAutomobile);
    }

    @Override
    public void deleteAllAutos() {
        automobileRepository.deleteAll();
    }
}
