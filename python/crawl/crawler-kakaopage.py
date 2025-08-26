from selenium import webdriver
from bs4 import BeautifulSoup
import time
import json
import csv
import requests

# --- 공통 GraphQL 세팅 ---
url = "https://bff-page.kakao.com/graphql"
headers = {
    "accept": "*/*",
    "content-type": "application/json",
    "origin": "https://page.kakao.com",
    "referer": "https://page.kakao.com/",
    "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
}

# --- GraphQL 쿼리 ---
# contentHomeOverview
overview_query = """
query contentHomeOverview($seriesId: Long!) {
  contentHomeOverview(seriesId: $seriesId) {
    content {
      seriesId
      title
      authors
      category
      subcategory
      description
      pubPeriod
      ageGrade
      thumbnail
      serviceProperty {
        viewCount
        ratingCount
        ratingSum
      }
    }
  }
}
"""

# contentHomeInfo (tag 가져오기)
info_query = """
query contentHomeInfo($seriesId: Long!) {
  contentHomeInfo(seriesId: $seriesId) {
    about {
      themeKeywordList {
        title
      }
    }
  }
}
"""

# contentHomeProductList 쿼리 (총 회차 수 가져오기)
product_query = """
query contentHomeProductList($seriesId: Long!, $boughtOnly: Boolean) {
  contentHomeProductList(
    seriesId: $seriesId
    boughtOnly: $boughtOnly
  ) {
    totalCount
  }
}
"""

# --- 크롤링 함수 ---
def crawl_kakao(menu_id, screen_id, filename):
    driver = webdriver.Chrome()
    driver.get(f"https://page.kakao.com/menu/{menu_id}/screen/{screen_id}")

    # 스크롤 내려서 300개 로드
    for _ in range(10):
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(2)

    html = driver.page_source
    driver.quit()

    soup = BeautifulSoup(html, "html.parser")
    series_ids = []
    for div in soup.find_all("div", attrs={"data-t-obj": True}):
        raw = div["data-t-obj"]
        data = json.loads(raw)
        eventMeta = data.get("eventMeta")
        if eventMeta and "series_id" in eventMeta:
            series_ids.append(eventMeta["series_id"])

    print(f"총 {len(series_ids)}개 추출됨")

    excluded = 0
    saved = 0

    with open(filename, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow([
            "seriesId", "title", "authors", "category", "subcategory",
            "description", "pubPeriod", "ageGrade",
            "viewCount", "ratingCount", "ratingSum", "thumbnail", "tags", "totalCount"
        ])

        for sid in series_ids:
            try:
                # --- contentHomeOverview 호출 ---
                res = requests.post(url, headers=headers, json={
                    "query": overview_query,
                    "variables": {"seriesId": int(sid)}
                })
                if res.status_code != 200:
                    continue
                data = res.json()
                content = data.get("data", {}).get("contentHomeOverview", {}).get("content")
                if not content:
                    continue

                ageGrade = str(content.get("ageGrade"))
                if "Nineteen" in ageGrade:
                    excluded += 1
                    continue

                seriesId = content.get("seriesId")
                title = content.get("title")
                authors_field = content.get("authors")
                if isinstance(authors_field, list):
                    authors = ",".join(authors_field)
                elif isinstance(authors_field, str):
                    authors = authors_field
                else:
                    authors = ""
                category = content.get("category")
                subcategory = content.get("subcategory")
                description = content.get("description")
                pubPeriod = content.get("pubPeriod")
                thumbnail = content.get("thumbnail")

                service = content.get("serviceProperty") or {}
                viewCount = service.get("viewCount")
                ratingCount = service.get("ratingCount")
                ratingSum = service.get("ratingSum")

                # --- contentHomeInfo (tag) 호출 ---
                tags = ""
                info_res = requests.post(url, headers=headers, json={
                    "query": info_query,
                    "variables": {"seriesId": int(sid)}
                })
                if info_res.status_code == 200:
                    theme_list = (
                        info_res.json()
                        .get("data", {})
                        .get("contentHomeInfo", {})
                        .get("about", {})
                        .get("themeKeywordList", [])
                    )
                    tags = ", ".join([t["title"] for t in theme_list if "title" in t])

                # --- ProductList (총 회차 수) 호출 ---
                total_count_res = requests.post(url, headers=headers, json={"query": product_query, "variables": {"seriesId": int(sid), "boughtOnly": False}})
                total_count = total_count_res.json()["data"]["contentHomeProductList"]["totalCount"]

                writer.writerow([
                    seriesId, title, authors, category, subcategory,
                    description, pubPeriod, ageGrade,
                    viewCount, ratingCount, ratingSum, thumbnail, tags, total_count
                ])
                saved += 1

            except Exception as e:
                print(f"{sid} 처리 중 오류:", e)

    print(f"\n{filename} 저장 완료 → 총 {len(series_ids)}개 시도 / {excluded}개 제외 / {saved}개 저장\n")


# --- 실행 ---
# 웹툰(Top300)
crawl_kakao(menu_id=10010, screen_id=93, filename="kakaopage_webtoon.csv")

# 웹툰 전체(18,625개)
# crawl_kakao(menu_id=10010, screen_id=82, filename="kakaopage_webtoon.csv")

# 웹소설(Top300)
crawl_kakao(menu_id=10011, screen_id=94, filename="kakaopage_webnovel.csv")

# 웹소설 전체(59,119개)
# crawl_kakao(menu_id=10011, screen_id=84, filename="kakaopage_webnovel.csv")
