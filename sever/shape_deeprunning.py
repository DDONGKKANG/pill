
import numpy as np
import tensorflow as tf
import time


class Deeprunning():

    def __init__(self,img):
        self.imagePath = img                                     # 추론을 진행할 이미지 경로
        self.modelFullPath = '/tmp/output_graph.pb'              # 읽어들일 graph 파일 경로
        self.labelsFullPath = '/tmp/output_labels.txt'           # 읽어들일 labels 파일 경로


    def create_graph(self):
        """저장된(saved) GraphDef 파일로부터 graph를 생성하고 saver를 반환한다."""
        # 저장된(saved) graph_def.pb로부터 graph를 생성한다.
        print("[System] 그래프 생성")
        start_vect = time.time()
        with tf.gfile.FastGFile(self.modelFullPath, 'rb') as f:
            graph_def = tf.GraphDef()
            graph_def.ParseFromString(f.read())
            _ = tf.import_graph_def(graph_def, name='')
        print("[System] create_graph : %0.2f Minutes" % ((time.time() - start_vect) / 60))


    def run_inference_on_image(self):

        print("\n")
        print("*****[System] 추론 시작*****")

        answer = None
        if not tf.gfile.Exists(self.imagePath):
            tf.logging.fatal('File does not exist %s', self.imagePath)
            return answer

        image_data = tf.gfile.FastGFile(self.imagePath, 'rb').read()

        # 저장된(saved) GraphDef 파일로부터 graph를 생성한다.
        self.create_graph()

        with tf.Session() as sess:
            start_vect = time.time()
            softmax_tensor = sess.graph.get_tensor_by_name('final_result:0')
            predictions = sess.run(softmax_tensor,
                                   {'DecodeJpeg/contents:0': image_data})
            predictions = np.squeeze(predictions)

            top_k = predictions.argsort()[-5:][::-1]  # 가장 높은 확률을 가진 5개(top 5)의 예측값(predictions)을 얻는다.
            f = open(self.labelsFullPath, 'rb')
            lines = f.readlines()
            labels = [str(w.decode()).replace("\n", "") for w in lines]
            print("[System] decode Runtime : %0.2f Minutes" % ((time.time() - start_vect) / 60))

            print("[System] 딥 러닝 : 상위 5개 결과 확인")
            answer = []
            score_list = [] # duck
            num = 1 #duck0922
            for node_id in top_k:
                human_string = labels[node_id]
                answer.append(human_string)
                score = predictions[node_id]
                score_list.append(score) # duck0922
                print(num,'> %s (score = %.5f)' % (human_string, score))
                num += 1
            print("*****[System] shape_Deep Running 종료*****")
            print("\n")

            return answer, max(score_list) #duck0922

