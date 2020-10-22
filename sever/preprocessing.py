# import the necessary packages
import cv2
import numpy as np

class Preprocessiong :
    def __init__(self, imagePath):
        self.image = cv2.imread(imagePath)

    def step1_getpillarea(self):
        """
        step1-알약의 주변부 찾기
        :param image: 물체를 찾을 사진의 이미지
        :return res : 찾아낸 물체 사진
        """

        # 원본 이미지 사진 크기 재조정
        resizeImage = self.image

        # initialize OpenCV's static fine grained saliency detector and
        # compute the saliency map
        saliency = cv2.saliency.StaticSaliencyFineGrained_create()
        (success, saliencyMap) = saliency.computeSaliency(resizeImage)
        saliencyMap = (saliencyMap * 255).astype("uint8")  # 0-1사이의 값을 0-255의 값으로 변환

        # if we would like a *binary* map that we could process for contours,
        # compute convex hull's, extract bounding boxes, etc., we can
        # additionally threshold the saliency map
        threshMap = cv2.threshold(saliencyMap.astype("uint8"), 0, 255,
                                  cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]

        # 이진화 이미지의 윤곽선을 검색
        # cv2.findContours(이진화 이미지, 검색 방법, 근사화 방법) return 윤곽선, 계층 구조
        contours, hierachy = cv2.findContours(threshMap, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)

        area, output = 0, contours[0]
        # 가장 큰 윤곽선 색
        for cnt in contours:
            if (area < cv2.contourArea(cnt)):
                area = cv2.contourArea(cnt)
                output = cnt

        # 근사치 구하기
        epsilon = 0.02 * cv2.arcLength(output, True)
        approx = cv2.approxPolyDP(output, epsilon, True)

        # 찾은 물약의 근사치 좌표 구하기
        x, y, w, h = cv2.boundingRect(approx)
        rx = x
        ry = y
        if (x > 200):
            rx = x - 200
        if (y > 200):
            ry = y - 200

        # 사진 자르기
        dst = resizeImage[ry:y + h + 200,
              rx:x + w + 200]  # NOTE: its img[y: y + h, x: x + w] and *not* img[x: x + w, y: y + h]]

        cv2.imwrite("step1.jpg", dst)  # duck0922
        return dst

    def step2_del_shadow(self,img):
        height, width, channel = img.shape  # split channel

        hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)  # change RGB to HSV
        result_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        h, s, v = cv2.split(hsv)  # separate channel

        # make empty np array
        B3 = np.zeros((height, width))
        notB3 = np.zeros((height, width))
        B5 = np.zeros((height, width))
        tmp_v = np.zeros((height, width))

        sum1 = 0
        sum2 = 0

        tmp_v = v / 255
        # max 1, min 0

        B3 = tmp_v < 0.3
        notB3 = tmp_v >= 0.3
        B5 = (0.3 <= tmp_v) == (tmp_v <= 0.7)

        sum1 = sum(sum(notB3 * tmp_v))
        sum2 = sum(sum(notB3))

        # Calculate m3 _ modify
        m3 = sum1 / sum2
        # Calculate Bw
        Bw = B3 + 0.7 * B5

        # Gaussian Filter
        dim1 = 2 * 4 * 2 + 1;
        dim2 = 2 * 4 * 20 + 1

        Gv = cv2.GaussianBlur(tmp_v, (dim1, dim1), 2)  # V에 Gaussian Filter
        Gw = cv2.GaussianBlur(Bw, (dim2, dim2), 20)  # Bw에 Gaussian Filter

        result_v = np.zeros((height, width))
        result_v = tmp_v + (m3 - Gv) * Gw

        for x in range(height):
            for y in range(width):
                result_hsv[x][y][2] = result_v[x][y] * 255
                # must modify *************

        result = cv2.cvtColor(result_hsv, cv2.COLOR_HSV2BGR)

        cv2.imwrite("step2.jpg", result) #duck0922
        return result

    def step3_extractpill(self,img):
        # gray -> adaptive Threshold
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        adap = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 21, -1)
        ero = cv2.erode(adap, None, iterations=1)

        # 컨투어 검출
        contour_info = []
        contours, hierarchy = \
            cv2.findContours(adap.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        ## 컨투어 최대값 찾기
        for c in contours:
            contour_info.append((
                c,
                cv2.isContourConvex(c),
                cv2.contourArea(c),
            ))
        contour_info = sorted(contour_info, key=lambda c: c[2], reverse=True)
        max_contour = contour_info[0]

        # mask is black, polygon is white
        mask = np.zeros(ero.shape)
        cv2.fillConvexPoly(mask, max_contour[0], (255))

        # -- Smooth mask, then blur it --------------------------------------------------------
        mask = cv2.dilate(mask, None, iterations=10)
        mask = cv2.erode(mask, None, iterations=10)
        mask = cv2.GaussianBlur(mask, (5, 5), 0)
        mask_stack = np.dstack([mask] * 3)  # Create 3-channel alpha mask

        # -- Blend masked img into MASK_COLOR background --------------------------------------
        mask_stack = mask_stack.astype('float32') / 255.0  # Use float matrices,
        img = img.astype('float32') / 255.0  # for easy blending

        masked = (mask_stack * img) + ((1 - mask_stack) * (0.0, 0.0, 0.0))  # Blend
        masked = (masked * 255).astype('uint8')  # Convert back to 8-bit

        cv2.imwrite("step3.jpg", masked)
        return mask, masked

    def run(self):
        print("\n`")
        print("*****[System] Pre-processiong Start*****")
        step1 = self.step1_getpillarea()
        print('[System] Step 1 End')
        step2 = self.step2_del_shadow(step1)
        print('[System] Step 2 End')
        mask, step3 = self.step3_extractpill(step2)
        print('[System] Step 3 End')
        print("*****[System] Pre-processiong End*****")
        print("\n`")

