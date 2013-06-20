import os
import datetime
from math import *
import numpy
import csv

num_of_runs = 0

def process(results, host_vms):
    tree= build_tree(results)
    verify_bmark_outputs(tree)

    print_raw(tree)
    compacted_results = compact_results(tree)

    order= [ ident for (ident, cmd_line, ___) in host_vms ]
    print_compacted_results(compacted_results, order)
    write_compacted_result_to_csv(compacted_results)

    ## 20110907/1128/sbr: TODO: should actually store the outputs using get_output_filename as a text representation...
    remove_temp_files(results)

def build_tree(tuples):
    """This function builds an intermediate tree out of two dimensions of dictionaries to enable
    sub-sequent processing of data grouped by benchmarks and host virtual machines.
    It is an *internal* function and used to generate the tree argument used in  "flatten_tree",
    "rows_by_bmark", "verify_bmark_outputs", and "print_results_by_time".
    """
    tree= {}
    for (native, host, s, run_time, output, path, r) in tuples:
        if s not in tree:
            tree[s]= {}
        if host not in tree[s]:
            tree[s][host]= []
        tree[s][host].append( (run_time, output, native) )
    return tree

def flatten_tree(tree, selector):
    """This function flattens the tree from two dimensions into one, where the values for each key
    is a list that contains all of the elements found.
    """
    table= {}
    for (bmark, host, elem) in rows_by_bmark(tree, selector):
        if bmark not in table:
            table[bmark]= []
        table[bmark].append( (host, elem) )
    return table

def verify_bmark_outputs(tree):
    outputs= flatten_tree( tree, lambda (t,o,n): o)

    for bmark, data in outputs.iteritems():
        for ((lH, lO), (rH, rO)) in zip(data, data[num_of_runs:]):
            if lO != rO:
                print "verify_bmark_outputs: %s: left: %s != right %s" % (bmark, lH, rH)
                print "BEGIN left\n%s\nEND left" % lO
                print "BEGIN right\n%s\nEND right" % rO

def rows_by_bmark(tree, selector=lambda x: x):
    """This generator uses a selector function to select data elements from the tuple and yield
    this selection together with the host and benchmark names. This simplifies sub-sequent
    processing in the "flatten_tree" function.
    """
    for bmark, bmark_data in tree.iteritems():
        outputs= []
        for host, host_data in bmark_data.iteritems():
            for elem in host_data:
                yield (bmark, host, selector(elem))
                
def get_output_filename():
    base_dir= get_cwd()
    base_ext= "-mbs-bmark-results.txt"
    now= datetime.datetime.now();
    identifier= base_dir
    identifier+= "%4d%02d%02d" % (now.year, now.month, now.day)
    identifier+= "-"
    identifier+= "%02d%02d" % (now.hour, now.minute)
    identifier+= base_ext
    return identifier
                
def get_current_time_string():
    now = datetime.datetime.now()
    time_string = "%4d%02d%02d" % (now.year, now.month, now.day)
    time_string += "-"
    time_string += "%02d%02d" % (now.hour, now.minute)
    return time_string

#Chen's Function to Print Out Raw Data
def print_raw(tree):
    results = flatten_tree(tree, lambda(t, o, n): (t, n))

    raw_results = []
    for bmark, data in results.iteritems():
        bmark = os.path.split(bmark)[1]
        result = []
        result.append(bmark)

        times = []
        for (host ,(time, native)) in data:
            times.append(time)

        result.append(times)

        raw_results.append(result)


    table_header = []

    hosts = []
    for (host ,(time, native)) in data:
        (host_name, ___, ___ ) = host
        host_native_name = host_name + '/' + native[0]
        if host_native_name not in hosts:
            hosts.append(host_native_name)

    for each_host in hosts:
        table_header.append('benchmark ' + each_host)


    #print "-------------------------------------------------------------------------------------------------------------------------------"
    time_banner = "[" + str(num_of_runs) + "X] " + str(datetime.datetime.now())
    #print "%127s" % time_banner
    #print "-------------------------------------------------------------------------------------------------------------------------------"

    index = 0
    host_output = []
    header = []
    header.append("type")
    header.append("benchmark")
    header.append("time")
    for each_host in hosts:
        title = 'benchmark [' + each_host + ']'
        #print "%29s" % (title)
        #print "-------------------------------------------------------------------------------------------------------------------------------"
        for result in raw_results:
            (bmark, times) = result

            format = ''
            format += '%29s\t'
            for i in range(1, num_of_runs + 1):
                format += '%15.2f'

            output = []
            output.append(bmark)

            for i in range(index, index + num_of_runs):
                raw_data = []
                raw_data.append(each_host)
                raw_data.append(bmark)
                raw_data.append(times[i])
                host_output.append(raw_data)
                output.append(times[i])

            #print format % tuple(output)
        index = index + num_of_runs



    #print "-------------------------------------------------------------------------------------------------------------------------------"
    result_file_base_name = get_current_time_string() + '-' + 'raw_result.csv'
    with open(result_file_base_name, "w") as output:
        writer = csv.writer(output)
        for result in host_output:
            writer.writerow(result)

    last_raw_file_name = 'raw_result.csv'
    with open(last_raw_file_name, "w") as output:
        writer = csv.writer(output)
        for result in host_output:
            writer.writerow(result)
#end print_raw


def compact_results(tree):
    results = flatten_tree(tree, lambda(t, o, n): (t, n))
    #print results
    compacted_results = []
    for bmark, data in results.iteritems():
        bmark = os.path.split(bmark)[1]
        result = []
        result.append(bmark)
        times = []
        index = 1
        for (host ,(time, native)) in data:
            #print time
            if index < num_of_runs:
                times.append(time)
                index += 1
            else:
                times.append(time)
                result.append(times)
                times = []
                index = 1

        # calculating standard deviation and variance
        # now result looks like this
        # ['scriptName', [list of time for host0], [list of time for host0]]
        list_iter = iter(result)
        first_item_is_script_name = list_iter.next()
        averaged_result = [first_item_is_script_name]

        for time_list in list_iter:
            mean = numpy.mean(time_list)
            std = numpy.std(time_list) / mean
            variance = numpy.var(time_list) / mean**2
            averaged_result.append(mean)
            averaged_result.append(std)
            averaged_result.append(variance)

#        compacted_results.append(tuple(result))
        compacted_results.append(averaged_result)

    compacted_results.sort()
    table_header = []
    table_header.append('benchmark')

    hosts = []
    for (host ,(time, native)) in data:
        (host_name, ___, ___ ) = host
        host_native_name = host_name + '/' + native[0]
        if host_native_name not in hosts:
            hosts.append(host_native_name)

    for each_host in hosts:
        table_header.append(each_host)
        table_header.append('std')
        table_header.append('var')

    compacted_results.insert(0, tuple(table_header))
    return compacted_results

def print_compacted_results(results, ordering):
    print "-------------------------------------------------------------------------------------------------------------------------------"
    time_banner = "[" + str(num_of_runs) + "X] " + str(datetime.datetime.now())
    print "%127s" % time_banner
    print "-------------------------------------------------------------------------------------------------------------------------------"
    (label, vm1, std1, var1, vm2, std2, var2) = results.pop(0)
    swap= False

    [order1, order2] = ordering
    if [vm1[0], vm2[0]] != [order1[0], order2[0]]:
        vm1, std1, var1, vm2, std2, var2= vm2, std2, var2, vm1, std1, var1
        swap= True

    print "%29s\t%15s%10s%10s\t%15s%10s%10s\t%15s" % (label, '[' + vm1 + ']', std1, var1, '[' + vm2 + ']', std2, var2, 'speedup:2/1')
    print "-------------------------------------------------------------------------------------------------------------------------------"
    time1s, time2s = [], []
    speedups = []
    for result in results:
        (bmark, time1, std1, var1, time2, std2, var2) = result
        if swap:
            time1, time2= time2, time1
        speedup = time1/time2
        print "%29s\t%15.2f%10.2f%10.2f\t%15.2f%10.2f%10.2f\t%15f" % (bmark, time1, std1, var1, time2, std2, var2, speedup)
        time1s.append(time1)
        time2s.append(time2)
        speedups.append(speedup)

    print "-------------------------------------------------------------------------------------------------------------------------------"
    print "%29s\t%15.2f\t%39.2f\t%39f" % ('arithmetic mean',
                            float(sum(time1s)/len(time1s)),
                            float(sum(time2s)/len(time2s)),
                            float(sum(speedups)/len(speedups)))

    gmean_time1 = exp(sum([log(time1) for time1 in time1s])/len(time1s))
    gmean_time2 = exp(sum([log(time2) for time2 in time2s])/len(time2s))
    gmean_speedup = exp(sum([log(speedup) for speedup in speedups])/len(speedups))
    print "%29s\t%15.2f\t%39.2f\t%39f" % ('geometric mean', gmean_time1, gmean_time2, gmean_speedup)
    print "-------------------------------------------------------------------------------------------------------------------------------"
    # put header back to results
    results.insert(0, (label, vm1, vm2))
    
def write_compacted_result_to_csv(results):
    result_file_base_name = 'result.csv'
    if os.path.isfile(result_file_base_name):
        os.rename(result_file_base_name, get_current_time_string() + '-' + result_file_base_name)

    with open("result.csv", "w") as output:
        writer = csv.writer(output)
        for result in results:
            writer.writerow(result)

def remove_temp_files(tuples):
    for (_, _, _, _, _, path, _) in tuples:
        if os.path.exists(path):
            os.remove(path)
            
def write_frame(frame):
    """
    archive the old frame.r file
    """
    frame_file_name = "frame.r"
    oldframepath = os.path.join(os.getcwd(), frame_file_name)
    ctime = os.path.getctime(oldframepath)
    arcframename = ("frame-%s.r" % (time.ctime(ctime))).replace(' ', '_')
    arcframepath = os.path.join(os.getcwd(), arcframename)
    shutil.copyfile(oldframepath, arcframepath)


    with open("frame.r", 'w') as output:
        for k, v in frame.items():
            output.write("%s <- c(%s)\n" % (k, (','.join([escape_r(x) for x in v]))))
        output.write("data <- data.frame(%s)\n" % ','.join(frame.keys()))