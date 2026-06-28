#!/usr/bin/env python3
"""
BingExcel demo - HTTP 并发压测脚本 (无第三方依赖, 仅用 stdlib)

用法:
  python3 stress.py --host http://<VM_IP>:8080 --concurrency 50 --total 1000

输出:
  - 总请求数 / 成功 / 失败
  - QPS
  - 延迟分位 (P50/P90/P95/P99)
  - HTTP 状态码分布
"""
import argparse
import concurrent.futures
import json
import statistics
import sys
import time
import urllib.request
import urllib.error


def do_get(url, timeout=30):
    t0 = time.perf_counter()
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp.read()
            return resp.status, time.perf_counter() - t0, None
    except Exception as e:
        return 0, time.perf_counter() - t0, str(e)[:80]


def do_post(url, body, timeout=30):
    t0 = time.perf_counter()
    try:
        data = body.encode("utf-8")
        req = urllib.request.Request(
            url, data=data, method="POST",
            headers={"Content-Type": "application/json"})
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            resp.read()
            return resp.status, time.perf_counter() - t0, None
    except Exception as e:
        return 0, time.perf_counter() - t0, str(e)[:80]


def percentile(sorted_values, p):
    if not sorted_values:
        return 0.0
    idx = int(len(sorted_values) * p / 100)
    if idx >= len(sorted_values):
        idx = len(sorted_values) - 1
    return sorted_values[idx]


def run(label, fn, url, concurrency, total, *args):
    print(f"\n=== {label} ===")
    print(f"  URL          : {url}")
    print(f"  Concurrency  : {concurrency}")
    print(f"  Total        : {total}")

    latencies = []
    status_counts = {}
    errors = 0
    success = 0

    t_start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        futures = [pool.submit(fn, url, *args) for _ in range(total)]
        for f in concurrent.futures.as_completed(futures):
            status, lat, err = f.result()
            latencies.append(lat)
            status_counts[status] = status_counts.get(status, 0) + 1
            if status == 200:
                success += 1
            else:
                errors += 1
    elapsed = time.perf_counter() - t_start

    latencies.sort()
    qps = total / elapsed if elapsed > 0 else 0

    print(f"  Success      : {success}")
    print(f"  Errors       : {errors}")
    print(f"  Elapsed      : {elapsed:.2f}s")
    print(f"  QPS          : {qps:.2f}")
    print(f"  Latency(ms)  : "
          f"min={latencies[0]*1000:.2f} "
          f"avg={statistics.mean(latencies)*1000:.2f} "
          f"max={latencies[-1]*1000:.2f}")
    print(f"  Percentiles  : "
          f"P50={percentile(latencies,50)*1000:.2f}ms "
          f"P90={percentile(latencies,90)*1000:.2f}ms "
          f"P95={percentile(latencies,95)*1000:.2f}ms "
          f"P99={percentile(latencies,99)*1000:.2f}ms")
    print(f"  Status codes : {dict(sorted(status_counts.items()))}")
    return errors


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--host", required=True, help="target base url, e.g. http://1.2.3.4:8080")
    ap.add_argument("--concurrency", type=int, default=50)
    ap.add_argument("--total", type=int, default=1000)
    args = ap.parse_args()

    body = json.dumps([
        {"name": "Alice", "age": 28, "salary": 8500.0, "gender": 0},
        {"name": "Bob", "age": 35, "salary": 12000.0, "gender": 1},
    ])

    # 预热
    print("=== Warm-up ===")
    for _ in range(3):
        do_get(f"{args.host}/api/excel/download-sample")
    print("  warm-up done")

    total_errors = 0
    total_errors += run(
        "GET /api/excel/download-sample",
        do_get, f"{args.host}/api/excel/download-sample",
        args.concurrency, args.total)
    total_errors += run(
        "POST /api/excel/write",
        do_post, f"{args.host}/api/excel/write",
        args.concurrency, args.total, body)

    print(f"\n=== Summary ===")
    print(f"  Total errors: {total_errors}")
    sys.exit(0 if total_errors == 0 else 1)


if __name__ == "__main__":
    main()
