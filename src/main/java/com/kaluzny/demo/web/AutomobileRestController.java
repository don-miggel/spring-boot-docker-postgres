package com.kaluzny.demo.web;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.domain.AutomobileRepository;
import com.kaluzny.demo.exception.AutoWasDeletedException;
import com.kaluzny.demo.exception.ThereIsNoSuchAutoException;
import com.kaluzny.demo.service.AutomobileService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.annotation.PostConstruct;
import jakarta.jms.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class AutomobileRestController implements AutomobileResource, AutomobileOpenApi {

 //   private final AutomobileRepository repository;
 //   private final JmsTemplate jmsTemplate;
    private final AutomobileService automobileService;

    public static double getTiming(Instant start, Instant end) {
        return Duration.between(start, end).toMillis();
    }

    @Transactional
    @PostConstruct
    public void init() {
//        repository.save(new Automobile(1L, "Ford", "Green", LocalDateTime.now(), LocalDateTime.now(), true, false));
        automobileService.createAuto(new Automobile(1L, "Ford", "Green",
                LocalDateTime.now(), LocalDateTime.now(), true, false));
    }

    @PostMapping("/automobiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    //@RolesAllowed("ADMIN")
    public Automobile saveAutomobile(@Valid @RequestBody Automobile automobile) {
        log.info("saveAutomobile() - start: automobile = {}", automobile);
        Automobile savedAutomobile = automobileService.createAuto(automobile);
        log.info("saveAutomobile() - end: savedAutomobile = {}", savedAutomobile.getId());
        return savedAutomobile;
    }

    @GetMapping("/automobiles")
    @ResponseStatus(HttpStatus.OK)
    //@Cacheable(value = "automobile", sync = true)
 //   @PreAuthorize("hasRole('USER')")
    public Collection<Automobile> getAllAutomobiles() {
        log.info("getAllAutomobiles() - start");
        Collection<Automobile> collection = automobileService.getAllAutos();
        log.info("getAllAutomobiles() - end");
        return collection;
    }

    @GetMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    //@Cacheable(value = "automobile", sync = true)
    //TODO: We do not have PERSON on the user map
    @PreAuthorize("hasRole('PERSON')")
    public Automobile getAutomobileById(@PathVariable Long id) {
        log.info("getAutomobileById() - start: id = {}", id);
        /*
        Automobile receivedAutomobile = repository.findById(id)
                //.orElseThrow(() -> new EntityNotFoundException("Automobile not found with id = " + id));
                .orElseThrow(ThereIsNoSuchAutoException::new);
        if (receivedAutomobile.getDeleted()) {
            throw new AutoWasDeletedException();
        }

         */
        Automobile receivedAutomobile = automobileService.getAutoById(id);
        log.info("getAutomobileById() - end: Automobile = {}", receivedAutomobile.getId());
        return receivedAutomobile;
    }

    @Hidden
    @GetMapping(value = "/automobiles", params = {"name"})
    @ResponseStatus(HttpStatus.OK)
    public Collection<Automobile> findAutomobileByName(@RequestParam(value = "name") String name) {
        log.info("findAutomobileByName() - start: name = {}", name);
        Collection<Automobile> collection = automobileService.getAllAutosByName(name);
        log.info("findAutomobileByName() - end: collection = {}", collection);
        return collection;
    }

    @PutMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.OK)
    //@CachePut(value = "automobile", key = "#id")
    public Automobile refreshAutomobile(@PathVariable Long id, @RequestBody Automobile automobile) {
        log.info("refreshAutomobile() - start: id = {}, automobile = {}", id, automobile);
        /*
        Automobile updatedAutomobile = repository.findById(id)
                .map(entity -> {
                    entity.checkColor(automobile);
                    entity.setName(automobile.getName());
                    entity.setColor(automobile.getColor());
                    entity.setUpdateDate(automobile.getUpdateDate());
                    if (entity.getDeleted()) {
                        throw new AutoWasDeletedException();
                    }
                    return repository.save(entity);
                })
                //.orElseThrow(() -> new EntityNotFoundException("Automobile not found with id = " + id));
                .orElseThrow(ThereIsNoSuchAutoException::new);

         */
        Automobile updatedAutomobile = automobileService.updAuto(id, automobile);
        log.info("refreshAutomobile() - end: updatedAutomobile = {}", updatedAutomobile);
        return updatedAutomobile;
    }

    @DeleteMapping("/automobiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @CacheEvict(value = "automobile", key = "#id")
    public String removeAutomobileById(@PathVariable Long id) {
        log.info("removeAutomobileById() - start: id = {}", id);
        /*
        Automobile deletedAutomobile = repository.findById(id)
                .orElseThrow(ThereIsNoSuchAutoException::new);
        deletedAutomobile.setDeleted(Boolean.TRUE);
        repository.save(deletedAutomobile);

         */
        automobileService.deleteAutoById(id);
        log.info("removeAutomobileById() - end: id = {}", id);
        return "Deleted";
    }

    @Hidden
    @DeleteMapping("/automobiles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAllAutomobiles() {
        log.info("removeAllAutomobiles() - start");
        automobileService.deleteAllAutos();
        log.info("removeAllAutomobiles() - end");
    }

    @GetMapping(value = "/automobiles", params = {"color"})
    @ResponseStatus(HttpStatus.OK)
    public Collection<Automobile> findAutomobileByColor(
            @Parameter(description = "Name of the Automobile to be obtained. Cannot be empty.", required = true)
            @RequestParam(value = "color") String color) {
        Instant start = Instant.now();
        log.info("findAutomobileByColor() - start: time = {}", start);
        log.info("findAutomobileByColor() - start: color = {}", color);
        Collection<Automobile> collection = automobileService.getAutosByColor(color);
        Instant end = Instant.now();
        log.info("findAutomobileByColor() - end: milliseconds = {}", getTiming(start, end));
        log.info("findAutomobileByColor() - end: collection = {}", collection);
        return collection;
    }

    @GetMapping(value = "/automobiles", params = {"name", "color"})
    @ResponseStatus(HttpStatus.OK)
    public Collection<Automobile> findAutomobileByNameAndColor(
            @Parameter(description = "Name of the Automobile to be obtained. Cannot be empty.", required = true)
            @RequestParam(value = "name") String name, @RequestParam(value = "color") String color) {
        log.info("findAutomobileByNameAndColor() - start: name = {}, color = {}", name, color);
        Collection<Automobile> collection = automobileService.getAutosByNameAndColor(color, name);
        log.info("findAutomobileByNameAndColor() - end: collection = {}", collection);
        return collection;
    }

    @GetMapping(value = "/automobiles", params = {"colorStartsWith"})
    @ResponseStatus(HttpStatus.OK)
    public Collection<Automobile> findAutomobileByColorStartsWith(
            @RequestParam(value = "colorStartsWith") String colorStartsWith,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size) {
        log.info("findAutomobileByColorStartsWith() - start: color = {}", colorStartsWith);
        Collection<Automobile> collection = automobileService
                .getByColorStartsWith(colorStartsWith, PageRequest.of(page, size, Sort.by("color")));
        log.info("findAutomobileByColorStartsWith() - end: collection = {}", collection);
        return collection;
    }

    @GetMapping("/automobiles-names")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAllAutomobilesByName() {
        log.info("getAllAutomobilesByName() - start");
        /*
        List<Automobile> autosByName = repository.findAll();
        List<String> collectionName = collection.stream()
                .map(Automobile::getName)
                .sorted()
                .collect(Collectors.toList());

         */
        List<String> autosByName = automobileService.getAutosByName();
        log.info("getAllAutomobilesByName() - end");
        return autosByName;
    }


    @PostMapping("/message")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Automobile> pushMessage(@RequestBody Automobile automobile) {

        log.info("\u001B[32m" + "Sending Automobile with id: " + automobile.getId() + "\u001B[0m");
        Automobile newAuto = automobileService.createAuto(automobile);
        return automobileService.pushMessage(newAuto, "AutoTopic");

        /*
        try {
            Topic autoTopic = Objects.requireNonNull(jmsTemplate
                    .getConnectionFactory()).createConnection().createSession().createTopic("AutoTopic");
            Automobile savedAutomobile = repository.save(automobile);
            log.info("\u001B[32m" + "Sending Automobile with id: " + savedAutomobile.getId() + "\u001B[0m");
            jmsTemplate.convertAndSend(autoTopic, savedAutomobile);
            return new ResponseEntity<>(savedAutomobile, HttpStatus.OK);
        } catch (Exception exception) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

         */

    }

    @PostMapping("/auto/{color}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Collection<Automobile>> pushAutosByColor(@PathVariable String color){

        Collection<Automobile> autosByColor = automobileService.getAutosByColor(color);

        autosByColor.stream().forEach(
                auto -> {
                    automobileService.pushMessage(auto, "Color");
                    log.info("\u001B[32m" + "Sending Automobile with id: " + auto.getId() + "\u001B[0m");
                }
        );
        return new ResponseEntity<>(autosByColor, HttpStatus.OK);
    }
}
