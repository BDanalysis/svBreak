# coding=utf-8

import os
import sys
import json
import scipy
import time

import numpy as np
import tensorflow as tf

from gensim import matutils


# uncertainty_score=None, u_feature=None, l_feature=None, entropy_similarity_ratio=1.
def batch_select(mode, u_num, batch_size, p_dict):
    """###
    batch的选择：
        entropy-max ：选择uncertainty_score最高的batch
        batch-mode  ：选择uncertainty_score最高而且彼此最不相似的batch,用贪心算法选择
    """
    indices_list = range(u_num)

    if mode in ['random']:
        np.random.shuffle(indices_list)
        return indices_list[:batch_size]
    elif mode in ['entropy-max','qbc']:
        uncertainty_score = p_dict['uncertainty_score']
        selectset_indices = sorted(indices_list, key=lambda index:uncertainty_score[index],reverse=True)[:batch_size]
        return selectset_indices
    elif mode in ['exploration-exploitation','exploration-exploitation-single']:
        entropy_similarity_ratio = p_dict['entropy_similarity_ratio']
        uncertainty_score = p_dict['uncertainty_score']
        u_feature = p_dict['u_feature']
        l_feature = p_dict['l_feature']
        exploration_num = p_dict['exploration_num']
        exploitation_num = p_dict['exploitation_num']
        # exploitation_num = batch_size - 0
        # exploration_num = batch_size - exploitation_num 
        # 归一化
        for i in range(u_num):
            u_feature[i] = matutils.unitvec(u_feature[i])
        for i in range(len(l_feature)):
            l_feature[i] = matutils.unitvec(l_feature[i])
        if len(l_feature):
            l_u_similarity = np.matmul(l_feature,np.transpose(u_feature))
        else:
            l_u_similarity = np.zeros([0,u_num])
        u_u_similarity = np.matmul(u_feature,np.transpose(u_feature))
        selectset_indices = batch_select('batch-mode-pairwise', u_num, exploitation_num, p_dict)
        for i in range(exploration_num):
            similarity_score = np.max(np.concatenate((u_u_similarity[selectset_indices],l_u_similarity),axis=0),axis=0) 
            # print similarity_score.shape
            sorted_indices = sorted(range(len(similarity_score)), key=lambda index:similarity_score[index])
            # np.random.shuffle(sorted_indices)
            for index in sorted_indices:
                if index not in selectset_indices:
                    selectset_indices.append(index)
                    break
        return selectset_indices
        # return range(batch_size)

    elif 'batch-mode' in mode:
        uncertainty_score = p_dict['uncertainty_score']
        u_feature = p_dict['u_feature']
        entropy_similarity_ratio = p_dict['entropy_similarity_ratio']
        # 归一化
        for i in range(u_num):
            u_feature[i] = matutils.unitvec(u_feature[i])
        similarity_matrix = np.matmul(u_feature,np.transpose(u_feature))
        selectset_indices = [np.argmax(uncertainty_score)]
        for i in range(1,batch_size):
            select_similarity = similarity_matrix[selectset_indices]
            # diversity_score = - np.mean(select_similarity,axis=0)
            diversity_score = - np.max(select_similarity,axis=0)
            add_score = np.array(uncertainty_score) + np.array(diversity_score) * entropy_similarity_ratio
            sorted_indices = sorted(range(len(add_score)), key=lambda index:add_score[index],reverse=True)
            for index in sorted_indices:
                if index not in set(selectset_indices):
                    selectset_indices.append(index)
                    break
        return selectset_indices
    raise NameError('mode error')
