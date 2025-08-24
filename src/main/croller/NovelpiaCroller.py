from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import time
import re
import csv

def setup_chrome_options():
    """Chrome 옵션 설정 - 경고 메시지 최소화"""
    options = Options()
    options.add_argument('--log-level=3')
    options.add_argument('--disable-logging')
    options.add_experimental_option('excludeSwitches', ['enable-logging'])
    options.add_experimental_option('useAutomationExtension', False)
    return options

def crawl_individual_novels(driver, novel_list):
    """
    Iterates through a list of novels and crawls each individual page,
    extracting specific information.
    """
    print("\n" + "=" * 80)
    print("Individual Novel Page Crawling Starting...")
    print("=" * 80)
    
    scraped_data = []

    for novel in novel_list:
        novel_id = novel['id']
        novel_url = f"https://novelpia.com/novel/{novel_id}"

        # crawl_individual_novels 함수 내부
    # ...
        try:
            print(f"작품 페이지 접속 중, ID: {novel_id}")
            driver.get(novel_url)
            wait = WebDriverWait(driver, 20)

            # 1. 제목 추출
            try:
                title_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[2]')))
                title = title_element.text
            except Exception as e:
                print(f" 제목 추출 실패: {str(e)}")
                title = "N/A" # 실패 시 기본값 설정

            # 2. 작가 추출
            try:
                author_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[3]/p[1]/a')))
                author = author_element.text
            except Exception as e:
                print(f" 작가 추출 실패: {str(e)}")
                author = "N/A"
                
            # 3. 조회수, 추천수 추출
            try:
                line_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, 'counter-line-a')))
                line = line_element.text

                view = line.split()[0][2:]
                recommend = line.split()[1][2:]

            except Exception as e:
                print(f" 조회수, 추천수 추출 실패: {str(e)}")
                view = "N/A"
                recommend = "N/A"
        
            # 4. 태그 추출
            try:
                tag_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, 'writer-tag')))
                tag = tag_element.text
            except Exception as e:
                print(f" 태그 추출 실패: {str(e)}")
                tag = "N/A"
            
            # ... 이하 모든 요소에 대해 동일하게 적용
            # description, views, recommendations, episodes
            # ...

            # 5. 소개글 추출
            try:
                synopsis_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, 'synopsis')))
                synopsis = synopsis_element.text

            except Exception as e:
                print(f" 소개글 추출 실패: {str(e)}")
                synopsis = "N/A"

            # 6. 회차 추출
            try:
                count_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, 'info-count2')))
                count = count_element.text.splitlines()[2].replace('회차',"").replace('회차', "")
            except Exception as e:
                print(f" 회차 추출 실패: {str(e)}")
                count = "N/A"
            
            # 7. 이미지 추출
            try:
                img_element = wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, '.cover_img.s_inv')))
                img = img_element.get_attribute("src")
            except Exception as e:
                print(f" 회차 추출 실패: {str(e)}")
                img = "N/A"

            novel_info = {
                "url": novel_url,
                "title": title,
                "author": author,
                "view" : view,
                "recommend" : recommend,
                "tag" : tag,
                "synopsis" : synopsis,
                "count" : count,
                "img" : img
            }
            scraped_data.append(novel_info)
            print(novel_info)
        except Exception as e:
            print(f" 작품 ID {novel_id} 크롤링 중 치명적인 오류 발생: {str(e)}")
            continue
            
            #
    print("\n" + "=" * 80)
    print("Individual Novel Page Crawling Complete!")
    print("=" * 80)
    return scraped_data

def main():
    NovelpiaURL = "https://novelpia.com/top100#more700"
    
    options = setup_chrome_options()
    driver = webdriver.Chrome(options=options)
    
    try:
        print("페이지 로딩 중...")
        driver.get(NovelpiaURL)
        
        wait = WebDriverWait(driver, 20)
        
        container = wait.until(EC.presence_of_element_located((By.XPATH, '//*[@id="top100_page"]')))
        
        print(" 컨테이너 발견 및 작품 정보 추출 시작...")
        
        novel_elements = container.find_elements(By.XPATH, ".//div[contains(@class, 'mobile_show') or contains(@class, 'novelbox')]")
        
        print(f"총 {len(novel_elements)}개의 작품을 찾았습니다.")
        novel_elements = novel_elements[0::2]
        novel_data = []
        for novel in novel_elements:
            try:
                title = novel.find_element(By.CLASS_NAME, "cut_line_one").text.strip()
                onclick_attr = novel.find_element(By.XPATH, ".//div[@onclick]").get_attribute('onclick')
                novel_id = re.search(r'location=\'/novel/(\d+)\';', onclick_attr).group(1)
                novel_data.append({"title": title, "id": novel_id})
            except Exception:
                continue
        
        # Crawl each individual novel page using the collected IDs
        final_scraped_data = crawl_individual_novels(driver, novel_data)
        
        print("\nFinal scraped data:")
        for item in final_scraped_data:
            print(item)
        
        if final_scraped_data:
            csv_file = 'novel_data_remote.csv'
            csv_columns = final_scraped_data[0].keys()
            
            with open(csv_file, 'w', newline='', encoding='utf-8') as f:
                writer = csv.DictWriter(f, fieldnames=csv_columns)
                writer.writeheader()
                for data in final_scraped_data:
                    writer.writerow(data)
            print(f"\n 데이터가 {csv_file} 파일로 성공적으로 저장되었습니다.")
        else:
            print("\n 저장할 데이터가 없습니다.")

    except Exception as e:
        print(f" 에러 발생: {str(e)}")
        
    finally:
        print("\n브라우저 종료 중...")
        driver.quit()
        print(" 완료!")

if __name__ == "__main__":
    main()