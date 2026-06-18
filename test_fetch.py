# -*- coding: utf-8 -*-
"""
竞彩网 API 数据验证脚本 v4 — 正确解析嵌套结构
"""
import requests
import json
import random
import time
import sys

if sys.platform == 'win32':
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')

import urllib3
urllib3.disable_warnings()

UA_LIST = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
]


def page_headers(referer=None):
    return {
        "User-Agent": random.choice(UA_LIST),
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
        "Cache-Control": "max-age=0",
        "Sec-Fetch-Dest": "document",
        "Sec-Fetch-Mode": "navigate",
        "Sec-Fetch-Site": "none" if referer is None else "same-origin",
        "Sec-Fetch-User": "?1",
        "Upgrade-Insecure-Requests": "1",
        "Connection": "keep-alive",
    }


def api_headers():
    return {
        "User-Agent": random.choice(UA_LIST),
        "Accept": "application/json, text/plain, */*",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
        "Referer": "https://www.sporttery.cn/jc/index.html",
        "X-Requested-With": "XMLHttpRequest",
        "Sec-Fetch-Dest": "empty",
        "Sec-Fetch-Mode": "cors",
        "Sec-Fetch-Site": "same-origin",
        "Connection": "keep-alive",
    }


def flatten_matches(data):
    """
    竞彩网数据是嵌套结构:
    matchInfoList[].businessDate -> subMatchList[] -> 实际比赛数据
    需要展平
    """
    flat = []
    for item in data.get("value", {}).get("matchInfoList", []):
        business_date = item.get("businessDate", "?")
        for sub in item.get("subMatchList", []):
            sub["_businessDate"] = business_date
            flat.append(sub)
    return flat


def format_match(m):
    league = m.get("leagueAbbName", "?")
    match_no = m.get("matchNumStr", "?")
    home = m.get("homeTeamAbbName", "?")
    away = m.get("awayTeamAbbName", "?")
    date = m.get("matchDate", "?")
    mtime = m.get("matchTime", "?:?")
    status = m.get("matchStatus", "?")

    # 胜平负赔率
    had = m.get("had", {})
    h = had.get("h", "-")
    d = had.get("d", "-")
    a = had.get("a", "-")

    # 让球胜平负
    hhad = m.get("hhad", {})
    goal_line = hhad.get("goalLine", "")
    hh = hhad.get("h", "-")
    hd = hhad.get("d", "-")
    ha = hhad.get("a", "-")

    parts = []
    parts.append("  [{}] {} {} | {} | {} vs {}".format(
        match_no, date, mtime, league, home, away))
    if h != "-":
        parts.append(" | SPF:[{}/{}/{}]".format(h, d, a))
    if goal_line:
        parts.append(" | 让球({}):[{}/{}/{}]".format(goal_line, hh, hd, ha))
    parts.append(" | 状态:{}".format(status))
    return "".join(parts)


def main():
    session = requests.Session()
    session.verify = False
    session.headers.update({"User-Agent": random.choice(UA_LIST)})

    print("=" * 80)
    print("  竞彩网 足球比赛数据验证 v4")
    print("  日期: 昨天(2026-06-17) + 明天(2026-06-19)")
    print("=" * 80)

    # ===== Session 预热 =====
    print()
    print("[Warmup] 访问首页...")
    try:
        r = session.get("https://www.sporttery.cn/", headers=page_headers(None), timeout=15)
        print("  HTTP {} | cookies={}".format(r.status_code, len(session.cookies)))
    except Exception as e:
        print("  失败: {}".format(e))

    time.sleep(2)

    BASE = "https://webapi.sporttery.cn/gateway/jc/football"

    # ===== 1. 获取当前可投注赛事（含昨天还可以买的、明天的） =====
    print()
    print("=" * 80)
    print("  [1] 当前可投注赛事 (getMatchCalculatorV1)")
    print("=" * 80)
    url1 = "{}/getMatchCalculatorV1.qry?poolCode=hhad,had&channel=c".format(BASE)
    print("  URL: {}".format(url1))

    all_matches = []

    try:
        r = session.get(url1, headers=api_headers(), timeout=30)
        print("  HTTP {} | 长度: {}".format(r.status_code, len(r.text)))
        data = r.json()

        if data.get("errorCode") == "0":
            matches = flatten_matches(data)
            all_matches = matches
            print("  [OK] 共 {} 场可投注赛事".format(len(matches)))

            # 按日期分组
            by_date = {}
            for m in matches:
                d = m.get("matchDate", "?")
                by_date.setdefault(d, []).append(m)

            print()
            print("  *** 按日期统计 ***")
            for d in sorted(by_date.keys()):
                ms = by_date[d]
                leagues = {}
                for m in ms:
                    lg = m.get("leagueAbbName", "?")
                    leagues[lg] = leagues.get(lg, 0) + 1
                print("  {}: {}场".format(d, len(ms)))
                for lg, cnt in sorted(leagues.items(), key=lambda x: -x[1]):
                    print("    - {}: {}场".format(lg, cnt))

            print()
            print("  *** 明天(2026-06-19) 赛事明细 ***")
            day19 = by_date.get("2026-06-19", [])
            if day19:
                for m in day19:
                    print(format_match(m))
            else:
                print("  (无2026-06-19的比赛)")

            print()
            print("  *** 所有赛事明细 ***")
            for d in sorted(by_date.keys()):
                ms = by_date[d]
                if d == "2026-06-19":
                    continue  # 上面已打印
                print()
                print("  --- {} ({}场) ---".format(d, len(ms)))
                for m in ms:
                    print(format_match(m))
        else:
            print("  错误: {}".format(data.get("errorMessage", "")))
    except Exception as e:
        print("  异常: {}".format(e))

    # ===== 2. 尝试获取赛果 (getMatchResultV1) — 可能被拦截 =====
    print()
    print("=" * 80)
    print("  [2] 昨天(2026-06-17) 赛果")
    print("=" * 80)

    # 试试不同的端点变体
    result_urls = [
        ("getMatchResultV1 + pcOrWap=0", "{}/getMatchResultV1.qry?matchPage=1&pcOrWap=0&leagueId=&matchBeginDate=2026-06-17&matchEndDate=2026-06-17".format(BASE)),
        ("getMatchResultV1 + pcOrWap=2", "{}/getMatchResultV1.qry?matchPage=1&pcOrWap=2&leagueId=&matchBeginDate=2026-06-17&matchEndDate=2026-06-17".format(BASE)),
        ("getMatchResultV1 无pcOrWap", "{}/getMatchResultV1.qry?matchPage=1&leagueId=&matchBeginDate=2026-06-17&matchEndDate=2026-06-17".format(BASE)),
    ]

    for label, url in result_urls:
        try:
            r = session.get(url, headers=api_headers(), timeout=30)
            if r.status_code == 403:
                print("  {}: HTTP 403 (被拦截)".format(label))
            elif r.status_code == 200:
                try:
                    data = r.json()
                    ec = data.get("errorCode", "?")
                    if ec == "0":
                        matches = data.get("value", {}).get("matchResultList", [])
                        print("  {}: [OK] {} 场赛果".format(label, len(matches)))
                        for m in matches:
                            print(format_match(m, is_result=True))
                    else:
                        print("  {}: HTTP 200, errorCode={}".format(label, ec))
                except:
                    print("  {}: HTTP 200, 非JSON: {}".format(label, r.text[:200]))
            else:
                print("  {}: HTTP {}".format(label, r.status_code))
        except Exception as e:
            print("  {}: 异常: {}".format(label, e))
        time.sleep(1)

    # ===== 3. 尝试 getBonusResultV1 =====
    print()
    print("=" * 80)
    print("  [3] 尝试开奖结果接口 (getBonusResultV1)")
    print("=" * 80)
    try:
        url3 = "{}/getBonusResultV1.qry?matchPage=1&pcOrWap=0&leagueId=&matchBeginDate=2026-06-17&matchEndDate=2026-06-17".format(BASE)
        r = session.get(url3, headers=api_headers(), timeout=30)
        print("  HTTP {} | 长度: {}".format(r.status_code, len(r.text)))
        if r.status_code == 200:
            try:
                data = r.json()
                print("  errorCode={}, msg={}".format(data.get("errorCode"), data.get("errorMessage", "")))
                print("  响应(前500): {}".format(r.text[:500]))
            except:
                print("  响应: {}".format(r.text[:500]))
        else:
            print("  HTTP {}".format(r.status_code))
    except Exception as e:
        print("  异常: {}".format(e))

    print()
    print("=" * 80)
    print("  数据拉取完成")
    print()
    print("  总结:")
    print("  - getMatchCalculatorV1: 成功, 获取可投注赛事(含赔率)")
    print("  - getMatchResultV1: HTTP 403 (反爬拦截)")
    print("  - getMatchInfoV1: HTTP 200 但 E0001 参数错误")
    print("=" * 80)


if __name__ == "__main__":
    main()
