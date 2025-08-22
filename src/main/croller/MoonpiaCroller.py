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

    for url in novel_list:
        try:
            print(f"작품 페이지 접속 중, url : {url}")
            driver.get(url)
            wait = WebDriverWait(driver, 20)

            try:
                title_element = wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, "div.title-wrap a:first-child")))
                title = title_element.get_attribute('title')

            except Exception as e :
                print(f"제목 추출 실패 : {str(e)}")
                title = "N/A"
            
            try:
                author_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, "member-trigger")))
                author = author_element.text
            except Exception as e :
                print(f"작가 추출 실패 : {str(e)}")
                author = "N/A"

            try:
                episodes = "N/A"
                views = "N/A"
                recommend = "N/A"
                meta_container = wait.until(EC.visibility_of_all_elements_located((By.CLASS_NAME, 'meta-etc')))
                dd_elements = meta_container[1].find_elements(By.TAG_NAME, 'dd')
                if len(dd_elements) > 0:
                    episodes = dd_elements[0].text.strip()
                if len(dd_elements) > 1:
                    views = dd_elements[1].text.strip()
                if len(dd_elements) > 2:
                    recommend = dd_elements[2].text.strip()
                    
            except Exception as e:
                print(f"❌ meta-etc 정보 추출 실패: {str(e)}")
            
            try:
                synopsis_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, "story")))
                synopsis = synopsis_element.text
            except Exception as e:
                print(f"❌ 소개글 정보 추출 실패: {str(e)}")
            
            try:
                tag_element = wait.until(EC.visibility_of_element_located((By.CLASS_NAME, "tag-list")))
                tag = tag_element.text
            except Exception as e:
                print(f"❌ 태그 정보 추출 실패: {str(e)}")

            try:
                img_element = wait.until(EC.visibility_of_element_located((By.XPATH, '//*[@id="board"]/div[1]/div[1]/img')))
                img = img_element.get_attribute("src")
            except Exception as e:
                print(f"❌ 태그 정보 추출 실패: {str(e)}")

            novel_info = {
                "id" : url,
                "title" : title,
                "author" : author,
                "episode" : episodes,
                "view" : views,
                "synopsis" : synopsis,
                "recommend" : recommend,
                "tag" : tag,
                "img" : img
            }

            print(novel_info)
        except Exception as e:
            print("오류")
            continue
    
    return scraped_data
def main():
    MoonpiaURL = "https://www.munpia.com/page/j/view/w/best/plsa.eachtoday?displayType="
    
    options = setup_chrome_options()
    driver = webdriver.Chrome(options=options)
    novel_list = list()
    try:
        driver.get(MoonpiaURL)
        novel_elements = driver.find_elements(By.CLASS_NAME, 'novel-wrap')
        
        for element in novel_elements:
            url = element.get_attribute("href")
            novel_list.append(url)


        final_scraped_data = crawl_individual_novels(driver, novel_list)
        
        if final_scraped_data:
            csv_file = 'novel_data.csv'
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
        print(f"오류가 발생했습니다: {e}")
        
    finally:
        # 이 부분을 제거하거나 주석 처리하면 브라우저가 닫히지 않습니다.
        # driver.quit()  
        print("\n브라우저를 종료하지 않습니다.")
    input()
if __name__ == "__main__":
    main()