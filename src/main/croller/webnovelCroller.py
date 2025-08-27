# -*- coding: utf-8 -*-
import csv
import time
import random
from urllib.parse import urljoin

from seleniumbase import Driver
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException, WebDriverException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

BASE = "https://www.webnovel.com"
START_URL = "https://www.webnovel.com/ranking/novel/bi_annual/power_rank"
CSV_PATH = "novel_data_webnovel.csv"

# ---------------------------------------
# 유틸
# ---------------------------------------
def human_pause(a=0.4, b=1.2):
    time.sleep(random.uniform(a, b))

def get_text_safe(driver, by, locator, timeout=12):
    try:
        el = WebDriverWait(driver, timeout).until(
            EC.visibility_of_element_located((by, locator))
        )
        return el.text.strip()
    except Exception:
        return "N/A"

def get_attr_safe(driver, by, locator, attr, timeout=12):
    try:
        el = WebDriverWait(driver, timeout).until(
            EC.visibility_of_element_located((by, locator))
        )
        val = (el.get_attribute(attr) or "").strip()
        return val if val else "N/A"
    except Exception:
        return "N/A"

# ---------------------------------------
# 1) 목록 페이지에서 작품 링크 수집
# ---------------------------------------
def collect_book_links(driver):
    """
    무한스크롤/지연로딩에 대응하여 목록 페이지의
    a.c_l[data-report-uiname="bookcover"] 모든 href를 수집.
    """
    print("\n=== 링크 수집 시작 ===")
    driver.uc_open(START_URL)   # timeout 파라미터 없음(지원X)
    human_pause(1, 2)

    # 초기 로딩 대기(주요 앵커 등장까지)
    for _ in range(10):
        if driver.find_elements(By.CSS_SELECTOR, 'a.c_l[data-report-uiname="bookcover"]'):
            break
        human_pause(0.8, 1.6)

    def collect_from_dom():
        anchors = driver.find_elements(By.CSS_SELECTOR, 'a.c_l[data-report-uiname="bookcover"]')
        out = []
        for a in anchors:
            href = a.get_attribute("href")
            if href:
                out.append(urljoin(BASE, href))
        return out

    seen = set()
    all_links = []
    last_count = -1
    stable_rounds = 0

    while True:
        for link in collect_from_dom():
            if link not in seen:
                seen.add(link)
                all_links.append(link)

        # 스크롤 다운 & 대기
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        human_pause(0.8, 1.6)

        # 더 이상 늘지 않으면 2회 확인 후 종료
        if len(all_links) == last_count:
            stable_rounds += 1
            if stable_rounds >= 2:
                break
        else:
            stable_rounds = 0
            last_count = len(all_links)

    print(f"총 {len(all_links)}개 링크 수집 완료")
    return all_links

# ---------------------------------------
# 2) 상세 페이지 크롤링
# ---------------------------------------
def crawl_individual_novels(driver, novel_list):
    """
    SeleniumBase Driver(uc=True 권장)를 받아 각 작품 상세 페이지에서 정보를 추출.
    - CSS 다중 클래스는 '.class1.class2' 형태
    - 가급적 CSS 사용, 불가피하면 XPath
    - 실패 시 'N/A'로 채움
    """
    print("\n" + "=" * 80)
    print("Individual Novel Page Crawling Starting...")
    print("=" * 80)

    scraped_data = []

    for idx, novel_url in enumerate(novel_list, 1):
        try:
            print(f"[{idx}/{len(novel_list)}] 접속: {novel_url}")
            driver.get(novel_url)

            # 페이지 로드 대기
            try:
                WebDriverWait(driver, 20).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "body"))
                )
            except TimeoutException:
                print("  초기 로드 타임아웃: body 감지 실패")
                continue

            # 1) 제목 (원 코드의 클래스 나열 → CSS로)
            title = get_text_safe(driver, By.CSS_SELECTOR, ".pt4.pb4.pr4.oh.mb4.fs36.lh40.auto_height")

            # 2) 작가
            author = get_text_safe(driver, By.CSS_SELECTOR, ".c_primary")

            # 3) 조회수 (가능하면 의미있는 CSS로 교체 권장; 임시로 보조 셀렉터 포함)
            view = get_text_safe(driver, By.XPATH, '/html/body/div[2]/div[2]/div/div/div[2]/div[2]/strong[2]/span')
            if view == "N/A":
                try:
                    el = driver.find_element(By.CSS_SELECTOR, "div div strong span")
                    view = el.text.strip() or "N/A"
                except Exception:
                    pass

            # 4) 태그
            tag = get_text_safe(driver, By.CSS_SELECTOR, ".writer-tag")

            # 5) 소개글
            synopsis = get_text_safe(driver, By.XPATH, '//*[@id="about"]/div[1]/div[1]/p')

            # 6) 추천수
            recommend = get_text_safe(driver, By.XPATH, '/html/body/div[2]/div[2]/div/div/div[2]/p/strong')

            # 7) 회차 수
            count = get_text_safe(driver, By.XPATH, '/html/body/div[2]/div[2]/div/div/div[2]/div[2]/strong[1]/span')

            # 8) 커버 이미지
            img = get_attr_safe(driver, By.XPATH, '/html/body/div[2]/div[2]/div/div/div[1]/i/img', "src")
            if img == "N/A":
                try:
                    img_el = driver.find_element(By.CSS_SELECTOR, "img")
                    img = img_el.get_attribute("src") or "N/A"
                except Exception:
                    pass

            novel_info = {
                "url": novel_url,
                "title": title,
                "author": author,
                "view": view,
                "recommend": recommend,
                "tag": tag,
                "synopsis": synopsis,
                "count": count,
                "img": img,
            }
            scraped_data.append(novel_info)
            print("  ✔", novel_info)

            human_pause(0.2, 0.6)  # 예의상 천천히

        except (TimeoutException, WebDriverException) as e:
            print(f"  치명 오류: {e}")
            continue
        except Exception as e:
            print(f"  예외 발생: {e}")
            continue

    print("\n" + "=" * 80)
    print("Individual Novel Page Crawling Complete!")
    print("=" * 80)
    return scraped_data

# ---------------------------------------
# 3) 메인: 링크 수집 → 상세 크롤링 → CSV 저장
# ---------------------------------------
def main():
    driver = Driver(
        uc=True,          # undetected-chromedriver (Cloudflare 대응)
        headless=False,   # 필요 시 True
        incognito=True,
        ad_block=True,
        no_sandbox=True,
        disable_csp=True,
    )
    try:
        links = collect_book_links(driver)
        if not links:
            print("수집된 링크가 없습니다. 종료합니다.")
            return

        data = crawl_individual_novels(driver, links)

        if data:
            keys = ["url", "title", "author", "view", "recommend", "tag", "synopsis", "count", "img"]
            with open(CSV_PATH, "w", newline="", encoding="utf-8") as f:
                wr = csv.DictWriter(f, fieldnames=keys)
                wr.writeheader()
                for row in data:
                    wr.writerow(row)
            print(f"\nCSV 저장 완료: {CSV_PATH}")
        else:
            print("\n저장할 데이터가 없습니다.")

    finally:
        print("\n브라우저 종료 중...")
        driver.quit()
        print("완료!")

if __name__ == "__main__":
    main()
