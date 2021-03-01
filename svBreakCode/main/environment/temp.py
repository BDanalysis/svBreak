# coding=utf-8

import os
import sys
import json
import scipy
import time

import numpy as np
import tensorflow as tf



# input of nnet
x = tf.placeholder("float", [10])

y = tf.nn.softmax(x)

with tf.Session() as sess:
    tf.initialize_all_variables().run()
    a = range(10)
    b = sess.run(y, feed_dict = {
                x: a
                })
    print b

