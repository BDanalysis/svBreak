import shutil
with open('/home/shaoqiangwang/BREAKPOINT/chr21.fa', 'r') as f:
    with open('/home/shaoqiangwang/BREAKPOINT/all_mutation_original.fa', 'w') as g:
        for line in f.readlines():
            if 'N' not in line:
                g.write(line)
#shutil.move('/home/shaoqiangwang/BREAKPOINT/all_mutation_original.fa', '/home/shaoqiangwang/BREAKPOINT/chr21.fa')
#去掉注释将直接覆盖掉原来的chr21.fa，并删除N