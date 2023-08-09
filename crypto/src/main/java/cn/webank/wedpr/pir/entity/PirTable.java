package cn.webank.wedpr.pir.entity;

import lombok.Data;
// import java.sql.Clob;
import javax.persistence.*;
// import java.time.LocalDateTime;

/**
 * @author caryliao
 */
@Data
@Entity
// @Table(name = "t_pir")
@Table(name = "")
public class PirTable {

    @Id
    @Column(unique = true, nullable = false)
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false)
    private String pirkey;

    @Column(nullable = false)
    private String pirvalue;
    // private Clob pirvalue;

    @Column(nullable = false)
    private Long timestamp;
}
