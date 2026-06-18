package com.football.repository;

import com.football.entity.OddsHistory;
import com.football.entity.enums.OddsSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OddsHistoryRepository extends JpaRepository<OddsHistory, Long> {

    /** 查询某场比赛某个机构的所有历史记录（按时间升序） */
    List<OddsHistory> findByMatchIdAndCompanyOrderByRecordedAtAsc(Long matchId, String company);

    /** 查询某场比赛所有机构的历史记录 */
    List<OddsHistory> findByMatchIdOrderByRecordedAtAsc(Long matchId);

    /** 查询某场比赛某个机构的最近一条记录 */
    Optional<OddsHistory> findTopByMatchIdAndCompanyOrderByRecordedAtDesc(Long matchId, String company);

    /** 查询比赛+来源类型的历史记录 */
    @Query("SELECT h FROM OddsHistory h WHERE h.match.id = :matchId AND h.sourceType = :sourceType ORDER BY h.recordedAt ASC")
    List<OddsHistory> findByMatchIdAndSourceType(@Param("matchId") Long matchId,
                                                  @Param("sourceType") OddsSourceType sourceType);

    /** 查询指定时间范围内变化幅度超过阈值的记录 */
    @Query("SELECT h FROM OddsHistory h WHERE h.match.id = :matchId " +
           "AND (ABS(h.homeChange) >= :threshold OR ABS(h.drawChange) >= :threshold OR ABS(h.awayChange) >= :threshold) " +
           "ORDER BY h.recordedAt ASC")
    List<OddsHistory> findSignificantChanges(@Param("matchId") Long matchId,
                                              @Param("threshold") java.math.BigDecimal threshold);
}
