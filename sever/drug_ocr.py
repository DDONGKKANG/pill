

import json
import cv2
import requests

class Ocr :
    def __init__(self,imagePath):

        self.imagePath = imagePath
        self.image = cv2.imread(imagePath)
        self.LIMIT_PX = 1024
        self.LIMIT_BYTE = 1024*1024  # 1MB
        self.LIMIT_BOX = 40

        self.appkey = 'a74042e884c98b944d02c7e31d105220'

    def ocr_resize(self,image_path: str):
        """
        ocr detect/recognize api helper
        ocr api의 제약사항이 넘어서는 이미지는 요청 이전에 전처리가 필요.

        pixel 제약사항 초과: resize
        용량 제약사항 초과  : 다른 포맷으로 압축, 이미지 분할 등의 처리 필요. (예제에서 제공하지 않음)

        :param image_path: 이미지파일 경로
        :return:
        """
        image = cv2.imread(image_path)
        height, width, _ = image.shape

        if self.LIMIT_PX < height or self.LIMIT_PX < width:
            ratio = float(self.LIMIT_PX) / max(height, width)
            image = cv2.resize(image, None, fx=ratio, fy=ratio)
            height, width, _ = height, width, _ = image.shape

            return image
        return None

    def ocr_detect(self,image: str, appkey: str):
        """
        detect api request example
        :param image_path: 이미지파일 경로
        :param appkey: 앱 REST API 키
        """
        API_URL = 'https://kapi.kakao.com/v1/vision/text/detect'

        headers = {'Authorization': 'KakaoAK {}'.format(appkey)}

        jpeg_image = cv2.imencode(".jpg", image)[1]
        data = jpeg_image.tobytes()

        return requests.post(API_URL, headers=headers, files={"file": data})

    def ocr_recognize(self,image: str, boxes: list, appkey: str):
        """
        recognize api request example
        :param boxes: 감지된 영역 리스트. Canvas 좌표계: 좌상단이 (0,0) / 우상단이 (limit,0)
                        감지된 영역중 좌상단 점을 기준으로 시계방향 순서, 좌상->우상->우하->좌하
                        ex) [[[0,0],[1,0],[1,1],[0,1]], [[1,1],[2,1],[2,2],[1,2]], ...]
        :param image_path: 이미지 파일 경로
        :param appkey: 앱 REST API 키
        """
        API_URL = 'https://kapi.kakao.com/v1/vision/text/recognize'

        headers = {'Authorization': 'KakaoAK {}'.format(appkey)}

        jpeg_image = cv2.imencode(".jpg", image)[1]
        data = jpeg_image.tobytes()

        return requests.post(API_URL, headers=headers, files={"file": data}, data={"boxes": json.dumps(boxes)})

    def draw_box(self,image, json_result):
        for obj in json_result['result']['boxes']:
            cv2.line(image, (obj[0][0], obj[0][1]), (obj[1][0], obj[1][1]), (0, 0, 255), 2)
            cv2.line(image, (obj[1][0], obj[1][1]), (obj[2][0], obj[2][1]), (0, 0, 255), 2)
            cv2.line(image, (obj[2][0], obj[2][1]), (obj[3][0], obj[3][1]), (0, 0, 255), 2)
            cv2.line(image, (obj[3][0], obj[3][1]), (obj[0][0], obj[0][1]), (0, 0, 255), 2)

        return image

    def run(self):
        print("\n")
        print("*****[System] ocr Start*****")
        resize_image = self.ocr_resize(self.imagePath)
        image = self.image
        if resize_image is not None:
            image = resize_image
            print("[System] 원본 대신 리사이즈된 이미지를 사용합니다.")

        # 문자영역 검색
        output = self.ocr_detect(image, self.appkey).json()

        boxes = output["result"]["boxes"]
        boxes = boxes[:min(len(boxes), self.LIMIT_BOX)]
        # 문자 인식
        output = self.ocr_recognize(image, boxes, self.appkey).json()

        if len(output) != 1:
            return 0, 0
        num =len(output['result']['recognition_words'])

        if num==0 :
            return 0, 0

        ans = []
        for i in range(num) :
            if not output['result']['recognition_words'][i] :
                num = num-1
                if num == 0 :
                    return 0, 0
                continue
            ans.append(output['result']['recognition_words'][i])

        print("*****[System] ocr End*****")
        print("\n")

        return ans, num # duck0922


