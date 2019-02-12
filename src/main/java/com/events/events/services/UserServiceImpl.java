package com.events.events.services;

import com.events.events.error.*;
import com.events.events.models.Event;
import com.events.events.models.Friend;
import com.events.events.models.User;
import com.events.events.repository.FriendRepository;
import com.events.events.repository.UserRepository;
import javassist.tools.web.BadHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.social.ExpiredAuthorizationException;
import org.springframework.social.InvalidAuthorizationException;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isPresent()){
            throw new UsernameNotFoundException("There is no user with the username: "+ username);
        }

        return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), Collections.emptyList());
    }

    @Transactional
    public User saveUser(User user){
        //check if the username exists
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new DuplicateCreationException("User with the username: "+user.getUsername()+" already exists");
        }else if(user.getPassword().trim().length() < 5){
            throw new AuthenticationException("New Password must be more than 5");
        }
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User getUserById(int id) {
        return verifyAndReturnUser(id);
    }

    @Override
    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(int userId) {
        User user = verifyAndReturnUser(userId);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword, String username) {
        User user = verifyAndReturnUser(username);
        if(!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())){
            throw new AuthenticationException("Password does not match current password");
        } else if(bCryptPasswordEncoder.matches(newPassword, user.getPassword())){
            throw new AuthenticationException("New Password can't be the same as the old password");
        } else if(newPassword.trim().length() < 5){
            throw new AuthenticationException("New Password must be more than 5");
        }
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        User user = verifyAndReturnUser(userId);
        User friend = verifyAndReturnUser(friendId);
        // create the friend
        if(user.equals(friend)){
            throw new IllegalFriendActionException("Can't add self as a friend");
        }
        Friend newFriend = new Friend(user, friend);
        // add the friend to the user
        friendRepository.save(newFriend);
    }

    @Override
    public List<User> getAllFriends(int userId) {
        User user = verifyAndReturnUser(userId);
        List<User> friends = new ArrayList<>();
        for(Friend friend : user.getFriends()){
            friends.add(friend.getFriend());
        }
        return friends;
    }

    @Override
    public List<User> getAllFollowers(int userId){
        User user = verifyAndReturnUser(userId);
        List<Friend> friendList = friendRepository.getAllFollowers(user.getUserId());
        List<User> followers = new ArrayList<>();
        if(friendList.isEmpty()){
            throw new EmptyListException("There are no followers for the user: "+userId);
        }
        for(Friend friend : friendList){
            followers.add(friend.getOwner());
        }
        return followers;
    }

    @Override
    public void acceptFollowRequest(int userId, Map<String, Object> userInput) {
        //check that the map has the data we are looking for
        if(!userInput.containsKey("requesterId")){
            throw new BadRequestException("Please provide a requesterId");
        }else if(!userInput.containsKey("acceptValue")){
            throw new BadRequestException("Please provide a acceptValue");
        }
        // check that the user and the
        verifyAndReturnUser(userId);
        int requesterId = (Integer) userInput.get("requesterId");
        verifyAndReturnUser(requesterId);

        friendRepository.findById(new Friend.Key(userId, requesterId));

    }

    @Override
    @Transactional
    public List<Event> listEventsByUser(int userId) {
        User user = verifyAndReturnUser(userId);
        if(user.getCreatedEvents().isEmpty()){
            throw new EmptyListException("There are no events for the user: "+ userId);
        }
        return user.getCreatedEvents();
    }

    @Override
    @Transactional
    public List<Event> listEventsUserIsAttending(int userId) {
        User user = verifyAndReturnUser(userId);
        if(user.getAttending().isEmpty()){
            throw new EmptyListException("The user: "+userId+" is not attending any events");
        }
        return user.getAttending();
    }

    @Override
    public void setFacebookIdAndToken(String token, String username) {
        //check that the token received is valid and belongs to the user
        try {
            Facebook facebook = new FacebookTemplate(token);
            String[] fields = {"id", "email", "first_name", "last_name"};
            org.springframework.social.facebook.api.User facebookUser = facebook.fetchObject("me", org.springframework.social.facebook.api.User.class, fields);
            // get the user and set the token and facebook Id
            User user = userRepository.findByUsername(username).get();
            user.setFacebookId(facebookUser.getId());
            user.setAccessToken(token);
        }catch(InvalidAuthorizationException e){
            throw new AuthenticationException("Facebook token provided is invalid");
        }catch(ExpiredAuthorizationException e){
            throw new AuthenticationException("Facebook token provided is expired");
        }
        System.out.println("Got here");

    }

    //Helper methods to get users below here

    private User verifyAndReturnUser(int userId){
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent()){
            throw new NotFoundException("User with id: "+userId+" not found");
        }
        return user.get();
    }

    private User verifyAndReturnUser(String username){
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()){
            throw new NotFoundException("User with the username: "+username+" not found");
        }
        return user.get();
    }

}
