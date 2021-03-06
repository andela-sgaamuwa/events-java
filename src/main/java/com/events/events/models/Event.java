package com.events.events.models;

import com.events.events.models.serializers.CustomURLSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.RepresentationModel;;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
        allowGetters = true)
@Table(name = "events")
public class Event extends RepresentationModel<Event> {

    @Id
    @GeneratedValue
    private int eventId;

    @Column(nullable = false)
    @NotEmpty
    private String title;

    private String location;

    private String description;

    private URL link;

    @JsonSerialize(using = CustomURLSerializer.class)
    private String imageKey;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Currency cost;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"createdEvents", "attending", "createdAt", "updatedAt", "enabled", "imageKey"})
    private User creator;

    @ManyToMany(mappedBy = "attending")
    @JsonIgnoreProperties({"createdEvents", "attending", "createdAt", "updatedAt", "enabled", "imageKey"})
    private Set<User> participants;

    @ManyToMany(mappedBy = "invites")
    @JsonIgnoreProperties({"createdEvents", "attending", "createdAt", "updatedAt", "enabled", "imageKey"})
    private Set<User> invitees;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDate createdAt;

    @Column(nullable = false)
    @LastModifiedDate
    private LocalDate updatedAt;

    private EventStatus eventStatus = EventStatus.OPEN;

    private EventPermission eventPermission = EventPermission.PUBLIC;

    public Event(){
        this.participants = new HashSet<>();
    }

    public Event(String title, String location, LocalDateTime startTime, LocalDateTime endTime, User creator) {
        this();
        this.title = title;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creator = creator;
    }

    public Event(String title, String location, String description, LocalDateTime startTime, LocalDateTime endTime, User creator) {
        this(title, location, startTime, endTime, creator);
        this.description = description;
    }

    public Event(String title, String location, String description, String link, LocalDateTime startTime, LocalDateTime endTime, User creator) throws MalformedURLException {
        this(title, location, description, startTime, endTime, creator);
        this.link = new URL(link);
    }

    public Event(String title, String location, URL link, LocalDateTime startTime, LocalDateTime endTime, User creator, Set<User> participants) {
        this.title = title;
        this.location = location;
        this.link = link;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creator = creator;
        this.participants = participants;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public URL getLink() {
        return link;
    }

    public void setLink(URL link) {
        this.link = link;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public Set<User> getInvitees() {
        return invitees;
    }

    public void setInvitees(Set<User> invitees) {
        this.invitees = invitees;
    }

    public LocalDate getCreatedAt(){
        return createdAt;
    }

    public LocalDate getUpdatedAt(){
        return updatedAt;
    }

    public Currency getCost() {
        return cost;
    }

    public void setCost(Currency cost) {
        this.cost = cost;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public EventPermission getEventPermission() {
        return eventPermission;
    }

    public void setEventPermission(EventPermission eventPermission) {
        this.eventPermission = eventPermission;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(!(obj instanceof Event)){
            return false;
        }

        return Integer.compare(eventId, ((Event) obj).eventId) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
