# coding=utf-8

import os
import sys
import json
import scipy
import time

import numpy as np
import tensorflow as tf

from gensim import matutils
from scipy  import stats
from tqdm   import tqdm
from collections import OrderedDict
from tensorflow.examples.tutorials.mnist import input_data


# input of nnet
p_keep_conv = tf.placeholder('float')
X = tf.placeholder("float", [None, 28, 28, 1])
Y = tf.placeholder("float", [None, 10])
# X1 = tf.placeholder("float", [None, 28, 28, 1])
# Y1 = tf.placeholder("float", [None, 10])
# X2 = tf.placeholder("float", [None, 28, 28, 1])
# Y2 = tf.placeholder("float", [None, 10])

# X = tf.placeholder("float", [32, 28, 28, 1])
# Y = tf.placeholder("float", [32, 10])

# 定义参数变量
def init_weights(shape,name):
     return tf.Variable(tf.random_normal(shape,stddev=0.01),name=name)

w1 = init_weights([3,3,1,32],'conv0_1')
w2 = init_weights([3,3,32,64],'conv1_1')
w3 = init_weights([3,3,64,128],'conv2_1')
w4 = init_weights([128*4*4,625],'w4')
w_o = init_weights([625,10],'mul_w')
b = init_weights([10],'b')

w1_2 = init_weights([3,3,1,32],'conv0_1')
w2_2  = init_weights([3,3,32,64],'conv1_1')
w3_2  = init_weights([3,3,64,128],'conv2_1')
w4_2  = init_weights([128*4*4,625],'w4')
w_o_2  = init_weights([625,10],'mul_w')
b_2  = init_weights([10],'b')


def get_mnist_feature():

    net = X

    net = tf.nn.relu(tf.nn.conv2d(net, w1, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    net = tf.nn.relu(tf.nn.conv2d(net, w2, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    net = tf.nn.relu(tf.nn.conv2d(net, w3, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    flattened_shape = 128 * 4 * 4
    # flattened_shape = np.prod([s.value for s in net.get_shape()[1:]])
    net = tf.reshape(net, [-1, flattened_shape], name="flatten")

    net = tf.nn.relu(tf.matmul(net, w4))
    net = tf.nn.dropout(net, p_keep_conv)

    return net

def model_single():
    feature = get_mnist_feature()
    n_in = feature.get_shape()[-1].value
    py_x = tf.nn.softmax(tf.matmul(feature, w_o)+b)
    return py_x,feature

def model_pairwise():
    """###
    pairwise 特征提取，计算之间的距离
    """

    feature1, py_x1 = model_single()
    feature2, py_x2 = feature1, py_x1  

    p_similarity = tf.matmul(feature1,tf.transpose(feature2))

    return p_similarity




def model_for_qbc():
    """###
    分类 query by committee
    """
    net = X

    net = tf.nn.relu(tf.nn.conv2d(net, w1_2, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    net = tf.nn.relu(tf.nn.conv2d(net, w2_2, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    net = tf.nn.relu(tf.nn.conv2d(net, w3_2, strides=[1, 1, 1, 1], padding='SAME'))
    net = tf.nn.max_pool(net, ksize=[1, 2, 2, 1], strides=[1, 2, 2, 1], padding='SAME')
    net = tf.nn.dropout(net, p_keep_conv)

    flattened_shape = 128 * 4 * 4
    # flattened_shape = np.prod([s.value for s in net.get_shape()[1:]])
    net = tf.reshape(net, [-1, flattened_shape], name="flatten")

    net = tf.nn.relu(tf.matmul(net, w4_2))
    net = tf.nn.dropout(net, p_keep_conv)

    py_x = tf.nn.softmax(tf.matmul(net, w_o_2)+b_2)

    return py_x

