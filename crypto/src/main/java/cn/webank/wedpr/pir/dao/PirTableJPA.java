package cn.webank.wedpr.pir.dao;

import cn.webank.wedpr.pir.entity.PirTable;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import java.util.List;

public interface PirTableJPA extends JpaRepository<PirTable, Integer> {

    // List<PirTable> findByPirkeyStartingWith(String idPrefix);

    // @Query(value = "SELECT * FROM :tableName e WHERE e.pirkey LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    // List<PirTable> findByPrefix(@Param("tableName") String tableName, @Param("prefix") String prefix);

}
