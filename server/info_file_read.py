# -*- coding: utf-8 -*-
import csv

class read_data():
    def __init__(self, result_m_name):
        self.m_name = result_m_name

    def read(self):
        f = open('drug_data.csv', 'r', encoding='euc-kr')
        data_medicine = csv.reader(f)
        # i번째 레코드의 '품목명'이 '스파민정'인지 확인
        for i in data_medicine:
            if i[1] == self.m_name:  # csv 파일과 추출한 이름이 일치 X // 괄호때문
                return i