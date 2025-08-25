import os
import csv
import pymysql
import pandas as pd


# === MySQL 연결 ===
conn = pymysql.connect(
    host="localhost", user="root", password="root", database="rankhub_db", charset="utf8mb4"
)
cursor = conn.cursor()

# === CSV 컬럼 → DB 컬럼 매핑 규칙 ===
COLUMN_MAPPING = {
    # 제목(title)
    "title":"title",     

    # 작가(authors)
    "authors":"authors", "writer":"authors", "author":"authors",

    # 작품설명(description)
    "description":"description", "synopsis":"description",     

    # 썸네일 URL(thumbnail_url)
    "thumbnail_url":"thumbnail_url", "thumbnail":"thumbnail_url", "img":"thumbnail_url",    

    # 작품링크(content_url)
    "content_url":"content_url", "url":"content_url", "seriesId":"content_url", "id":"content_url",
    
    # 총 회차(total_episodes)                   
    "total_episodes":"total_episodes", "totalCount":"total_episodes", "count":"total_episodes", "episode":"total_episodes", 

    # 태그(tags)
    "tags":"tags", "tag":"tags",   

    # 장르(category)
    "category":"category", "genre":"category", "subcategory":"category",   

    # 연령제한(age_rating)                    
    "age_rating":"age_rating", "ageGrade":"age_rating",       

    # 연재 요일(pub_period)
    "pub_period":"pub_period", "pubPeriod":"pub_period", "weekday":"pub_period",  

    # 조회수(views)
    "views":"views", "viewCount": "views", "view":"views",     

    # 좋아요 수(likes), 추천 수
    "likes":"likes", "recommend":"likes",

    # 평점(rating)                                             
    "rating":"rating",
    
    # 카카오페이지 평점 관련
    "ratingSum":"rating_sum", "ratingCount":"rating_count"
}

# === 파일명에서 platform / content_type 추출 ===
def parse_file_info(filename: str):
    name = filename.lower()
    platform, content_type = None, None

    if "naver" in name and "webtoon" in name:
        platform = "NAVER_WEBTOON"
    elif "kakaopage" in name:
        platform = "KAKAO_PAGE"
    elif "kakao" in name and "webtoon" in name:
        platform = "KAKAO_WEBTOON"
    elif "moonpia" in name or "munpia" in name:
        platform = "MUNPIA"
    elif "novelpia" in name:
        platform = "NOVELPIA"

    if "webtoon" in name or "웹툰" in name:
        content_type = "WEBTOON"
    elif "webnovel" in name or "novel" in name or "웹소설" in name:
        content_type = "WEBNOVEL"

    return platform, content_type


# === DB 저장 함수 ===
def insert_entity(row: dict, platform: str, content_type: str):
    mapped = {
        "title": None,
        "authors": None,
        "description": None,
        "thumbnail_url": None,
        "content_url": None,
        "total_episodes": 0,
        "tags": None,
        "category": None,
        "age_rating": None,
        "pub_period": None,
        "views": 0,
        "likes": 0,
        "rating": 0.0,
        "rating_sum": 0,
        "rating_count": 0,
        "platform": platform,
        "content_type": content_type,
    }

    for col, val in row.items():
        col = col.strip() 
        std_col = COLUMN_MAPPING.get(col)
        if std_col:
            mapped[std_col] = val

    # --- 1. 카카오 썸네일 절대경로 처리 ---
    if platform.lower() == "kakao_page" and mapped["content_url"]:
        # content_url 값이 seriesId라면 URL로 변환
        if str(mapped["content_url"]).isdigit():
            mapped["content_url"] = f"https://page.kakao.com/content/{mapped['content_url']}"

    sql = """
    INSERT INTO content
    (title, authors, description, thumbnail_url, content_url,
     total_episodes, tags, category, age_rating, pub_period,
     views, likes, rating, rating_sum, rating_count,
     platform, content_type)
    VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
    """

    mapped["views"] = parse_korean_number(mapped["views"])
    mapped["likes"] = parse_korean_number(mapped["likes"])
    mapped["total_episodes"] = parse_korean_number(mapped["total_episodes"])
    mapped["rating_sum"] = parse_korean_number(mapped["rating_sum"])
    mapped["rating_count"] = parse_korean_number(mapped["rating_count"])

    cursor.execute(sql, (
        mapped["title"], mapped["authors"], mapped["description"], mapped["thumbnail_url"],
        mapped["content_url"], mapped["total_episodes"], mapped["tags"], mapped["category"],
        mapped["age_rating"], mapped["pub_period"], mapped["views"], mapped["likes"],
        mapped["rating"], mapped["rating_sum"], mapped["rating_count"],
        mapped["platform"], mapped["content_type"]
    ))

def parse_korean_number(val):
    # 문자열에 '만', '억' 단위가 있는 한국식 숫자를 정수로 변환

    if val is None:
        return 0

    s = str(val).strip()
    if s == "":
        return 0

    # 숫자와 소수점만 남기고 , 제거
    s = s.replace(",", "")
    s = s.replace(" 회", "")

    try:
        if s.endswith("억"):
            num = float(s.replace("억", ""))
            return int(num * 100000000)

        if s.endswith("만"):
            num = float(s.replace("만", ""))
            return int(num * 10000)

        # 단위 없으면 그냥 정수 처리
        return int(float(s))
    except ValueError:
        return 0

# === 5. CSV 읽고 저장 ===
def load_and_save(data_dir=None):
    if data_dir is None:
        BASE_DIR = os.path.dirname(os.path.abspath(__file__))  # 이 파일이 있는 위치
        data_dir = os.path.join(BASE_DIR, "data")              # python/db/data

    for filename in os.listdir(data_dir):
        if filename.endswith(".csv"):

            file_path = os.path.join(data_dir, filename)
            print(f"[INFO] 처리 중: {file_path}")

            platform, content_type = parse_file_info(filename)

            seen = set()  # 파일별 중복 체크용 집합

            with open(file_path, "r", encoding="utf-8-sig") as f:
                reader = csv.DictReader(f)
                for row in reader:
                    # 중복 체크 키 (url 있으면 url, 없으면 title+platform)
                    key = row.get("url") or row.get("content_url") or f"{row.get('title')}::{platform}"
                    
                    if key in seen:
                        # 중복이면 건너뛰기
                        continue
                    seen.add(key)

                    insert_entity(row, platform, content_type)

            conn.commit()
            print(f"[DONE] {filename} 저장 완료 (총 {len(seen)}개)")


if __name__ == "__main__":
    # 실행할 때마다 테이블 초기화
    cursor.execute("TRUNCATE TABLE content")
    load_and_save()
    cursor.close()
    conn.close()
