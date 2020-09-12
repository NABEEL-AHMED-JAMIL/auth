package com.barco.auth.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Service
@Transactional
public class QueryUtil {

    public Logger logger = LogManager.getLogger(QueryUtil.class);

    @Autowired
    private EntityManager entityManager;
}
