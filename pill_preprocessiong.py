# import the necessary packages
import cv2
import numpy as np
import math
import os

def step1_getpillarea(image) :
    """
    step1-알약의 주변부 찾기
    :param image: 물체를 찾을 사진의 이미지
    :return res : 찾아낸 물체 사진
    """
    #원본 이미지 사진 크기 재조정
    imageHeight, imageWidth = image.shape[:2]
    resizeHeight = int(1 * imageHeight)
    resizeWidth = int(1 * imageWidth)
    #if (imageHeight > 700): resizeHeight = 700
    #if (imageWidth > 700): resizeWidth = 700
    #resizeImage = cv2.resize(image, (resizeHeight, resizeWidth), interpolation=cv2.INTER_CUBIC)  # Resize image 보간법 이용
    resizeImage = image

    # initialize OpenCV's static fine grained saliency detector and
    # compute the saliency map
    saliency = cv2.saliency.StaticSaliencyFineGrained_create()
    (success, saliencyMap) = saliency.computeSaliency(resizeImage)
    saliencyMap = (saliencyMap * 255).astype("uint8")  # 0-1사이의 값을 0-255의 값으로 변환

    #cv2.imshow("saliency Map",cv2.resize(saliencyMap, (500, 700), interpolation=cv2.INTER_CUBIC))
    # if we would like a *binary* map that we could process for contours,
    # compute convex hull's, extract bounding boxes, etc., we can
    # additionally threshold the saliency map
    threshMap = cv2.threshold(saliencyMap.astype("uint8"), 0, 255,
                              cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]


    # 이진화 이미지의 윤곽선을 검색
    # cv2.findContours(이진화 이미지, 검색 방법, 근사화 방법) return 윤곽선, 계층 구조
    contours, hierachy = cv2.findContours(threshMap, cv2.RETR_LIST, cv2.CHAIN_APPROX_NONE)
    #cv2.drawContours(resizeImage, contours, 0, (0, 0, 255), 2)
    area, output = 0, contours[0]
    #가장 큰 윤곽선 색
    for cnt in contours:
        cv2.drawContours(threshMap, cnt, 0, (0, 0, 255), 2)
        if (area < cv2.contourArea(cnt)):
            area = cv2.contourArea(cnt)
            output = cnt

    # 근사치 구하기
    epsilon = 0.02 * cv2.arcLength(output, True)
    approx = cv2.approxPolyDP(output, epsilon, True)
    #cv2.drawContours(resizeImage, [approx], 0, (0, 0, 255), 2)
    #cv2.imshow("re",cv2.resize(resizeImage, (500, 700), interpolation=cv2.INTER_CUBIC))
    #찾은 물약의 근사치 좌표 구하기
    x, y, w, h = cv2.boundingRect(approx)
    rx = x
    ry = y
    if (x > 30):
        rx = x - 30
    if (y > 100):
        ry = y - 100
    #cv2.rectangle(resizeImage, (rx, ry), (x + w + 100, y + h + 100), (0, 255, 0), 2)

    #사진 자르기
    dst = resizeImage[ry:y + h + 100,rx:x + w + 100]  # NOTE: its img[y: y + h, x: x + w] and *not* img[x: x + w, y: y + h]]

    return dst

def step2_del_shadow(img):
    height, width, channel = img.shape # split channel

    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV) # change RGB to HSV
    result_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    h, s, v = cv2.split(hsv) # separate channel

    # make empty np array
    B3 = np.zeros((height, width))
    notB3 = np.zeros((height, width))
    B5 = np.zeros((height, width))
    tmp_v = np.zeros((height,width))

    sum1 = 0
    sum2 = 0

    for i in range(height):
        for j in range(width):
            tmp_v[i][j] = v[i][j] / 255
            # max 1, min 0

            if(tmp_v[i][j] < 0.3):
                B3[i][j] = 1
                notB3[i][j] = 0
            else:
                B3[i][j] = 0
                notB3[i][j] = 1

            if(0.3 <= tmp_v[i][j] <= 0.7):
                B5[i][j] = 1
            else:
                B5[i][j] = 0

            sum1 += notB3[i][j] * tmp_v[i][j]
            sum2 += notB3[i][j]


    # Calculate m3 _ modify
    m3 = sum1 / sum2
    # Calculate Bw
    Bw = B3 + 0.7*B5

    # Gaussian Filter
    dim1 = 2*4*2 + 1
    dim2 = 2*4*20 + 1

    Gv = cv2.GaussianBlur(tmp_v,(dim1, dim1),2) # V에 Gaussian Filter
    Gw = cv2.GaussianBlur(Bw,(dim2, dim2),20) # Bw에 Gaussian Filter

    result_v = np.zeros((height,width))
    for x in range(height):
        for y in range(width):
            result_v[x][y] = tmp_v[x][y] + (m3 - Gv[x][y]) * Gw[x][y]
            result_hsv[x][y][2] = result_v[x][y] * 255
    result = cv2.cvtColor(result_hsv, cv2.COLOR_HSV2BGR)

    return result

def step3_extractpill(img):
    # gray -> adaptive Threshold
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    adap = cv2.adaptiveThreshold(gray, 255, cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY, 21, -1)
    ero = cv2.erode(adap, None, iterations = 1)

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

    return mask, masked

def step4_matchshape(img):
    min = 1.0
    imgfile_list = ['drug_shape/hexagon.png',
                    'drug_shape/oval_curve.png',
                    'drug_shape/oval_rect.png',
                    'drug_shape/pentagon.png',
                    'drug_shape/round.png',
                    'drug_shape/square.png',
                    'drug_shape/triangle.png',
                    'drug_shape/u-shaped.png']

    wins = map(lambda x: 'img' + str(x+1), range(8))
    wins = list(wins)
    imgs = []
    contour_list = []

    i = 0
    for imgfile in imgfile_list:
        img = cv2.imread(imgfile, cv2.IMREAD_GRAYSCALE)
        imgs.append(img)

        ret, thr = cv2.threshold(img, 127, 255, 0)
        contours, _ = cv2.findContours(thr, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contour_list.append(contours[0])
        i += 1

    for i in range(8):
        ret = cv2.matchShapes(mask, contour_list[i], 1, 0.0)
        if ( min > ret ) :
            min = ret
            minval = i

        print(ret)

    print("min" + str(min))

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


if __name__ == "__main__":

    #파일 읽어오기(이미지 1개)
    # image = cv2.imread("gg1.jpg")
    # cv2.imshow("original", image)

    #폴더로 읽어오기
    cur_path = os.getcwd()
    print(cur_path)

    path= os.path.join(cur_path,"pill_image") #pill_image = 약 이미지 폴더
    imagepaths_list = [os.path.join(path, file_name) for file_name in os.listdir(path)]
    #print(imagepaths_list)
    a = 1
    for i in imagepaths_list :
        print(a)
        image = cv2.imread(i)

        # step1 실행
        print("step1 start")
        step1 = step1_getpillarea(image)
        # cv2.imshow("step1", step1)

        # step2 실행
        print("step2 start")
        step2 = step2_del_shadow(step1)
        # cv2.imshow("step2", step2)

        # step3 실행
        print("step3 start")
        mask, step3 = step3_extractpill(step2)
        outpath = os.path.join(cur_path,"step3")
        cv2.imwrite(os.path.join(outpath,str(a)+".jpg"), step3)
        #cv2.imshow("step3", step3)

        # step4 실행
        print("step4 start")
        step4 = step3

        # step5 실행
        print("step5 start")
        tmp = step4
        tmp = step5_1_grayscale(tmp)
        # tmp = step5_2_intensity(tmp)
        tmp = step5_3_magnitude(tmp)
        tmp = step5_4_otsu(tmp)
        check_label = step5_5_imshow_components(tmp)
        # cv2.imshow("check_label", check_label)
        step5, check = step5_6_contours(tmp, step4)
        if check == 2:
            print("done!")
            outpath = os.path.join(cur_path, "step5")
            cv2.imwrite(os.path.join(outpath, str(a) + ".jpg"), step5)

        a = a + 1

    cv2.waitKey(0)





