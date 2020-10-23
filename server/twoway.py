# -*- coding: utf-8 -*-
import re
from difflib import SequenceMatcher


class Twoway:
    def __init__(self, shape, max_score, ocr, check): #duck0922
        self.shape= shape
        self.max_score = max_score #duck0922
        self.ocr = ocr
        self.check = check

    def twoway_shape(self) :
        shape = self.shape

        m_name = []
        m_char = []

        for i in shape :
            tmp = i.split('_')
            m_name.append(tmp[0])
            m_char.append(tmp[1])

        return m_name, m_char

    def twoway_ocr(self):
        ocr = self.ocr
        result = ''

        for i in ocr:
            # 특문 없애기
            regular = re.sub('[^0-9a-zA-Z]','',i)
            tmp = regular.lower() # 소문자로 바꾸기
            tmp += result
            result = tmp

        return result

    def twoway_processing(self):
        shape_score = self.max_score
        print("\n`")
        print("*****[System] Twoway Start*****")
        shape_name, shape_char = self.twoway_shape()  # ['메부틴정' : 'gj', '스파민정' : 's80', ...]

        if self.ocr == 0 or self.check == 0:
            if shape_score < 0.52:
                print("[System] low score and ocr notfound : ", self.ocr)
                return 'notfound'
            else:
                print("[System] high score and ocr notfound : ", self.ocr)
                return shape_name[0]

        result_ocr = self.twoway_ocr()  # ['gj']

        # compare ocr, shape
        for i in range(len(shape_char)):
            if (shape_char[i] == result_ocr):
                print("[System] perfectly include ocr : ", self.ocr)
                return shape_name[i]  # 각인 문자와 완벽하게 일치
        result = []

        # result_ocr과 일치하는 incep_char가 없을 경우
        # shape result와 ocr result 유사도 계산
        for i in range(len(shape_char)):
            tmp = SequenceMatcher(a=shape_char[i], b=result_ocr).quick_ratio()
            result.append(tmp)

        # inception수치가 낮아 질수록 ocr 비중이 커짐
        if shape_score > 0.7 :
           if max(result) > 0.8 :
               print("[System] shape1-ocr1 : ",shape_score, self.ocr)
               return shape_name[result.index(max(result))]
           else :
               print("[System] shape1-ocr2 : ",shape_score, self.ocr)
               return shape_name[0]
        elif shape_score <= 0.7 and shape_score> 0.5  :
           if max(result) > 0.5 :
               print("[System] shape2-ocr1 : ",shape_score, self.ocr)
               return shape_name[result.index(max(result))]
           else :
               print("[System] shape2-ocr2 : ",shape_score, self.ocr)
               return shape_name[0]
        elif shape_score <= 0.5 and shape_score> 0.35  :
           if max(result) > 0.33 :
               print("[System] shape3-ocr1 : ",shape_score, self.ocr)
               return shape_name[result.index(max(result))]
           else :
               print("[System] shape3-ocr2 : ",shape_score, self.ocr)
               return shape_name[0]
        else :#마지노선
            if max(result) > 0.5 :
                print("[System] shape4-ocr1 : ", shape_score, self.ocr)
                return shape_name[result.index(max(result))]
            else :
                print("[System] low score and not include ocr : ", self.ocr)
                return 'notfound'

