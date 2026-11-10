# -*- coding: utf-8 -*-
"""多源尝试下载 tporadowski/Redis for Windows 5.0.14 绿色版 zip。"""
import os
import sys

import requests

SOURCES = [
    "https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip",
    "https://gh-proxy.com/https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip",
    "https://ghfast.top/https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip",
    "https://ghproxy.net/https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip",
    "https://mirror.ghproxy.com/https://github.com/tporadowski/redis/releases/download/v5.0.14.1/Redis-x64-5.0.14.1.zip",
]

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "redis.zip")

for url in SOURCES:
    print(f"尝试: {url[:70]}...", flush=True)
    try:
        r = requests.get(
            url, stream=True, timeout=(15, 120), headers={"User-Agent": "Mozilla/5.0"}
        )
        if r.status_code != 200:
            print(f"  HTTP {r.status_code}，跳过", flush=True)
            continue
        total = 0
        with open(OUT, "wb") as f:
            for chunk in r.iter_content(65536):
                f.write(chunk)
                total += len(chunk)
        if total > 1000000:  # >1MB 才算有效 zip
            print(f"成功！下载 {total} 字节 -> {OUT}", flush=True)
            sys.exit(0)
        print(f"  文件过小({total}字节)，跳过", flush=True)
    except Exception as e:  # noqa: BLE001
        print(f"  失败: {repr(e)}", flush=True)

print("全部源失败", flush=True)
sys.exit(1)
