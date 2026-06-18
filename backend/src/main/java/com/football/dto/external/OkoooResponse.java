package com.football.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 澳客网 (okooo.com) API 响应结构
 * 接口: /soccer/match/odds/ajax/
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OkoooResponse {

    @JsonProperty("code")
    private Integer code;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("data")
    private List<OddsEntry> data;

    /**
     * 单条赔率记录
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OddsEntry {
        /** 公司名称 */
        @JsonProperty("companyname")
        private String companyName;

        /** 公司ID */
        @JsonProperty("companyid")
        private String companyId;

        /** 主胜赔率 */
        @JsonProperty("h")
        private String homeWin;

        /** 平局赔率 */
        @JsonProperty("d")
        private String draw;

        /** 客胜赔率 */
        @JsonProperty("a")
        private String awayWin;

        /** 初盘主胜 */
        @JsonProperty("hI")
        private String homeWinInit;

        /** 初盘平局 */
        @JsonProperty("dI")
        private String drawInit;

        /** 初盘客胜 */
        @JsonProperty("aI")
        private String awayWinInit;

        /** 赔率变化趋势: up/down/same */
        @JsonProperty("hT")
        private String homeTrend;

        @JsonProperty("dT")
        private String drawTrend;

        @JsonProperty("aT")
        private String awayTrend;

        /** 凯利指数-主 */
        @JsonProperty("kellyH")
        private String kellyHome;

        /** 凯利指数-平 */
        @JsonProperty("kellyD")
        private String kellyDraw;

        /** 凯利指数-客 */
        @JsonProperty("kellyA")
        private String kellyAway;

        /** 返还率 */
        @JsonProperty("payout")
        private String payout;
    }
}
