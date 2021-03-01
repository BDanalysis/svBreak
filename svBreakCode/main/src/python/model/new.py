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

def load_mnist_data():
    mnist = input_data.read_data_sets("MNIST_data/", one_hot=True)
    x_train, y_train, x_test, y_test = mnist.train.images, mnist.train.labels, mnist.test.images, mnist.test.labels

    x_train = x_train[:10000]
    y_train = y_train[:10000]

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
    batch_size = 32# 每个batch数量 30
    batch_num = [40, 20]    # 重复实验40次,每次25个batch
    retrain_num = 5 # 每次选择完batch之后,多训练0次
    entropy_similarity_ratio = 1
    beta = 0.000001

    # 读取数据
    x_train,y_train,x_dev,y_dev,x_test,y_test = load_mnist_data()

    def get_label_dist(indices):
        dist = [0 for ind in range(11)]
        for index in indices:
            dist[np.argmax(y_train[index])] += 1

        '''
        if sum(dist):
            return int(scipy.stats.entropy(dist[:-1]) * 100) 
        else:
            return 0
        '''

        if sum(dist):
            dist_s = sorted(dist,reverse=True)
            dist[-1] = float(np.sum([dist_s[:5]]))/np.sum(dist)
        return dist[-1]

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
    '''
    # similarity  = tf.matmul(Y,tf.transpose(Y))
    # similarity  = (2 * similarity - 1) * np.triu(np.ones([batch_size,batch_size]),1) 
    # similarity  = similarity  / (batch_size * (batch_size - 1) / 2) * beta
    similarity  = 2 * tf.matmul(Y,tf.transpose(Y)) - 1
    # cost_pairwise = tf.reduce_mean(tf.log(1 + tf.exp(- similarity * p_similarity) )) * beta
    cost_pairwise = tf.reduce_mean(- similarity * p_similarity) * beta
    '''
    similarity = 2 * tf.matmul(Y,tf.transpose(Y)) -1 
    # cost_pairwise = tf.reduce_mean(- similarity * p_similarity) * beta
    cost_pairwise = tf.reduce_mean(tf.log(1 + tf.exp(- similarity * p_similarity) )) * beta
    train_op_pairwise = optimizer_1.minimize(cost_pairwise)

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
        dist_list = []

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
                    py_unlabelset_1,u_feature = sess.run(res_single,feed_dict={
                        X: x_train[unlabelset],
                            p_keep_conv : 1 })
                    uncertainty_score = scipy.stats.entropy(np.transpose(py_unlabelset_1))
                    p_dict['uncertainty_score'] = uncertainty_score
                    p_dict['u_feature'] = u_feature
                    p_dict['entropy_similarity_ratio'] = entropy_similarity_ratio


                    if mode in ['exploration-exploitation']:
                        py_labelset, l_feature = sess.run(res_single,feed_dict={
                            X: x_train[label_indices],
                            p_keep_conv : 1 })
                        p_dict['l_feature'] = l_feature
                        # p_dict['exploration_num'] = 8
                        p_dict['exploration_num'] = 16
                        p_dict['exploration_num'] = int(max(20-3.*i,0))
                        p_dict['exploitation_num'] = batch_size -  p_dict['exploration_num'] 

                    selectset_indices = batch_select(mode, len(unlabelset), batch_size, p_dict)
                    selectset = np.array(unlabelset)[selectset_indices]

                for label in  selectset:
                    unlabel_indices.remove(label)
                    label_indices.append(label)
                dist_list.append(get_label_dist(label_indices))
                np.random.shuffle(label_indices)
                selectset0 = selectset[:]

                if i : # 第0次 加载pretrain
                    for j in range(retrain_num+1):
                        for k in range(len(label_indices)/batch_size):
                            # 刚刚选出的数据训练一次
                            np.random.shuffle(selectset0)
                            sess.run(train_op_single, feed_dict = {
                                X: x_train[selectset0],
                                Y: y_train[selectset0],
                                p_keep_conv : 0.8, })
                            # 以前选出的数据训练一次
                            selectset = label_indices[k*batch_size : (k+1)*batch_size]
                            np.random.shuffle(selectset)
                            sess.run(train_op_single, feed_dict = {
                                X: x_train[selectset],
                                Y: y_train[selectset],
                                p_keep_conv : 0.8, })
                            #####
                            if 'qbc' in mode:
                                # 训练另外一个模型
                                sess.run(train_op_single, feed_dict = {
                                    X: x_train[selectset0],
                                    Y: y_train[selectset0],
                                    p_keep_conv : 0.8, })
                                sess.run(train_op_2, feed_dict = {
                                    X: x_train[selectset],
                                    Y: y_train[selectset],
                                    p_keep_conv : 0.8, })
                            #####

                        for k in range(len(label_indices)/batch_size):
                            # 使得同一类的图片之间更相似
                            if mode in ['batch-mode-pairwise','exploration-exploitation']:
                                k = k % ( len(label_indices) / batch_size)
                                selectset = label_indices[k*batch_size : (k+1)*batch_size]
                                # selectset1 = selectset[:]
                                # np.random.shuffle(selectset)
                                # selectset2 = selectset[:]
                                sess.run(train_op_pairwise, feed_dict = {
                                    X: x_train[selectset],
                                    Y: y_train[selectset],
                                    p_keep_conv : 0.8
                                    })

                dev_acc = np.mean(np.argmax(y_dev, axis=1) == 
                    sess.run(predict_op_single, feed_dict = {
                        X: x_dev,
                        p_keep_conv : 1 }))


                if dev_acc > best_dev_acc:
                    test_acc = np.mean(np.argmax(y_test, axis=1) == 
                        sess.run(predict_op_single, feed_dict = {
                            X: x_test,
                            p_keep_conv : 1, }) )
                    print ii,mode,'训练集数量:',len(label_indices),
                    print 'best_dev_acc: {:.2f}\t dev_acc: {:.2f}\t  test_acc: {:.2f}\t retrain_num: {:d}\t '.format(best_dev_acc,dev_acc,test_acc,retrain_num)
                    best_dev_acc = dev_acc
                acc[i] = test_acc
        return acc, np.array(dist_list)

    random_acc = np.zeros(batch_num)
    entropymax_acc  = np.zeros(batch_num)
    batchmodepairwise_acc = np.zeros(batch_num)
    batchmodeqbc_acc= np.zeros(batch_num)
    batchmode_acc = np.zeros(batch_num)
    entropymaxrandom_acc = np.zeros(batch_num)
    qbc_acc = np.zeros(batch_num)
    ee_acc = np.zeros(batch_num)
    dacc = {

            'random': random_acc,
            'entropy-max': entropymax_acc,
            'batch-mode-pairwise': batchmodepairwise_acc,
            # 'qbc': qbc_acc,
            'batch-mode': batchmode_acc,
            # 'exploration-exploitation': ee_acc,

            }
    dist_dict = { k:np.zeros(batch_num[1]) for k in dacc.keys() }
    for ii in range(batch_num[0]):
        os.system('clear')
        date = time.strftime("%Y%m%d")
        title = '{:s}-mnist-retrain-{:d}-{:s}.png'.format(date,retrain_num,'notmix')
        fname = '../../result/title.txt'
        with open(fname,'w') as f:
            f.write(title)
        # os.system('scp {:s} ycclab:/home/cc/ycc/virtual-env/tensorflow/mnist-classification/result/'.format(fname))
        with open('../../result/title.txt','w') as f:
            f.write(title)
        for mi,mode in enumerate(sorted(dacc.keys(),reverse=False)):
            print 'mode   ',mi
            dacc[mode][ii], dist = lauch_session(mode,ii)
            dist_dict[mode] += dist
            fname = '../../result/{:s}-mnist-{:s}.npy'.format(date,mode)
            np.save(fname,dacc[mode])
            with open('../../result/{:s}-dist.json'.format(mode), 'w') as f:
                f.write(json.dumps([int(100*d/(ii+1)) for d in dist_dict[mode]]))
            # os.system('scp {:s} ycclab:/home/cc/ycc/virtual-env/tensorflow/mnist-classification/result/'.format(fname))


if __name__ == '__main__':
    # pretrain()
    main()
