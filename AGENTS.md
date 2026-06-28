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

## Dependencies

| Module | Dependency | Version | Notes |
|--------|-----------|---------|-------|
| excel | poi / poi-ooxml | 5.4.1 | 5.5.1 available, not critical |
| excel | commons-lang3 | 3.17.0 | 3.20.0 available, low priority |
| excel | commons-csv | 1.11.0 | 1.14.1 available, low priority |
| excel | caffeine | 3.2.3 | Replaced Guava in commit 971c241 |
| excel | xercesImpl | 2.12.2 | Latest |
| excel | junit | 4.13.2 (test) | Latest JUnit 4 |
| excel | log4j-core | 2.24.3 (test) | Provides Log4j 2.x runtime for POI, test-only |
| demo | spring-boot-starter-parent | 3.4.3 | 3.4.4 patch exists; 4.x needs JDK 22+ |

## Code Style

- Source files use **tab indentation** with **CRLF line endings**. The Edit tool will fail on whitespace mismatches — always read the file first and copy exact indentation.
- Commit style: Chinese conventional commits (`feat:`, `fix:`, `docs:`, `refactor:`). Message body in Chinese, concise, explaining "why".

## Naming Conventions (Do Not "Fix")

- `registeAdapter` — intentional misspelling; used across both `BingExcelImpl` and `BingExcelEventImpl`. Do not rename.
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
  - `converterCache`: Caffeine (thread-safe)
  - `ConversionMapper.fieldMapper`: `ConcurrentHashMap`
- **Known flaw**: `FieldConverterMapper.setFieldConverter()` is lazy-called during marshal/unmarshal without synchronization. Mitigated by **startup warm-up** in `BingExcelWarmUpRunner` — triggers all lazy-init at container startup, leaving only read paths at runtime.
- Each read/write operation creates its own handler/listener internally, no shared I/O state.

### `ListRow` and Cell Handling

- `ListRow` stores `CellKV<String>(index, value)` sparsely. `toFullArray()` expands to a dense `String[]` with `null` gaps.
- **XSSF** (`DefaultXSSFSaxHandler`): Always adds a `CellKV`, empty values as `null`.
- **HSSF** (`HSSFListenerAbstract`): Skips empty value cells (except `MissingCellDummyRecord` which adds `null`).
- Both end up with consistent `toFullArray()` behavior; maxIndex may differ in edge cases.

### Deprecated APIs

- `BingExcelBuilder.builder()` is `@Deprecated` — use `.build()` instead.
- `BingExcelEvent` / `BingExcelEventImpl` / `BingExcelEventBuilder` are `@Deprecated` but still functional. Do not remove unless explicitly asked.
- `ArrayConverter` / `CollectionConverter` are `@Deprecated` with TODO comments.
- `ListRow.getList()` is `@Deprecated`.

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
  - WSL VM IP resolution: `wsl -d podman-machine-default sh -c 'ip -4 addr show eth0 | grep -oP "inet \K[0-9.]+" | head -1'`
  - Image already exists locally as `localhost/bing-excel-demo:latest`.
  - Stress test result: 50 concurrency × 2000 requests, 0 errors.

## Common Pitfalls

- `@CellConfig.aliasName` without any `index >= 0` cannot be written — write throws `IllegalCellConfigException`.
- Empty list write creates a sheet but no rows; the output file is a valid minimal xlsx.
- Excel files for test scenarios should use POI `XSSFWorkbook` to create, not `BingExcel.writeExcel` (which requires index).
- `OutValue` only has `OutValue(OutType, Object)` constructor; use static factories: `OutValue.stringValue()`, `OutValue.intValue()`, `OutValue.dateValue()`, etc.
- `WriteHandler.setDataValidationList()` takes `short` parameters, not `int`.
- When upgrading POI, the forked SAX/HSSF listeners (`ExcelXSSFSheetXMLHandler`, `ExcelFormatTrackingHSSFListener`) replaced `POILogFactory/POILogger` with `java.util.logging.Logger` — check those files when upgrading POI again.
