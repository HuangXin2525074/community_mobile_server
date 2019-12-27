package com.mycomany.community.util;

import com.mycomany.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * save user info, replace with session.
 */

@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    // remove user;
    public void clear(){
        users.remove();
    }

}
