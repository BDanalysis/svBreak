with open('/home/shaoqiangwang/BREAKPOINT/test/all_mutation_10_24.sam', 'r') as f:
    with open('/home/shaoqiangwang/BREAKPOINT/test/all_mutation_10_24_input.sam', 'w') as g:
        for line in f.readlines():
            if '100M' not in line:
                g.write(line)
