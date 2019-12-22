package com.mycomany.community.dao;


import org.springframework.stereotype.Repository;

@Repository
public class AlphaDaoHibernmatelmpl implements AlphaDao{

    @Override
    public String select() {
        return "Hibernate";
    }
}
