package cn.webank.wedpr.pir.mapper;

import cn.webank.wedpr.pir.entity.PirTable;

import java.util.List;
import org.apache.ibatis.annotations.*;

@Mapper
public interface QueryFilterMapper {
    // @Select("SELECT * FROM \" + datasetId + \" e WHERE e.pirkey LIKE CONCAT(:prefix, '%')")
    @Select("SELECT * FROM ${tablename} WHERE pirkey LIKE CONCAT(#{prefix}, '%')")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "pirkey", column = "pirkey"),
        @Result(property = "pirvalue", column = "pirvalue"),
        @Result(property = "timestamp", column = "timestamp")
    })
    List<PirTable> idFilterTable(@Param("tablename") String datasetId, @Param("prefix") String filter);

    @Select("SELECT * FROM ${tablename} WHERE pirkey = #{exactvalue}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "pirkey", column = "pirkey"),
        @Result(property = "pirvalue", column = "pirvalue"),
        @Result(property = "timestamp", column = "timestamp")
    })
    List<PirTable> idObfuscationTable(@Param("tablename") String datasetId, @Param("exactvalue") String searchId);
}
