package com.football.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 竞彩网 (sporttery.cn) API 响应结构
 * 接口: /gateway/jc/football/getMatchCalculatorV1.qry
 *
 * <p>真实 API 响应为嵌套结构: matchInfoList[].businessDate -> subMatchList[] -> 实际比赛
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SportteryResponse {

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("value")
    private Value value;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("matchInfoList")
        private List<BusinessDateGroup> matchInfoList;

        @JsonProperty("matchResultList")
        private List<MatchResult> matchResultList;
    }

    /**
     * 按 businessDate 分组的比赛数据
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusinessDateGroup {
        /** 竞彩业务日期 */
        @JsonProperty("businessDate")
        private String businessDate;

        /** 该日期下的比赛列表 */
        @JsonProperty("subMatchList")
        private List<MatchInfo> subMatchList;
    }

    /**
     * 比赛基本信息（含赔率）
     * 实际字段名来自 getMatchCalculatorV1 响应
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchInfo {
        /** 赛事编号，如 "周四025" */
        @JsonProperty("matchNumStr")
        private String matchNumStr;

        /** 赛事数字编号 */
        @JsonProperty("matchNum")
        private Integer matchNum;

        /** 内部赛事ID */
        @JsonProperty("matchId")
        private Long matchId;

        /** 联赛简称，如 "世界杯" */
        @JsonProperty("leagueAbbName")
        private String leagueAbbName;

        /** 联赛全称 */
        @JsonProperty("leagueAllName")
        private String leagueAllName;

        /** 主队简称 */
        @JsonProperty("homeTeamAbbName")
        private String homeTeamAbbName;

        /** 主队全称 */
        @JsonProperty("homeTeamAllName")
        private String homeTeamAllName;

        /** 客队简称 */
        @JsonProperty("awayTeamAbbName")
        private String awayTeamAbbName;

        /** 客队全称 */
        @JsonProperty("awayTeamAllName")
        private String awayTeamAllName;

        /** 比赛日期 yyyy-MM-dd */
        @JsonProperty("matchDate")
        private String matchDate;

        /** 比赛时间 HH:mm:ss */
        @JsonProperty("matchTime")
        private String matchTime;

        /** 赛事状态 (Selling=销售中, StopSelling=停售) */
        @JsonProperty("matchStatus")
        private String matchStatus;

        // ===== 胜平负赔率 (had) =====
        @JsonProperty("had")
        private OddsData had;

        // ===== 让球胜平负赔率 (hhad) =====
        @JsonProperty("hhad")
        private OddsData hhad;

        /** 业务日期（从父级注入） */
        private String businessDate;
    }

    /**
     * 赔率数据（嵌套在 had/hhad 中）
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OddsData {
        /** 主胜/让球主胜赔率 */
        @JsonProperty("h")
        private String h;

        /** 平局/让球平赔率 */
        @JsonProperty("d")
        private String d;

        /** 客胜/让球客胜赔率 */
        @JsonProperty("a")
        private String a;

        /** 让球数（仅 hhad 有） */
        @JsonProperty("goalLine")
        private String goalLine;

        /** 让球数值 */
        @JsonProperty("goalLineValue")
        private String goalLineValue;

        /** 更新时间 */
        @JsonProperty("updateDate")
        private String updateDate;

        @JsonProperty("updateTime")
        private String updateTime;
    }

    /**
     * 比赛结果信息（用于 getMatchResultV1 端点）
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchResult {
        @JsonProperty("matchNumStr")
        private String matchNumStr;

        @JsonProperty("halfScore")
        private String halfScore;

        @JsonProperty("finalScore")
        private String finalScore;

        @JsonProperty("spfResult")
        private String spfResult;

        @JsonProperty("rqspfResult")
        private String rqspfResult;
    }
}
