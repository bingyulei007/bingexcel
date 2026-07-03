# AGENTS.md

This file provides guidance to the AI agent when working with code in this repository.

## Build & Test

- Main module is under `excel/`; there is no parent POM. Run all Maven commands from `excel/`:
  ```
  cd excel && mvn compile -q
  cd excel && mvn test -q
  ```
- Demo module is under `demo/`. It depends on `excel` via local `mvn install`. Build excel first:
  ```
  cd excel && mvn install -DskipTests -q
  cd demo && mvn compile -q
  ```
- **JDK baseline is 17** with `<release>17</release>` in compiler. POI is 5.4.1. Do not use APIs from earlier or later versions.
- `demo/pom.xml` uses Spring Boot 3.4.3 as parent. The `BingExcel` bean is a singleton, wired via `BingExcelBuilder.builderInstance()`.
- When the excel module is changed, run `mvn install -DskipTests -q` in excel before running demo tests.

## Podman (local)

本机 Podman 环境速查 — 后续需要容器化构建/测试/压测时按需选用。

- **环境**: `podman 5.8.3`，WSL machine `podman-machine-default` (running, rootless, 11 CPU / 2 GiB / 100 GiB)。架构 linux/amd64。
  ```bash
  podman machine list                  # 查看 VM 状态
  podman info --format '{{.Host.Security.Rootless}}'   # rootless 确认
  ```
- **Registry 连通性**: `docker.io` ✅ / `quay.io` ✅ 均可直连。已配置国内镜像加速器（`docker.1panel.live`、`docker.m.daocloud.io`），拉取慢时优先走加速器，例如 `podman pull docker.m.daocloud.io/library/<image>`。
- **本机已有镜像（可直接复用，无需重新拉取）**:
  | 镜像 | 用途（bingexcel 场景） |
  |------|------------------------|
  | `docker.io/library/maven:3.9-eclipse-temurin-17` | JDK17 + Maven 构建，与本库 `<release>17</release>` 基线匹配。容器内构建 excel/demo |
  | `docker.1panel.live/library/eclipse-temurin:17-jre` | 仅运行 demo（Spring Boot 3.4.3）的 JRE 基础镜像 |
  | `docker.io/library/python:3.12-alpine` | 跑 `demo/stress.py` 压测（配合 `--network=host`） |
  | `localhost/rakyll/hey` | hey 压测工具，用法见下方 Stress testing 段 |
  | `docker.io/library/redis:7-alpine` | 若 demo 引入缓存场景可本地起一个 |
  | `docker.io/testcontainers/ryuk:0.12.0` | testcontainers 集成用 |
  | `docker.m.daocloud.io/library/golang:1.24-alpine` | 通用工具/旁路脚本 |
- **Maven 依赖缓存**: 主机 `~/.m2`（约 938 MB）。容器内构建可挂载复用，避免重复下载：
  ```bash
  podman run --rm -v "$(pwd):/work:Z" -v "$HOME/.m2:/root/.m2:Z" \
      -w /work/excel docker.io/library/maven:3.9-eclipse-temurin-17 mvn -B install -DskipTests
  ```
  > Windows 路径挂载注意: Git Bash 下 `$HOME` 指向 `/c/Users/28354`；WSL rootless 下 `:Z` 标签可省，遇到 SELinux 报错再加。
- **容器内访问宿主服务**: 容器不能直接用 `127.0.0.1` 访问 Windows 上的 Spring Boot。详见下方 **Stress testing** 段的 IP 探测方法（2026-06-29 实测 `172.28.240.1` 可达）。
- 现成的 bingexcel 镜像: `localhost/bing-excel-demo:latest`（根 `Dockerfile` 多阶段构建产物）。

## Dependencies

| Module | Dependency | Version | Notes |
|--------|-----------|---------|-------|
| excel | poi / poi-ooxml | 5.4.1 | 5.5.1 available, not critical |
| excel | commons-lang3 | 3.18.0 | Bumped from 3.17.0 in commit b9525f3 |
| excel | commons-csv | 1.11.0 (optional) | Marked optional in commit df30a37; `writeCsv` callers must self-declare |
| excel | ~~caffeine~~ | — | Removed in commit df30a37; `converterCache` now uses `ConcurrentHashMap` |
| excel | xercesImpl | 2.12.2 | Latest |
| excel | junit | 4.13.2 (test) | Latest JUnit 4 |
| excel | ~~log4j-core~~ | — | Removed in commit df30a37; main code uses `java.util.logging`. POI still pulls `log4j-api` transitively |
| demo | spring-boot-starter-parent | 3.4.3 | 3.4.4 patch exists; 4.x needs JDK 22+ |

## Code Style

- Source files use **tab indentation** with **CRLF line endings**. The Edit tool will fail on whitespace mismatches — always read the file first and copy exact indentation.
- Commit style: Chinese conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`). Message body in Chinese, concise, explaining "why".

## Naming Conventions (Do Not "Fix")

- `registeAdapter` — intentional misspelling; used in `BingExcelImpl`. Do not rename.
- `tagertClazz` — intentional misspelling in listener inner classes. Do not rename.

## Architecture

### Mapping & Index Resolution

- `@CellConfig(index = -1, aliasName = "X")` — aliasName-only field. Read by header matching. Write direction requires `index >= 0`, otherwise throws.
- `@CellConfig(index = N, aliasName = "X")` — index-based + header text. Read uses index, write uses both.
- `addFieldConversionMapper(...)` is **whole-field replacement**, not per-property merge. Once called for a field, annotations on that field are fully masked. Do not "enhance" it to merge.
- **`aliasName` is read-only**. Do not add aliasName support to the write path.

### TitleAliasResolver Behavior (updated in commit 266d3bd)

- `captureTitleRow()` skips `null` value cells — they are not added to the title map.
- `resolve()` checks every aliasName-only field:
  - **Found** in header → maps to resolved index.
  - **Not found + `readRequired=true`** → throws `IllegalCellConfigException`.
  - **Not found + `readRequired=false`** (default) → skip, field stays default/null.
- `hasAliasOnlyFields()` returns true if ANY alias-only field exists (regardless of `readRequired`). When `startRow=0`, this triggers an immediate exception since there's no title row to resolve against.
- Header matching is case-insensitive and trims whitespace (`normalize()` uses `trim().toLowerCase(Locale.ROOT)`).

### Thread Safety

- **BingExcel singleton is safe** for concurrent use. Internal state uses:
  - `typeTokenCache`: `ConcurrentHashMap`
  - `userDefineMapperHandler`: `volatile` + DCL
  - `annotatedTypes`: `Collections.synchronizedSet()`
  - `converterCache`: `ConcurrentHashMap` (replaced Caffeine in commit df30a37)
  - `ConversionMapper.fieldMapper`: `ConcurrentHashMap`
- `TypeAdapterConverter` resolves default converters per marshal/unmarshal call without writing them back to shared `FieldConverterMapper` instances. Field-level converters from annotations/user mappings remain stored in mapper objects, but default converter lookup no longer mutates shared state.
- Each read/write operation creates its own handler/listener internally, no shared I/O state.

### `ListRow` and Cell Handling

- `ListRow` stores `CellKV<String>(index, value)` sparsely. `toFullArray()` expands to a dense `String[]` with `null` gaps.
- **XSSF** (`DefaultXSSFSaxHandler`): Always adds a `CellKV`, empty values as `null`.
- **HSSF** (`HSSFListenerAbstract`): Skips empty value cells (except `MissingCellDummyRecord` which adds `null`).
- Both end up with consistent `toFullArray()` behavior; maxIndex may differ in edge cases.

### Removed APIs (previously deprecated)

The following deprecated APIs have been removed. Use the replacements listed:

- `BingExcelBuilder.builder()` / `ExcleBuilder.builder()` — removed; use `.build()` instead.
- `BingExcelEvent` / `BingExcelEventImpl` / `BingExcelEventBuilder` / `BingReadListener` — removed; use `BingExcelBuilder` + `BingExcelImpl`.
- `ArrayConverter` / `CollectionConverter` — removed (were dead code, never registered).
- `ListRow.getList()` — removed (zero callers).
- `ExcelConverterMapperHandler.getLocalConverter(Class, String)` — removed; use `getLocalFieldConverterMapper(Class, String)` then `.getFieldConverter()`.
- `ExcelBuiltinFormats.getBuiltinFormats()` — removed; use `getAll()`.

## Demo Project (`demo/`)

- Spring Boot 3.4.3 web app for testing bingExcel.
- **REST endpoints**:
  - `POST /api/excel/read` — upload Excel, return parsed JSON.
  - `GET /api/excel/download-sample` — download sample Person.xlsx.
  - `POST /api/excel/write` — submit JSON, download Excel.
- **Tests**:
  - `BingExcelConcurrencyTest`: 16 threads × 50 iterations = 800 ops, 0 errors.
  - `BingExcelReadTest`: 21 scenarios (aliasName matching, index reading, missing columns, required fields, empty cells, xls, roundtrip, etc.).
- **Stress testing** (podman):
  - `Dockerfile` at repo root: multi-stage build.
  - `demo/stress.sh`: orchestration script. Uses `python:3.12-alpine` with `--network=host` + `demo/stress.py`.
  - `demo/stress.py`: pure-stdlib HTTP load tester (no dependencies). Supports configurable concurrency and total requests.
  - `localhost/rakyll/hey` can also be used for manual load testing, for example:
    ```bash
    podman run --rm localhost/rakyll/hey -n 5000 -c 500 http://<reachable-host-ip>:8080/api/excel/download-sample
    ```
  - On Windows + Podman WSL, container networking can be misleading:
    - `127.0.0.1:8080` from inside the container may point to the container/Podman VM view, not the Windows Spring Boot process, and can fail with `connect: connection refused`.
    - `host.containers.internal` may resolve but still fail in this environment.
    - The Podman VM `eth0` IP is useful to diagnose the VM, but it is not necessarily the Windows host IP that reaches the demo service:
      ```bash
      wsl -d podman-machine-default sh -c 'ip -4 addr show eth0 2>/dev/null | grep -oP "inet \\K[0-9.]+" | head -1' 2>/dev/null || true
      ```
  - To find a container-reachable Windows host IP, list non-loopback Windows IPv4 addresses and probe them from the `hey` container:
    ```bash
    powershell.exe -NoProfile -Command 'Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike "127.*" } | Select-Object -ExpandProperty IPAddress'
    podman run --rm localhost/rakyll/hey -n 1 -c 1 http://<candidate-ip>:8080/api/excel/download-sample
    ```
  - Known-good example on 2026-06-29: `172.28.240.1:8080` was reachable from the `localhost/rakyll/hey` container; `172.28.248.148:8080` (Podman VM `eth0`) was not.
  - Image already exists locally as `localhost/bing-excel-demo:latest`.
  - Stress test result: 50 concurrency × 2000 requests, 0 errors.
  - `hey` result on 2026-06-29: 500 concurrency × 5000 requests against `/api/excel/download-sample`, 5000 HTTP 200 responses, about 6502 req/s, p99 about 217 ms.

## Common Pitfalls

- `@CellConfig.aliasName` without any `index >= 0` cannot be written — write throws `IllegalCellConfigException`.
- Empty list write creates a sheet but no rows; the output file is a valid minimal xlsx.
- Excel files for test scenarios should use POI `XSSFWorkbook` to create, not `BingExcel.writeXlsx` (which requires index).
- `OutValue` only has `OutValue(OutType, Object)` constructor; use static factories: `OutValue.stringValue()`, `OutValue.intValue()`, `OutValue.dateValue()`, etc.
- `WriteHandler.setDataValidationList()` takes `short` parameters, not `int`.
- When upgrading POI, the forked SAX/HSSF listeners (`ExcelXSSFSheetXMLHandler`, `ExcelFormatTrackingHSSFListener`) replaced `POILogFactory/POILogger` with `java.util.logging.Logger` — check those files when upgrading POI again.
