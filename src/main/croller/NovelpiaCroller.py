from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.options import Options
import time
import re

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
                
            # 2. 조회수, 추천수 추출
            try:
                view_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[5]/div[1]')))
                view = view_element.text
                if "조회" in view and "추천" in view:
                    data_list = view.split()
                    view = re.sub(r'[^0-9,]', '', data_list[0]).replace(',', '')
                    recommend = re.sub(r'[^0-9,]', '', data_list[1]).replace(',', '')
                else:
                    view_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[4]/div[1]')))
                    view = view_element.text

                    data_list = view.split()

                    view = re.sub(r'[^0-9,]', '', data_list[0]).replace(',', '')
                    recommend = re.sub(r'[^0-9,]', '', data_list[1]).replace(',', '')

            except Exception as e:
                print(f" 조회수, 추천수 추출 실패: {str(e)}")
                view = "N/A"
                recommend = "N/A"
        
            # 2. 태그 추출
            try:
                tag_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[5]/div[1]/p[1]')))
                tag = tag_element.text
                if "조회" in tag in tag:
                    tag_element = wait.until(EC.visibility_of_element_located((By.XPATH, '/html/body/div[6]/div[1]/div[2]/div[6]/div[1]/p[1]')))
                    tag = tag_element.text
                tag = tag.split("\n#")
            except Exception as e:
                print(f" 태그 추출 실패: {str(e)}")
                tag = "N/A"
        
            # ... 이하 모든 요소에 대해 동일하게 적용
            # description, views, recommendations, episodes
            # ...

            novel_info = {
                "id": novel_id,
                "title": title,
                "author": author,
                "view" : view,
                "recommend" : recommend,
                "tag" : tag,
            }
            scraped_data.append(novel_info)
            print(novel_info)
        except Exception as e:
            print(f" 작품 ID {novel_id} 크롤링 중 치명적인 오류 발생: {str(e)}")
            continue
                
    print("\n" + "=" * 80)
    print("Individual Novel Page Crawling Complete!")
    print("=" * 80)
    return scraped_data

def main():
    NovelpiaURL = "https://novelpia.com/top100/all/today/view/all/all?main_genre="
    
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
        
    except Exception as e:
        print(f" 에러 발생: {str(e)}")
        
    finally:
        print("\n브라우저 종료 중...")
        driver.quit()
        print(" 완료!")

if __name__ == "__main__":
    main()