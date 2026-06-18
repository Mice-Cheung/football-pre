package com.football.controller;

import com.football.dto.*;
import com.football.service.OddsService;
import com.football.service.OddsSyncScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/odds")
@RequiredArgsConstructor
public class OddsController {

    private final OddsService oddsService;
    private final OddsSyncScheduler syncScheduler;

    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<OddsDTO>> getOddsByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(oddsService.getOddsByMatch(matchId));
    }

    @GetMapping("/match/{matchId}/comparison")
    public ResponseEntity<OddsComparisonDTO> getOddsComparison(@PathVariable Long matchId) {
        return ResponseEntity.ok(oddsService.getOddsComparison(matchId));
    }

    @GetMapping("/kelly/match/{matchId}")
    public ResponseEntity<List<KellyDTO>> getKellyByMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(oddsService.getKellyByMatch(matchId));
    }

    /**
     * 赔率追溯 — 完整历史变化 + 显著变化告警
     */
    @GetMapping("/match/{matchId}/traceability")
    public ResponseEntity<OddsTraceabilityDTO> getOddsTraceability(
            @PathVariable Long matchId,
            @RequestParam(required = false) String sourceType) {
        if (sourceType != null && !sourceType.isBlank()) {
            return ResponseEntity.ok(oddsService.getOddsTraceabilityByType(matchId, sourceType));
        }
        return ResponseEntity.ok(oddsService.getOddsTraceability(matchId));
    }

    /**
     * 数据同步状态监控接口
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        OddsSyncScheduler.SyncStatus status = syncScheduler.getStatus();
        return ResponseEntity.ok(Map.of(
                "syncEnabled", status.enabled(),
                "totalRounds", status.totalRounds(),
                "lastSyncTime", status.lastSyncTime(),
                "lastResult", status.lastResult()
        ));
    }
}
