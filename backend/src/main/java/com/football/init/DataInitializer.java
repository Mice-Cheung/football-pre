package com.football.init;

import com.football.entity.*;
import com.football.entity.enums.MatchStatus;
import com.football.entity.enums.OddsSourceType;
import com.football.entity.enums.PlayerPosition;
import com.football.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final LineupRepository lineupRepository;
    private final OddsRepository oddsRepository;
    private final KellyRepository kellyRepository;
    private final TacticsRepository tacticsRepository;

    @Override
    public void run(String... args) {
        if (teamRepository.count() > 0) {
            log.info("Data already initialized, skipping...");
            return;
        }
        log.info("Initializing mock data...");

        // === Teams ===
        Team manCity = createTeam("曼彻斯特城", "Manchester City", "曼城", "https://placehold.co/80x80/6CABDD/white?text=MC", "瓜迪奥拉", "4-3-3", "英超", "England", "#6CABDD");
        Team arsenal = createTeam("阿森纳", "Arsenal", "阿森纳", "https://placehold.co/80x80/EF0107/white?text=ARS", "阿尔特塔", "4-3-3", "英超", "England", "#EF0107");
        Team liverpool = createTeam("利物浦", "Liverpool", "利物浦", "https://placehold.co/80x80/C8102E/white?text=LIV", "斯洛特", "4-3-3", "英超", "England", "#C8102E");
        Team chelsea = createTeam("切尔西", "Chelsea", "切尔西", "https://placehold.co/80x80/034694/white?text=CHE", "波切蒂诺", "4-2-3-1", "英超", "England", "#034694");
        Team manUtd = createTeam("曼彻斯特联", "Manchester United", "曼联", "https://placehold.co/80x80/DA291C/white?text=MU", "滕哈格", "4-2-3-1", "英超", "England", "#DA291C");
        Team realMadrid = createTeam("皇家马德里", "Real Madrid", "皇马", "https://placehold.co/80x80/FEBE10/1A1D23?text=RM", "安切洛蒂", "4-3-3", "西甲", "Spain", "#FEBE10");
        Team barcelona = createTeam("巴塞罗那", "Barcelona", "巴萨", "https://placehold.co/80x80/A50044/white?text=FCB", "哈维", "4-3-3", "西甲", "Spain", "#A50044");
        Team bayern = createTeam("拜仁慕尼黑", "Bayern Munich", "拜仁", "https://placehold.co/80x80/DC052D/white?text=FCB", "图赫尔", "4-2-3-1", "德甲", "Germany", "#DC052D");
        Team dortmund = createTeam("多特蒙德", "Borussia Dortmund", "多特", "https://placehold.co/80x80/FDE100/1A1D23?text=BVB", "泰尔齐奇", "4-3-3", "德甲", "Germany", "#FDE100");
        Team inter = createTeam("国际米兰", "Inter Milan", "国米", "https://placehold.co/80x80/010E80/white?text=INT", "小因扎吉", "3-5-2", "意甲", "Italy", "#010E80");
        Team juventus = createTeam("尤文图斯", "Juventus", "尤文", "https://placehold.co/80x80/000000/white?text=JUV", "阿莱格里", "3-5-2", "意甲", "Italy", "#000000");
        Team psg = createTeam("巴黎圣日耳曼", "Paris Saint-Germain", "巴黎", "https://placehold.co/80x80/004170/white?text=PSG", "恩里克", "4-3-3", "法甲", "France", "#004170");

        teamRepository.saveAll(List.of(manCity, arsenal, liverpool, chelsea, manUtd,
                realMadrid, barcelona, bayern, dortmund, inter, juventus, psg));

        // === Matches (中国竞彩足球赛程) ===
        LocalDateTime now = LocalDateTime.now();
        Match m1 = createMatch("周一001", "英超", manCity, arsenal, now.plusDays(1).withHour(20).withMinute(0), new BigDecimal("-0.50"));
        Match m2 = createMatch("周一002", "西甲", realMadrid, barcelona, now.plusDays(1).withHour(22).withMinute(0), new BigDecimal("0.00"));
        Match m3 = createMatch("周一003", "德甲", bayern, dortmund, now.plusDays(2).withHour(20).withMinute(30), new BigDecimal("-1.00"));
        Match m4 = createMatch("周一004", "意甲", inter, juventus, now.plusDays(2).withHour(21).withMinute(0), new BigDecimal("-0.25"));
        Match m5 = createMatch("周一005", "英超", liverpool, chelsea, now.plusDays(3).withHour(20).withMinute(0), new BigDecimal("-0.75"));
        Match m6 = createMatch("周一006", "法甲", psg, manUtd, now.plusDays(3).withHour(22).withMinute(0), new BigDecimal("-0.50"));
        Match m7 = createMatch("周一007", "英超", manUtd, arsenal, now.plusDays(4).withHour(20).withMinute(0), new BigDecimal("0.25"));
        Match m8 = createMatch("周一008", "西甲", barcelona, realMadrid, now.plusDays(5).withHour(21).withMinute(0), new BigDecimal("0.00"));

        matchRepository.saveAll(List.of(m1, m2, m3, m4, m5, m6, m7, m8));

        // === Lineups ===
        createLineupsForMatch(m1, manCity, "4-3-3", List.of(
                lp("埃德森", 31, PlayerPosition.GK, true, 1),
                lp("沃克", 2, PlayerPosition.DF, true, 2), lp("迪亚斯", 3, PlayerPosition.DF, true, 3),
                lp("阿克", 6, PlayerPosition.DF, true, 4), lp("格瓦迪奥尔", 24, PlayerPosition.DF, true, 5),
                lp("罗德里", 16, PlayerPosition.MF, true, 6), lp("德布劳内", 17, PlayerPosition.MF, true, 7),
                lp("福登", 47, PlayerPosition.MF, true, 8), lp("B席", 20, PlayerPosition.FW, true, 9),
                lp("哈兰德", 9, PlayerPosition.FW, true, 10), lp("多库", 11, PlayerPosition.FW, true, 11)
        ));
        createLineupsForMatch(m1, arsenal, "4-3-3", List.of(
                lp("拉亚", 22, PlayerPosition.GK, true, 1),
                lp("本怀特", 4, PlayerPosition.DF, true, 2), lp("萨利巴", 2, PlayerPosition.DF, true, 3),
                lp("加布里埃尔", 6, PlayerPosition.DF, true, 4), lp("津琴科", 35, PlayerPosition.DF, true, 5),
                lp("赖斯", 41, PlayerPosition.MF, true, 6), lp("厄德高", 8, PlayerPosition.MF, true, 7),
                lp("哈弗茨", 29, PlayerPosition.MF, true, 8), lp("萨卡", 7, PlayerPosition.FW, true, 9),
                lp("热苏斯", 9, PlayerPosition.FW, true, 10), lp("马丁内利", 11, PlayerPosition.FW, true, 11)
        ));

        createLineupsForMatch(m2, realMadrid, "4-3-3", List.of(
                lp("库尔图瓦", 1, PlayerPosition.GK, true, 1),
                lp("卡瓦哈尔", 2, PlayerPosition.DF, true, 2), lp("吕迪格", 22, PlayerPosition.DF, true, 3),
                lp("阿拉巴", 4, PlayerPosition.DF, true, 4), lp("门迪", 23, PlayerPosition.DF, true, 5),
                lp("巴尔韦德", 15, PlayerPosition.MF, true, 6), lp("克罗斯", 8, PlayerPosition.MF, true, 7),
                lp("贝林厄姆", 5, PlayerPosition.MF, true, 8), lp("罗德里戈", 11, PlayerPosition.FW, true, 9),
                lp("维尼修斯", 7, PlayerPosition.FW, true, 10), lp("姆巴佩", 9, PlayerPosition.FW, true, 11)
        ));
        createLineupsForMatch(m2, barcelona, "4-3-3", List.of(
                lp("特尔施特根", 1, PlayerPosition.GK, true, 1),
                lp("孔德", 23, PlayerPosition.DF, true, 2), lp("阿劳霍", 4, PlayerPosition.DF, true, 3),
                lp("克里斯滕森", 15, PlayerPosition.DF, true, 4), lp("巴尔德", 3, PlayerPosition.DF, true, 5),
                lp("佩德里", 8, PlayerPosition.MF, true, 6), lp("京多安", 22, PlayerPosition.MF, true, 7),
                lp("德容", 21, PlayerPosition.MF, true, 8), lp("亚马尔", 27, PlayerPosition.FW, true, 9),
                lp("莱万多夫斯基", 9, PlayerPosition.FW, true, 10), lp("拉菲尼亚", 11, PlayerPosition.FW, true, 11)
        ));

        createLineupsForMatch(m3, bayern, "4-2-3-1", List.of(
                lp("诺伊尔", 1, PlayerPosition.GK, true, 1),
                lp("基米希", 6, PlayerPosition.DF, true, 2), lp("于帕梅卡诺", 2, PlayerPosition.DF, true, 3),
                lp("金玟哉", 3, PlayerPosition.DF, true, 4), lp("戴维斯", 19, PlayerPosition.DF, true, 5),
                lp("格雷茨卡", 8, PlayerPosition.MF, true, 6), lp("莱默尔", 27, PlayerPosition.MF, true, 7),
                lp("萨内", 10, PlayerPosition.MF, true, 8), lp("穆西亚拉", 42, PlayerPosition.FW, true, 9),
                lp("科曼", 11, PlayerPosition.FW, true, 10), lp("凯恩", 9, PlayerPosition.FW, true, 11)
        ));
        createLineupsForMatch(m3, dortmund, "4-3-3", List.of(
                lp("科贝尔", 1, PlayerPosition.GK, true, 1),
                lp("莱尔森", 26, PlayerPosition.DF, true, 2), lp("胡梅尔斯", 15, PlayerPosition.DF, true, 3),
                lp("施洛特贝克", 4, PlayerPosition.DF, true, 4), lp("马特森", 22, PlayerPosition.DF, true, 5),
                lp("詹", 23, PlayerPosition.MF, true, 6), lp("萨比策", 20, PlayerPosition.MF, true, 7),
                lp("布兰特", 19, PlayerPosition.MF, true, 8), lp("马伦", 21, PlayerPosition.FW, true, 9),
                lp("菲尔克鲁格", 14, PlayerPosition.FW, true, 10), lp("阿德耶米", 27, PlayerPosition.FW, true, 11)
        ));

        createLineupsForMatch(m4, inter, "3-5-2", List.of(
                lp("索默", 1, PlayerPosition.GK, true, 1),
                lp("帕瓦尔", 28, PlayerPosition.DF, true, 2), lp("阿切尔比", 15, PlayerPosition.DF, true, 3),
                lp("巴斯托尼", 95, PlayerPosition.DF, true, 4),
                lp("邓弗里斯", 2, PlayerPosition.MF, true, 5), lp("巴雷拉", 23, PlayerPosition.MF, true, 6),
                lp("恰尔汗奥卢", 20, PlayerPosition.MF, true, 7), lp("姆希塔良", 22, PlayerPosition.MF, true, 8),
                lp("迪马尔科", 32, PlayerPosition.MF, true, 9),
                lp("劳塔罗", 10, PlayerPosition.FW, true, 10), lp("图拉姆", 9, PlayerPosition.FW, true, 11)
        ));
        createLineupsForMatch(m4, juventus, "3-5-2", List.of(
                lp("什琴斯尼", 1, PlayerPosition.GK, true, 1),
                lp("加蒂", 4, PlayerPosition.DF, true, 2), lp("布雷默", 3, PlayerPosition.DF, true, 3),
                lp("达尼洛", 6, PlayerPosition.DF, true, 4),
                lp("坎比亚索", 27, PlayerPosition.MF, true, 5), lp("麦肯尼", 16, PlayerPosition.MF, true, 6),
                lp("洛卡特利", 5, PlayerPosition.MF, true, 7), lp("拉比奥", 25, PlayerPosition.MF, true, 8),
                lp("科斯蒂奇", 11, PlayerPosition.MF, true, 9),
                lp("弗拉霍维奇", 9, PlayerPosition.FW, true, 10), lp("基耶萨", 7, PlayerPosition.FW, true, 11)
        ));

        createLineupsForMatch(m5, liverpool, "4-3-3", List.of(
                lp("阿利松", 1, PlayerPosition.GK, true, 1),
                lp("阿诺德", 66, PlayerPosition.DF, true, 2), lp("科纳特", 5, PlayerPosition.DF, true, 3),
                lp("范迪克", 4, PlayerPosition.DF, true, 4), lp("罗伯逊", 26, PlayerPosition.DF, true, 5),
                lp("索博斯洛伊", 8, PlayerPosition.MF, true, 6), lp("麦卡利斯特", 10, PlayerPosition.MF, true, 7),
                lp("琼斯", 17, PlayerPosition.MF, true, 8), lp("萨拉赫", 11, PlayerPosition.FW, true, 9),
                lp("努涅斯", 9, PlayerPosition.FW, true, 10), lp("迪亚斯", 7, PlayerPosition.FW, true, 11)
        ));
        createLineupsForMatch(m5, chelsea, "4-2-3-1", List.of(
                lp("桑切斯", 1, PlayerPosition.GK, true, 1),
                lp("詹姆斯", 24, PlayerPosition.DF, true, 2), lp("迪萨西", 2, PlayerPosition.DF, true, 3),
                lp("科尔维尔", 26, PlayerPosition.DF, true, 4), lp("库库雷利亚", 3, PlayerPosition.DF, true, 5),
                lp("凯塞多", 25, PlayerPosition.MF, true, 6), lp("恩佐", 8, PlayerPosition.MF, true, 7),
                lp("帕尔默", 20, PlayerPosition.MF, true, 8), lp("斯特林", 7, PlayerPosition.FW, true, 9),
                lp("杰克逊", 15, PlayerPosition.FW, true, 10), lp("穆德里克", 10, PlayerPosition.FW, true, 11)
        ));

        // === Odds (中外对比数据) ===
        // Match 1: 曼城 vs 阿森纳
        createOdds(m1, OddsSourceType.NATIONAL_LOTTERY, "中国体育彩票", "1.80", "3.50", "3.90", "1.85", "3.40", "4.00");
        createOdds(m1, OddsSourceType.INTERNATIONAL, "Bet365", "1.85", "3.60", "4.00", "1.90", "3.40", "4.20");
        createOdds(m1, OddsSourceType.INTERNATIONAL, "William Hill", "1.83", "3.50", "4.10", "1.88", "3.45", "4.15");
        createOdds(m1, OddsSourceType.INTERNATIONAL, "Pinnacle", "1.90", "3.65", "3.95", "1.92", "3.55", "4.05");
        createOdds(m1, OddsSourceType.INTERNATIONAL, "Bwin", "1.82", "3.55", "4.05", "1.87", "3.40", "4.15");
        createOdds(m1, OddsSourceType.INTERNATIONAL, "Interwetten", "1.88", "3.58", "3.98", "1.91", "3.50", "4.10");

        // Match 2: 皇马 vs 巴萨
        createOdds(m2, OddsSourceType.NATIONAL_LOTTERY, "中国体育彩票", "2.15", "3.30", "3.00", "2.10", "3.35", "3.10");
        createOdds(m2, OddsSourceType.INTERNATIONAL, "Bet365", "2.20", "3.40", "3.10", "2.15", "3.35", "3.20");
        createOdds(m2, OddsSourceType.INTERNATIONAL, "William Hill", "2.10", "3.30", "3.25", "2.08", "3.40", "3.30");
        createOdds(m2, OddsSourceType.INTERNATIONAL, "Pinnacle", "2.25", "3.45", "3.05", "2.20", "3.40", "3.15");

        // Match 3: 拜仁 vs 多特
        createOdds(m3, OddsSourceType.NATIONAL_LOTTERY, "中国体育彩票", "1.45", "4.50", "5.25", "1.50", "4.30", "5.50");
        createOdds(m3, OddsSourceType.INTERNATIONAL, "Bet365", "1.50", "4.75", "5.50", "1.55", "4.40", "5.80");
        createOdds(m3, OddsSourceType.INTERNATIONAL, "William Hill", "1.44", "4.60", "5.40", "1.48", "4.35", "5.65");
        createOdds(m3, OddsSourceType.INTERNATIONAL, "Pinnacle", "1.52", "4.80", "5.35", "1.56", "4.50", "5.60");

        // Match 4: 国米 vs 尤文
        createOdds(m4, OddsSourceType.NATIONAL_LOTTERY, "中国体育彩票", "1.90", "3.20", "3.85", "1.95", "3.15", "3.90");
        createOdds(m4, OddsSourceType.INTERNATIONAL, "Bet365", "1.95", "3.30", "3.90", "2.00", "3.20", "4.00");
        createOdds(m4, OddsSourceType.INTERNATIONAL, "William Hill", "1.88", "3.25", "3.95", "1.92", "3.18", "4.05");
        createOdds(m4, OddsSourceType.INTERNATIONAL, "Bwin", "1.93", "3.28", "3.88", "1.98", "3.22", "3.95");

        // Match 5: 利物浦 vs 切尔西
        createOdds(m5, OddsSourceType.NATIONAL_LOTTERY, "中国体育彩票", "1.75", "3.60", "4.00", "1.80", "3.50", "4.20");
        createOdds(m5, OddsSourceType.INTERNATIONAL, "Bet365", "1.80", "3.75", "4.10", "1.85", "3.55", "4.30");
        createOdds(m5, OddsSourceType.INTERNATIONAL, "William Hill", "1.73", "3.65", "4.15", "1.78", "3.55", "4.25");
        createOdds(m5, OddsSourceType.INTERNATIONAL, "Pinnacle", "1.82", "3.80", "4.05", "1.87", "3.60", "4.20");

        // === Kelly Index ===
        createKelly(m1, "中国体育彩票", "0.92", "0.88", "0.95");
        createKelly(m1, "Bet365", "0.94", "0.86", "0.93");
        createKelly(m1, "William Hill", "0.91", "0.89", "0.96");
        createKelly(m1, "Pinnacle", "0.95", "0.87", "0.94");

        createKelly(m2, "中国体育彩票", "0.90", "0.92", "0.88");
        createKelly(m2, "Bet365", "0.93", "0.90", "0.87");
        createKelly(m2, "William Hill", "0.91", "0.91", "0.89");
        createKelly(m2, "Pinnacle", "0.94", "0.89", "0.86");

        createKelly(m3, "中国体育彩票", "0.85", "0.95", "0.98");
        createKelly(m3, "Bet365", "0.88", "0.93", "0.96");
        createKelly(m3, "William Hill", "0.84", "0.96", "0.99");
        createKelly(m3, "Pinnacle", "0.89", "0.94", "0.97");

        createKelly(m4, "中国体育彩票", "0.93", "0.89", "0.91");
        createKelly(m4, "Bet365", "0.96", "0.87", "0.90");
        createKelly(m4, "William Hill", "0.92", "0.90", "0.93");

        createKelly(m5, "中国体育彩票", "0.88", "0.91", "0.94");
        createKelly(m5, "Bet365", "0.91", "0.89", "0.92");
        createKelly(m5, "William Hill", "0.87", "0.92", "0.95");
        createKelly(m5, "Pinnacle", "0.92", "0.88", "0.93");

        // === Tactics ===
        createTactics(m1, manCity, "控球渗透", "高位逼抢", "4-3-3",
                "曼城采用标志性的传控打法，通过中场三人组的精准传切配合撕裂对手防线。哈兰德作为支点前锋牵制中后卫，为边路球员创造内切空间。防守端采用高位逼抢策略，一旦丢球立即反抢，压缩对手出球空间。", 94, 62.5, "WWWDW");
        createTactics(m1, arsenal, "快速转换", "区域协防", "4-3-3",
                "阿森纳注重快速的攻防转换，萨卡和马丁内利两翼齐飞，利用速度冲击对手边路。中场赖斯担任防守屏障，厄德高负责组织串联。防守时采用4-4-2落位，边锋回撤参与协防，形成紧凑的防守阵型。", 91, 55.0, "DWWWL");

        createTactics(m2, realMadrid, "快速反击", "链式防守", "4-3-3",
                "皇马擅长利用维尼修斯和姆巴佩的超强个人能力发动闪电反击。中场贝林厄姆的插上进攻极具威胁。防守端采用区域结合盯人的混合防守体系，库尔图瓦把守最后一关。", 93, 52.0, "WWWDW");
        createTactics(m2, barcelona, "Tiki-Taka传控", "高位防守", "4-3-3",
                "巴萨坚持传统传控足球哲学，通过短传渗透逐步推进。佩德里和京多安组成创造力十足的中场双核，莱万作为终结者把握门前机会。防守端强调高位防线，但存在被打身后的风险。", 90, 60.0, "LWWWD");

        createTactics(m3, bayern, "立体轰炸", "整体压迫", "4-2-3-1",
                "拜仁依靠凯恩的支点作用和穆西亚拉的前插威胁对手防线。边路萨内和科曼提供宽度和突破能力。防守端采用整体压迫战术，基米希和格雷茨卡形成双后腰屏障。", 92, 58.0, "WWDLW");
        createTactics(m3, dortmund, "快速转换反击", "中低位防守", "4-3-3",
                "多特蒙德注重快速攻防转换，马伦和阿德耶米的速度是反击利器。菲尔克鲁格作为禁区支点争抢头球。防守端采用中低位密集防守策略，压缩空间反击制胜。", 86, 45.0, "WLWDL");

        createTactics(m4, inter, "边翼卫驱动", "三中卫铁桶", "3-5-2",
                "国际米兰依靠双翼卫邓弗里斯和迪马尔科的往返能力打通两条边路。巴雷拉和恰尔汗奥卢组成技术型中场，劳塔罗和图拉姆双中锋配合默契。三中卫体系防守稳固，身高优势明显。", 90, 54.0, "WWWWW");
        createTactics(m4, juventus, "稳健控制", "密集防守", "3-5-2",
                "尤文图斯采用阿莱格里经典的务实打法，三中卫体系强调防守稳固性。拉比奥和麦肯尼提供中场硬度，弗拉霍维奇作为前场支点。打法偏保守，注重不丢球为先。", 85, 46.0, "DWDLW");

        createTactics(m5, liverpool, "高位压迫反击", "全场紧逼", "4-3-3",
                "利物浦延续克洛普体系的高强度压迫打法，萨拉赫和迪亚斯双翼驱动进攻。索博斯洛伊提供中场创造力，范迪克统领防线。攻防转换速度极快，反击犀利。", 91, 56.0, "WWDLW");
        createTactics(m5, chelsea, "地面传控", "中前场压迫", "4-2-3-1",
                "切尔西依靠帕尔默的组织能力和恩佐的中场覆盖掌控比赛节奏。斯特林和穆德里克提供边路突破，杰克逊担任单箭头。防守端凯塞多担任扫荡后腰，但防线默契度有待提升。", 84, 57.5, "DLWLW");

        log.info("Mock data initialization completed: {} teams, {} matches", teamRepository.count(), matchRepository.count());
    }

    private Team createTeam(String name, String nameEn, String shortName, String logoUrl,
                            String coach, String formation, String league, String country, String color) {
        return Team.builder()
                .name(name).nameEn(nameEn).shortName(shortName).logoUrl(logoUrl)
                .coach(coach).defaultFormation(formation).league(league).country(country).teamColor(color)
                .build();
    }

    private Match createMatch(String matchNo, String league, Team home, Team away,
                               LocalDateTime date, BigDecimal handicap) {
        return Match.builder()
                .matchNo(matchNo).league(league).homeTeam(home).awayTeam(away)
                .matchDate(date).handicap(handicap).status(MatchStatus.PENDING)
                .build();
    }

    private void createLineupsForMatch(Match match, Team team, String formation, List<Lineup> players) {
        for (Lineup p : players) {
            p.setMatch(match);
            p.setTeam(team);
        }
        lineupRepository.saveAll(players);
    }

    private LineupItem lp(String name, int number, PlayerPosition pos, boolean starter, int order) {
        return new LineupItem(name, number, pos, starter, order);
    }

    private record LineupItem(String name, int number, PlayerPosition pos, boolean starter, int order) {}

    private void createLineupsForMatch(Match match, Team team, String formation, List<LineupItem> items) {
        List<Lineup> lineups = items.stream().map(item -> Lineup.builder()
                .match(match).team(team)
                .playerName(item.name).number(item.number)
                .position(item.pos).isStarter(item.starter).sortOrder(item.order)
                .build()).toList();
        lineupRepository.saveAll(lineups);
    }

    private void createOdds(Match match, OddsSourceType sourceType, String company,
                            String home, String draw, String away,
                            String homeInit, String drawInit, String awayInit) {
        oddsRepository.save(Odds.builder()
                .match(match).company(company).sourceType(sourceType)
                .homeWin(new BigDecimal(home)).draw(new BigDecimal(draw)).awayWin(new BigDecimal(away))
                .homeWinInit(new BigDecimal(homeInit)).drawInit(new BigDecimal(drawInit)).awayWinInit(new BigDecimal(awayInit))
                .build());
    }

    private void createKelly(Match match, String company, String home, String draw, String away) {
        kellyRepository.save(KellyIndex.builder()
                .match(match).company(company)
                .homeKelly(new BigDecimal(home)).drawKelly(new BigDecimal(draw)).awayKelly(new BigDecimal(away))
                .build());
    }

    private void createTactics(Match match, Team team, String attack, String defense, String formation,
                               String description, int rating, double possession, String form) {
        tacticsRepository.save(Tactics.builder()
                .match(match).team(team).attackStyle(attack).defenseStyle(defense)
                .formation(formation).description(description)
                .strengthRating(rating).possessionAvg(possession).recentForm(form)
                .build());
    }
}
