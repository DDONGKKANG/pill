import socketserver
import threading
import preprocessing
import shape_deeprunning
import drug_ocr
import socket
import twoway
import info_file_read
import warnings

warnings.filterwarnings("ignore")
#HOST = "192.168.0.3"#카페"192.168.0.19"#집"192.168.0.3";
HOST = '173.20.10.2'
PORT = 5000
ADDR = (HOST,PORT)

def ipcheck():
    return socket.gethostbyname(socket.getfqdn())

class MyTcpHandler(socketserver.BaseRequestHandler):

    def handle(self):

        # get socket request
        socket = self.request

        data_transferred = 0.0
        print('[System] [%s] 연결됨' % self.client_address[0])

        print('[System] 파일 수신 시작...')

        # get image file size from client
        file_size = socket.recv(1024)
        socket.send(file_size)#파일 사이즈를 보냄

        if not file_size:
            print('[Error] 서버에 존재하지 않거나 수신중 오류발생')
            return

        data = socket.recv(1024)

        if not data:
            print('[Error] 서버에 존재하지 않거나 수신중 오류발생')
            return

        with open('recv.jpg', 'wb') as f:#전처리 집어 넣으면 "recv.jpg"로 바꿀것
            try:
                while True:
                    f.write(data)
                    data_transferred += len(data)
                    if (data_transferred - (int(file_size.decode())) == 0):
                        break
                    data = socket.recv(1024)
            except Exception as e:
                print("[Error] 파일 수신 중 오류")
                print(e)

        print('[System] 전송완료[%s], 전송량[%d]' % ("test", data_transferred))

        #전처리, 인셉션, ocr실행
        pre_run()
        two_shape, max_score, msg = shape_run() # duck0922
        two_ocr, num = ocr_run() # dj

        # two way processing _ dj
        t_result = twoway.Twoway(two_shape, max_score, two_ocr, num) #duck0922
        twoway_result = t_result.twoway_processing()

        #duck0922
        if(twoway_result == 'notfound'):
            print('[System] notfound')
            notfound = 'notfound'

            #최종결과 추출
            final_result = bytearray(notfound, 'utf-8')  # 전송하기 위한 바이너리화
        else:
            read_data = info_file_read.read_data(twoway_result)
            medicine_info = read_data.read()  # csv에서 찾은 약 정보

            print(">>>> medicine info <<<<")
            print(medicine_info[1])  # dj

            #최종결과 추출
            final_result = make_result(medicine_info)

        socket.send(final_result)#최종결과
        socket.close

        print("\n[System] 현재 소캣 종료\n")
        print("\n[System] wait new socket\n")


def make_result(medicine_info) :
    medicine_info.pop(0)#품목일련번호 삭세
    medicine_info.pop(2)#앞표시 삭제
    medicine_info.pop(2)#뒷표시 삭제
    dst = "///".join(medicine_info)
    result = bytearray(dst, 'utf-8')  # 전송하기 위한 바이너리화

    return result

def pre_run() :
    pre_inst = preprocessing.Preprocessiong('recv.jpg')
    pre_inst.run()


def shape_run() :
    shape_inst = shape_deeprunning.Deeprunning("step3.jpg")
    dst, max_score = shape_inst.run_inference_on_image()#가장 높은 상위 5개 문자 return _ duck0922
    msg = bytearray(dst[0], 'utf-8')#전송하기 위한 바이너리화

    return dst, max_score, msg # duck0922

def ocr_run() :
    ocr_inst = drug_ocr.Ocr("step3.jpg")
    dst, num = ocr_inst.run()#받은 문자들, 문자 개수

    if not dst :
        print("[System] 각인문자 발견 못함")
    elif num == 0 :
        print("[System] 각인문자 발견 못함")
    else:
        print("[System] 각인문자 발견")

    return dst, num # dj


#@logging_time
def runServer():
    print('++++++파일 서버를 시작++++++')
    print("+++파일 서버를 끝내려면 'Ctrl + C'를 누르세요.\n")

    try:
        server = socketserver.TCPServer((HOST, PORT), MyTcpHandler)
        server_thread = threading.Thread(target=server.serve_forever())
        server_thread.setDaemon(True)#보조 쓰래드
        server_thread.start()

    except KeyboardInterrupt:
        print('++++++파일 서버를 종료합니다.++++++')


runServer()



