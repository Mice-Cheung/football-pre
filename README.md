# FootballPred - 足球赛事数据平台

前后端分离架构的足球赛事数据展示平台，功能对齐中国竞彩足球。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Element Plus + Tailwind CSS + Vite |
| 后端 | SpringBoot 3.3 + JDK 21 + Spring Data JPA + PostgreSQL |
| 图表 | ECharts 5 (vue-echarts) |
| 图标 | Lucide Icons (lucide-vue-next) |
| 状态 | Pinia |
| 路由 | Vue Router 4 |

## 项目结构

```
football-prediction/
├── backend/          # SpringBoot 3 后端
│   └── src/main/java/com/football/
│       ├── entity/      # JPA 实体 (Match, Team, Lineup, Odds, KellyIndex, Tactics)
│       ├── dto/         # 数据传输对象
│       ├── repository/  # Spring Data JPA Repository
│       ├── service/     # 业务逻辑层
│       ├── controller/  # RESTful API 控制器
│       ├── config/      # CORS、OpenAPI 配置
│       ├── init/        # Mock 数据初始化器 (DataInitializer)
│       └── exception/   # 全局异常处理
├── frontend/         # Vue 3 前端
│   └── src/
│       ├── views/       # 页面组件 (HomeView, MatchDetailView, TeamView, NotFoundView)
│       ├── components/  # 通用组件 (layout/, match/, lineup/, odds/, tactics/)
│       ├── stores/      # Pinia 状态管理
│       ├── api/         # Axios API 接口层
│       ├── types/       # TypeScript 类型定义
│       └── router/      # Vue Router 路由配置
```

## 核心功能

1. **比赛列表页** — 联赛筛选、比赛卡片、分页加载，对齐竞彩足球赛程
2. **对阵阵容** — 首发11人阵型图可视化（CSS Grid 模拟足球场布局）
3. **技战术打法** — 进攻/防守风格、近期战绩、战术描述
4. **凯利指数** — ECharts 柱状图 + 数据表格，多公司对比
5. **中外赔率对比** — 中国体育彩票 vs Bet365/William Hill/Pinnacle 等外国机构实时对比

## 快速启动

### 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 后端

**环境要求**: JDK 21 + Maven 3.9+ + PostgreSQL 16 (开发环境使用 H2 内存数据库)

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

- API 地址: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 控制台: http://localhost:8080/h2-console

### API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/matches` | GET | 比赛列表（支持 league/status 筛选 + 分页） |
| `/api/matches/{id}` | GET | 比赛详情 |
| `/api/matches/{id}/detail` | GET | 比赛详情（含阵容） |
| `/api/matches/leagues` | GET | 联赛列表 |
| `/api/teams/{id}` | GET | 球队详情 |
| `/api/teams/lineups/match/{matchId}` | GET | 比赛阵容 |
| `/api/odds/match/{matchId}` | GET | 比赛赔率 |
| `/api/odds/match/{matchId}/comparison` | GET | 中外赔率对比 |
| `/api/odds/kelly/match/{matchId}` | GET | 凯利指数 |
| `/api/tactics/match/{matchId}` | GET | 技战术数据 |

## 设计特色

- 深色体育竞技风主题，草地绿 + 金色点缀
- 毛玻璃效果卡片布局
- 阵型图 CSS Grid 可视化
- 赔率趋势红绿标注
- 中外赔率差异高亮对比
