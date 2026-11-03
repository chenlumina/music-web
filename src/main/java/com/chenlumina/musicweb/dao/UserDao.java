package com.chenlumina.musicweb.dao;

import com.chenlumina.musicweb.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

import java.util.Optional;

/** 用户查询与写入（开发手册 4.2 Repository 层） */
public class UserDao {

    public Optional<User> findByUsername(EntityManager em, String username) {
        try {
            return Optional.of(em.createQuery("select u from User u where u.username = :u", User.class)
                    .setParameter("u", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(EntityManager em, String username) {
        return em.createQuery("select count(u) from User u where u.username = :u", Long.class)
                .setParameter("u", username)
                .getSingleResult() > 0;
    }

    public void insert(EntityManager em, User user) {
        em.persist(user);
    }
}
