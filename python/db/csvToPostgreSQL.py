import os
import csv
import psycopg2
import pandas as pd

# === PostgreSQL 연결 ===
try:
    conn = psycopg2.connect(
        host="ep-nameless-river-a1iotqh1-pooler.ap-southeast-1.aws.neon.tech",  # Neon 호스트
        dbname="neondb",              # 기본 DB 이름
        user="neondb_owner",          # Neon 계정 이름
        password="npg_6gqah9QuvHzo",           # Neon에서 발급받은 비밀번호
        port=5432,                    # 기본 포트
        sslmode="require"             # SSL 필수 (Neon은 SSL 없으면 접속 불가)
    )
    cursor = conn.cursor()

    # === content 테이블 없으면 생성 ===
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS content (
        id SERIAL PRIMARY KEY,
        title TEXT,
        authors TEXT,
        description TEXT,
        thumbnail_url TEXT,
        content_url TEXT,
        total_episodes INTEGER,
        tags TEXT,
        category TEXT,
        age_rating TEXT,
        pub_period TEXT,
        views BIGINT,
        likes BIGINT,
        rating DOUBLE PRECISION,
        rating_sum BIGINT,
        rating_count BIGINT,
        platform TEXT,
        content_type TEXT
    )
    """)
    conn.commit()

    # === CSV 컬럼 → DB 컬럼 매핑 규칙 ===
    COLUMN_MAPPING = {
        # 제목(title)
        "title": "title",

        # 작가(authors)
        "authors": "authors", "writer": "authors", "author": "authors",

        # 작품설명(description)
        "description": "description", "synopsis": "description",

        # 썸네일 URL(thumbnail_url)
        "thumbnail_url": "thumbnail_url", "thumbnail": "thumbnail_url", "img": "thumbnail_url",

        # 작품링크(content_url)
        "content_url": "content_url", "url": "content_url",
        "seriesId": "content_url", "id": "content_url",

        # 총 회차(total_episodes)
        "total_episodes": "total_episodes", "totalCount": "total_episodes",
        "count": "total_episodes", "episode": "total_episodes",

        # 태그(tags)
        "tags": "tags", "tag": "tags",

        # 장르(category)
        "category": "category", "genre": "category", "subcategory": "category",

        # 연령제한(age_rating)
        "age_rating": "age_rating", "ageGrade": "age_rating",

        # 연재 요일(pub_period)
        "pub_period": "pub_period", "pubPeriod": "pub_period", "weekday": "pub_period",

        # 조회수(views)
        "views": "views", "viewCount": "views", "view": "views",

        # 좋아요 수(likes), 추천 수
        "likes": "likes", "recommend": "likes",

        # 평점(rating)
        "rating": "rating",

        # 카카오페이지 평점 관련
        "ratingSum": "rating_sum", "ratingCount": "rating_count"
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

    # === 한국식 숫자 파싱 (만, 억 단위) ===
    def parse_korean_number(val):
        if val is None:
            return 0
        s = str(val).strip()
        if s == "":
            return 0

        s = s.replace(",", "").replace(" 회", "")
        try:
            if s.endswith("억"):
                return int(float(s.replace("억", "")) * 100000000)
            if s.endswith("만"):
                return int(float(s.replace("만", "")) * 10000)
            return int(float(s))
        except ValueError:
            return 0

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

        # 컬럼 매핑
        for col, val in row.items():
            col = col.strip()
            std_col = COLUMN_MAPPING.get(col)
            if std_col:
                mapped[std_col] = val

        # 카카오페이지 content_url 변환
        if platform and platform.upper() == "KAKAO_PAGE" and mapped["content_url"]:
            if str(mapped["content_url"]).isdigit():
                mapped["content_url"] = f"https://page.kakao.com/content/{mapped['content_url']}"

        # 숫자형 데이터 변환
        mapped["views"] = parse_korean_number(mapped["views"])
        mapped["likes"] = parse_korean_number(mapped["likes"])
        mapped["total_episodes"] = parse_korean_number(mapped["total_episodes"])
        mapped["rating_sum"] = parse_korean_number(mapped["rating_sum"])
        mapped["rating_count"] = parse_korean_number(mapped["rating_count"])

        # SQL 실행
        sql = """
        INSERT INTO content
        (title, authors, description, thumbnail_url, content_url,
         total_episodes, tags, category, age_rating, pub_period,
         views, likes, rating, rating_sum, rating_count,
         platform, content_type)
        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
        """
        cursor.execute(sql, (
            mapped["title"], mapped["authors"], mapped["description"], mapped["thumbnail_url"],
            mapped["content_url"], mapped["total_episodes"], mapped["tags"], mapped["category"],
            mapped["age_rating"], mapped["pub_period"], mapped["views"], mapped["likes"],
            mapped["rating"], mapped["rating_sum"], mapped["rating_count"],
            mapped["platform"], mapped["content_type"]
        ))

    # === CSV 읽고 저장 ===
    def load_and_save(data_dir=None):
        if data_dir is None:
            BASE_DIR = os.path.dirname(os.path.abspath(__file__))  # 이 파일이 있는 위치
            data_dir = os.path.join(BASE_DIR, "data")              # python/db/data

        for filename in os.listdir(data_dir):
            if filename.endswith(".csv"):
                file_path = os.path.join(data_dir, filename)
                print(f"[INFO] 처리 중: {file_path}")

                platform, content_type = parse_file_info(filename)
                seen = set()  # 중복 체크용 집합

                with open(file_path, "r", encoding="utf-8-sig") as f:
                    reader = csv.DictReader(f)
                    for row in reader:
                        # 중복 체크 키 (url 있으면 url, 없으면 title+platform)
                        key = row.get("url") or row.get("content_url") or f"{row.get('title')}::{platform}"
                        if key in seen:
                            continue
                        seen.add(key)
                        insert_entity(row, platform, content_type)

                conn.commit()
                print(f"[DONE] {filename} 저장 완료 (총 {len(seen)}개)")

    # === 실행부 ===
    if __name__ == "__main__":
        cursor.execute("TRUNCATE TABLE content")
        load_and_save()

except Exception as e:
    print("오류 발생:", e)

finally:
    if cursor:
        cursor.close()
    if conn:
        conn.close()
