package cn.webank.wedpr.pir.service;

import cn.webank.wedpr.pir.entity.PirTable;

import java.util.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Service
public class QueryFilterService {

    @Autowired private EntityManager entityManager;

    public List<PirTable> queryFilterSql(String datasetId, String filter) throws Exception {

        String nativeQuery = "SELECT * FROM " + datasetId + " e WHERE e.pirkey LIKE CONCAT(:prefix, '%')";
        Query query = entityManager.createNativeQuery(nativeQuery, PirTable.class);
        query.setParameter("prefix", filter);
        List<PirTable> pirTableList = query.getResultList();

        return pirTableList;
    }

    public List<PirTable> queryMatchSql(String datasetId, String searchId) throws Exception {

        String nativeQuery = "SELECT * FROM " + datasetId + " e WHERE e.pirkey = :exactValue";
        Query query = entityManager.createNativeQuery(nativeQuery, PirTable.class);
        query.setParameter("exactValue", searchId);
        List<PirTable> pirTableList = query.getResultList();

        return pirTableList;
    }
}
