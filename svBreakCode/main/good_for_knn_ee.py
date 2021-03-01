# coding=utf-8

import os
import sys
import json
import scipy
import time
from model import *
from batch_select import *

import numpy as np
import tensorflow as tf

from gensim import matutils
from scipy  import stats
from tqdm   import tqdm
from collections import OrderedDict
from tensorflow.examples.tutorials.mnist import input_data

def add_noise(y_train,p):
    indices = range(len(y_train))
    np.random.shuffle(indices)
    indices = indices[:int(p*len(y_train))]
    label = range(10)
    for index in indices:
        y_train[index] = 0
        np.random.shuffle(label)
        y_train[index,label[0]] = 1
    return y_train



def load_mnist_data():
    mnist = input_data.read_data_sets("MNIST_data/", one_hot=True)
    x_train, y_train, x_test, y_test = mnist.train.images, mnist.train.labels, mnist.test.images, mnist.test.labels

    x_train = x_train[:10000]
    y_train = y_train[:10000]

    y_train = add_noise(y_train,0.00)

    x_train = x_train.reshape(-1, 28, 28, 1)  # 28x28×1 input img
    x_test  = x_test.reshape(-1, 28, 28, 1)   # 28x28×1  input img

    x_dev = x_test[:1000]
    y_dev = y_test[:1000]

    x_test = x_test[1000:]
    y_test = y_test[1000:]

    return x_train,y_train,x_dev,y_dev,x_test,y_test

def main():
    """###
    主函数
    """
    # 参数设置
    batch_size = 32 # 每个batch数量 30
    batch_num = [40, 20]    # 重复实验40次,每次25个batch
    retrain_num = 6 # 每次选择完batch之后,多训练0次
    entropy_similarity_ratio = 1 # 

    # 读取数据
    x_train,y_train,x_dev,y_dev,x_test,y_test = load_mnist_data()

    ####################################################################################################
    ####################################################################################################

    optimizer_1 = tf.train.RMSPropOptimizer(0.001,0.9)

    # single mode下的train
    res_single = model_single()
    py_x, feature = res_single
    cost_single = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(py_x, Y)) 
    train_op_single  = optimizer_1.minimize(cost_single)
    predict_op_single = tf.argmax(py_x, 1)

    # pairwise下的train
    p_similarity = model_pairwise()
    similarity  = tf.matmul(Y,tf.transpose(Y))
    similarity  = (2 * similarity - 1) * np.triu(np.ones([batch_size,batch_size]),1) 
    # similarity  = similarity  / (batch_size * (batch_size - 1) / 2) / 100000
    cost_pairwise = tf.reduce_mean(tf.log(1 + tf.exp(- similarity * p_similarity) )) 
    beta = 0.5
    # train_op_pairwise = optimizer_1.minimize(cost_pairwise*beta + cost_single)
    train_op_pairwise = optimizer_1.minimize(cost_pairwise*beta )

    # query by committee下的train
    py_x_2 = model_for_qbc()
    cost_2 = tf.reduce_mean(tf.nn.softmax_cross_entropy_with_logits(py_x_2, Y)) 
    train_op_2 = tf.train.RMSPropOptimizer(0.001,0.9).minimize(cost_2)
    predict_op_2 = tf.argmax(py_x_2, 1)
    ####################################################################################################
    ####################################################################################################

    # lauch the graph in a session
    saver = tf.train.Saver()
    def lauch_session(mode,ii):
        """###
        一次训练,返回acc(np.array),为训练每个batch之后测试的准确率
        """
        acc = np.zeros(batch_num[1])

        with tf.Session() as sess:
            # 还未标注的数据
            unlabel_indices = range(batch_size,len(x_train))
            np.random.shuffle(unlabel_indices)
            # 已标注的数据
            label_indices = []

            test_acc = 0
            best_dev_acc = 0

            tf.initialize_all_variables().run()
            
            # 加载pretrain的variable
            for i in tqdm(range(batch_num[1])):

                unlabelset = unlabel_indices[:]
                np.random.shuffle(unlabelset)

                if i == 0 :
                    # load pretrain model
                    pretrain_dir = 'mnist-pretrain'
                    saver_new = tf.train.import_meta_graph(pretrain_dir + '/model.meta')
                    saver_new.restore(sess, tf.train.latest_checkpoint(pretrain_dir))
                    selectset = []
                elif mode == 'random' : # or best_dev_acc < 0.5:
                    selectset = unlabelset[:batch_size]
                else:
                    p_dict = { }
                    p_dict['mode'] = mode
                    py_unlabelset_1,u_feature = sess.run(res_single,feed_dict={
                        X: x_train[unlabelset],
                            p_keep_conv : 1 })
                    uncertainty_score = scipy.stats.entropy(np.transpose(py_unlabelset_1))
                    p_dict['uncertainty_score'] = uncertainty_score
                    p_dict['u_feature'] = u_feature
                    p_dict['entropy_similarity_ratio'] = entropy_similarity_ratio


                    if 'exploration-exploitation' in mode:
                        py_labelset, l_feature = sess.run(res_single,feed_dict={
                            X: x_train[label_indices],
                            p_keep_conv : 1 })
                        p_dict['l_feature'] = l_feature
                        # p_dict['exploration_num'] = 8
                        # p_dict['exploration_num'] = int(batch_size * - 0.3)
                        p_dict['exploration_num'] = int(max(batch_size - int(batch_size * 0.08),int(0.0*batch_size)))
                        ### 最好
                        ## batch_size = 32
                        ## p_dict['exploration_num'] = int(max(batch_size - int(batch_size * 0.08),int(0.0*batch_size)))
                        ## 有pairwise训练
                        p_dict['exploitation_num'] = batch_size -  p_dict['exploration_num'] 
                        p_dict['mode'] = 'exploration-exploitation'

                    if mode in ['kff']:
                        py_labelset, l_feature = sess.run(res_single,feed_dict={
                            X: x_train[label_indices],
                            p_keep_conv : 1 })
                        p_dict['l_feature'] = l_feature
                        # p_dict['exploration_num'] = 8
                        p_dict['exploration_num'] = batch_size
                        p_dict['exploitation_num'] = 0
                        p_dict['mode'] = 'exploration-exploitation'


                    selectset_indices = batch_select(p_dict['mode'], len(unlabelset), batch_size, p_dict)
                    selectset = np.array(unlabelset)[selectset_indices]

                
                new_indices = []
                for index in  selectset:
                    unlabel_indices.remove(index)
                    label_indices.append(index)
                    new_indices.append(index)

                selectset0 = selectset[:]
                np.random.shuffle(label_indices)

                if i : # 第0次 加载pretrain
                    for j in range(retrain_num):
                        new_ratio = 1
                        np.random.shuffle(label_indices)
                        for k in range(len(label_indices)/batch_size):

                            '''
                            if 'pairwise' in mode:
                                # train_op = train_op_pairwise
                                train_op = train_op_single
                            else:
                                train_op = train_op_single
                            '''

                            # 以前选出的数据训练一次
                            # if 'kff' not in mode:
                            if 1:
                                np.random.shuffle(selectset0)
                                sess.run(train_op_single, feed_dict = {
                                    X: x_train[selectset0],
                                    Y: y_train[selectset0],
                                    p_keep_conv : 0.8, })

                            if 1:

                                selectset = label_indices[k*batch_size : (k+1)*batch_size]
                                np.random.shuffle(selectset)
                                sess.run(train_op_single, feed_dict = {
                                    X: x_train[selectset],
                                    Y: y_train[selectset],
                                    p_keep_conv : 0.8, })

                            if 'pairwise' in mode:
                                sess.run(train_op_pairwise, feed_dict = {
                                    X: x_train[selectset],
                                    Y: y_train[selectset],
                                    p_keep_conv : 0.8, })


                            


                dev_acc = np.mean(np.argmax(y_dev, axis=1) == 
                    sess.run(predict_op_single, feed_dict = {
                        X: x_dev,
                        p_keep_conv : 1 }))


                if dev_acc > best_dev_acc:
                    test_acc = np.mean(np.argmax(y_test, axis=1) == 
                        sess.run(predict_op_single, feed_dict = {
                            X: x_test,
                            p_keep_conv : 1, }) )
                    if 'qbc' in mode :
                        test_acc_2 = np.mean(np.argmax(y_test, axis=1) == 
                            sess.run(predict_op_2, feed_dict = {
                                X: x_test,
                                p_keep_conv : 1, }) )
                        print test_acc_2
                    print ii,mode,'训练集数量:',len(label_indices),
                    print 'best_dev_acc: {:.2f}\t dev_acc: {:.2f}\t  test_acc: {:.2f}\t retrain_num: {:d}\t '.format(best_dev_acc,dev_acc,test_acc,retrain_num)
                    best_dev_acc = dev_acc
                acc[i] = test_acc
        return acc

    random_acc = np.zeros(batch_num)
    entropymax_acc  = np.zeros(batch_num)
    batchmodepairwise_acc = np.zeros(batch_num)
    batchmodeqbc_acc= np.zeros(batch_num)
    batchmode_acc = np.zeros(batch_num)
    entropymaxrandom_acc = np.zeros(batch_num)
    qbc_acc = np.zeros(batch_num)
    ee_acc = np.zeros(batch_num)
    eep_acc = np.zeros(batch_num)
    kff_acc = np.zeros(batch_num)
    dacc = {

            # 'random': random_acc,
            # 'entropy-max': entropymax_acc,
            # 'batch-mode': batchmode_acc,
            'exploration-exploitation-pairwise': eep_acc,
            'kff': kff_acc,

            }
    fname_dict = {
            'random': 'Random.npy',
            'entropy-max': 'EM.npy',
            'batch-mode': 'BMAL.npy',
            'exploration-exploitation-pairwise': 'Exploration-P.npy',
            'kff': 'KFF.npy',
            }
    for ii in range(batch_num[0]):
        os.system('clear')
        date = time.strftime("%Y%m%d")
        title = '{:s}-mnist-retrain-{:d}-{:s}.png'.format(date,retrain_num,'notmix')
        fname = '../../result/title.txt'
        with open(fname,'w') as f:
            f.write(title)
        for mi,mode in enumerate(sorted(dacc.keys(),reverse=False)):
            print 'mode   ',mi
            dacc[mode][ii] = lauch_session(mode,ii)
            fname = '../../result/'+ fname_dict[mode]
            np.save(fname,dacc[mode])


if __name__ == '__main__':
    # pretrain()
    main()
