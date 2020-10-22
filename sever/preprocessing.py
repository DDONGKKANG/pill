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
    

    def mean_squared_error(y, t):
        return ((y-t)**2).mean(axis=None)

    def step4_matchshape(img):
        desc = ZernikeMoments(21)
        index = {}

        # =============== drug_shape(6가지) 이미지파일 Zernike moment
        drug_list = [
            'drug_shape/oval_curve.png',
            'drug_shape/oval_rect.png',
            'drug_shape/pentagon.png',
            'drug_shape/round.png',
            'drug_shape/square.png',
            'drug_shape/capsule.png'
        ]
        drug_imgs = []
        contour_list = []
        i = 0

        for drug_img in drug_list:

            sample = cv2.imread(drug_img)
            #drug_imgs.append(sample)

            gray = cv2.cvtColor(sample, cv2.COLOR_BGR2GRAY)
            index[i] = desc.describe(gray)
            i += 1

        '''
        for i in range(0, 6):
            print(drug_list[i])
            for j in range(25):
                print(index[i][j])
            print('--------------------')
        '''

        # =============== img파일 Zernike moment

        # gray
        #gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        # Zernike moment
        moments = desc.describe(img)


        '''
        print('=========================================')
        for i in range(0, 25):
            print(moments[i])
        '''

        S = []
        for i in range(len(index)):
            S.append(mean_squared_error(np.array(moments), np.array(index[i])))

        print(S)
        #print("np.min(S) :", str(np.min(S)))
        print("np.argmin(S) :", str(np.argmin(S)))
        print("min :", str(drug_list[np.argmin(S)]))

    def step5_1_grayscale(img):
        """
        step5_1-이미지 회색처리
        새로운 폴더에 저장했다가 불러올 때 회색으로 불러옴.
        :param img:
        :return:
        """
        path = os.getcwd()
        cv2.imwrite(os.path.join(path, 'step4.jpg'), img)
        img = cv2.imread(os.path.join(path, 'step4.jpg'), cv2.IMREAD_GRAYSCALE)
        return img

    def step5_2_intensity(img):
        """
        step5_2-밝기 조절
        :param img:
        :return:
        """
        for i in range(0, img.shape[0]):
            for j in range(0, img.shape[1]):
                #print(i,j,img[i,j])
                if img[i, j] > 225:
                    img[i, j] = 235
                    continue
                elif img[i, j] > 200:
                    img[i, j] += 10
                    continue
                elif img[i, j] > 150:
                    img[i, j] += 11
                    continue
                elif img[i, j] > 100:
                    img[i, j] += 20
                    continue
                elif img[i, j] > 50:
                    img[i, j] += 30
                    continue
                elif img[i, j] < 20:
                    img[i, j] = 0
                    continue
                img[i, j] += 50
        return img

    def step5_3_magnitude (img):
        """
        step5_3-Gaussian Filter
        :param img:
        :return:
        """
        img = cv2.GaussianBlur(img, (15, 15), 0)
        sobelx = cv2.Sobel(img, cv2.CV_64F, 1, 0, ksize=5)
        sobely = cv2.Sobel(img, cv2.CV_64F, 0, 1, ksize=5)
        dxabs = cv2.convertScaleAbs(sobelx)
        dyabs = cv2.convertScaleAbs(sobely)
        img = cv2.addWeighted(dxabs, 0.7, dyabs, 0.7, 0)
        return img

    def step5_4_otsu(img):
        """
        step5_4-블러처리 후 이진화
        :param img:
        :return:
        """
        blur = cv2.GaussianBlur(img, (11, 11), 0)
        ret, img = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        return img

    def step5_5_imshow_components(img):
        """
        step5_5-테스트용 라벨링
        step5_5는 사실 확인용임 실제 이미지에 적용되면 안됨.
        :param img:
        :return:
        """
        # image connected component
        ret, labels = cv2.connectedComponents(img)

        # Map component labels to hue val
        label_hue = np.uint8(179*labels/np.max(labels))
        blank_ch = 255*np.ones_like(label_hue)
        labeled_img = cv2.merge([label_hue, blank_ch, blank_ch])

        # cvt to BGR for display
        labeled_img = cv2.cvtColor(labeled_img, cv2.COLOR_HSV2BGR)

        # set bg label to black
        labeled_img[label_hue==0] = 0

        return labeled_img

    def step5_6_contours(img,src):
        """
        step5_6-Bounding Box추출
        적절한 contours를 찾아서 Bounding Box 그리기
        :param img:
        :param src:
        :return:
        """
        c, h = cv2.findContours(img, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
        a1 = 0
        a2 = 0
        a3 = 0
        a4 = 0
        a5 = 0

        for i in range(len(c)):
            cnt = c[i]
            # Aspect Ratio < 3
            if (1.0 * cnt.shape[1] / cnt.shape[0]) < 3:
                a1 += 1
            else:
                continue

            # 100 < Area < 20000
            area = cv2.contourArea(cnt)
            if 100 < area < 20000:
                a2 += 1
            else:
                continue

            # Eccentricity < 0.995
            (fx, fy), (MA, ma), angle = cv2.fitEllipse(cnt)
            fa = ma / 2
            fb = MA / 2
            eccentricity = math.sqrt(pow(fa, 2) - pow(fb, 2))
            eccentricity = round(eccentricity / fa, 2)

            if eccentricity < 0.995:
                a3 += 1
            else:
                continue

            # Solidity > 0.3
            hull = cv2.convexHull(cnt)
            hull_area = cv2.contourArea(hull)
            solidity = float(area) / hull_area
            if solidity > 0.3:
                a4 += 1
            else:
                continue

            # 0.2 < Extent< 0.9
            x, y, w, h = cv2.boundingRect(cnt)
            rect_area = w * h
            extent = float(area) / rect_area

            if 0.2 < extent < 0.9:
                a5 += 1
            else:
                continue

            rect = cv2.minAreaRect(cnt)
            box = cv2.boxPoints(rect)
            box = np.int0(box)
            result_img = cv2.drawContours(src, [box], 0, (0, 0, 255), 2)

        return result_img, a5

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

