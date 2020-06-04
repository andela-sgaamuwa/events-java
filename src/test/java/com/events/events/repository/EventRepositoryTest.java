package com.events.events.repository;

import com.events.events.EventsApplication;
import com.events.events.config.database.JPAConfiguration;
import com.events.events.models.Event;
import com.events.events.models.EventStatus;
import com.events.events.models.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {EventsApplication.class, JPAConfiguration.class})
public class EventRepositoryTest {

    @Autowired
    public TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private User samuel = new User("martha", "kyozira", "mkyozira", "pass123", "mkyozira@email.com");
    private Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), samuel);
    private User male = new User("michael", "male", "mmale", "pass123", "mmale@email.com");
    private User bruce = new User("bruce", "bigirwenyka", "bbigirwenkya", "pass123", "bbigirwenkya@email.com");

    @Before
    public void setup(){
        entityManager.persist(samuel);
        entityManager.persist(male);
        entityManager.persist(bruce);
    }

    @Test
    public void canAddUsersToAnEvent(){

        Set<User> participants = new HashSet<>();
        participants.add(male);
        participants.add(bruce);

        cinemaMovie.setParticipants(participants);
        // save using the repository
        Event savedEvent = eventRepository.save(cinemaMovie);

        Event returned = entityManager.find(Event.class, savedEvent.getEventId());

        Assert.assertTrue(returned.getParticipants().contains(male));
        Assert.assertTrue(returned.getParticipants().contains(bruce));
    }

    @Test
    public void canGetEventsByDate(){
        //save an event
        entityManager.persist(cinemaMovie);
        // retrieve it by date
        List<Event> events = eventRepository.findByDate(LocalDate.now());

        Assert.assertEquals(events.size(), 1);
        Assert.assertEquals(events.get(0).getTitle(), "Movie");
    }

    @Test
    public void canGetEventsBetweenDates(){
        Event beach = new Event("beach", "Entebbe", LocalDate.now().plusDays(2), samuel);
        Event jumping = new Event("jumping", "Jinja", LocalDate.now().plusDays(3), samuel);
        Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), samuel);

        entityManager.persist(beach);
        entityManager.persist(jumping);
        entityManager.persist(cinemaMovie);

        Assert.assertEquals(eventRepository.findByDateBetween(LocalDate.now(), LocalDate.now().plusDays(2)).size(), 4);
        Assert.assertEquals(eventRepository.findByDateBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)).size(), 2);
    }

    @Test
    public void canGetEventsAfterCertainDate(){
        Event beach = new Event("beach", "Entebbe", LocalDate.now().plusDays(2), samuel);
        Event jumping = new Event("jumping", "Jinja", LocalDate.now().plusDays(3), samuel);
        Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), samuel);

        entityManager.persist(beach);
        entityManager.persist(jumping);
        entityManager.persist(cinemaMovie);

        Assert.assertEquals(eventRepository.findByDateGreaterThan(LocalDate.now().plusDays(1)).size(), 8);
        Assert.assertEquals(eventRepository.findByDateGreaterThan(LocalDate.now().minusDays(1)).size(), 10);
    }

    @Test
    public void canGetEventsBeforeCertainDate(){
        Event beach = new Event("beach", "Entebbe", LocalDate.now().plusDays(2), samuel);
        Event jumping = new Event("jumping", "Mauritious", LocalDate.now().plusDays(3), samuel);
        Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), samuel);

        entityManager.persist(beach);
        entityManager.persist(jumping);
        entityManager.persist(cinemaMovie);

        Assert.assertEquals(eventRepository.findByDateLessThan(LocalDate.now().plusDays(1)).size(), 1);
        Assert.assertEquals(eventRepository.findByDateLessThan(LocalDate.now().minusDays(1)).size(), 0);
        Assert.assertEquals(eventRepository.findByDateLessThan(LocalDate.now().plusDays(4)).size(), 6);
    }

    @Test
    public void canFindEventsByStatus(){
        Event beach = new Event("beach", "Entebbe", LocalDate.now().plusDays(2), samuel);
        Event jumping = new Event("jumping", "Jinja", LocalDate.now().plusDays(3), samuel);
        Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), samuel);

        beach.setEventStatus(EventStatus.CANCELLED);

        entityManager.persist(beach);
        entityManager.persist(jumping);
        entityManager.persist(cinemaMovie);

        Assert.assertEquals(eventRepository.findByEventStatus(EventStatus.OPEN).size(), 9);
        Assert.assertEquals(eventRepository.findByEventStatus(EventStatus.CANCELLED).size(), 1);
        Assert.assertEquals(eventRepository.findByEventStatus(EventStatus.CANCELLED).get(0), beach);
    }

    @Test
    public void canFindAllEventsByUsersFriends(){
        Event beach = new Event("trip", "Namayiba", LocalDate.now().plusDays(2), samuel);
        Event jumping = new Event("jumping", "Jinja", LocalDate.now().plusDays(3), male);
        Event nightDancing = new Event("nightDancing", "mukono", LocalDate.now().plusDays(4), samuel);
        Event quidditch = new Event("quidditch", "hogwarts", LocalDate.now().plusDays(5), male);
        Event cinemaMovie = new Event("Movie", "Acacia Mall", LocalDate.now(), bruce);

        entityManager.persist(beach);
        entityManager.persist(jumping);
        entityManager.persist(cinemaMovie);
        entityManager.persist(nightDancing);
        entityManager.persist(quidditch);

        Assert.assertEquals(eventRepository.findAll().size(), 12);
        Assert.assertEquals(eventRepository.findAllEventsByFriends(Arrays.asList(samuel.getUserId(), male.getUserId())).size(), 4);
    }

}
