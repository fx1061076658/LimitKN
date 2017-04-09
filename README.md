>写在前面: 
> 1. 赛题答疑联系人（可通过钉钉联系）：万少
> 2. 在开始coding前请仔细阅读以下内容
> 3. 内部赛钉钉群：11751103

# 重要通知：正式代码提交入口已经开启，欢迎大家提交代码~ 原有评测机器已经全部换成物理机，避免随机环境的影响。


# 1. 题目背景
题目主要解决的是NewSQL领域中使用最频繁的一个场景:分页排序，其对应的SQL执行为order by id limit k,n
主要的技术挑战为"分布式"的策略，赛题中使用多个文件模拟多个数据分片。

# 2. 题目描述

## 2.1 题目内容
给定一批数据，求解按顺序从小到大，顺序排名从第k下标序号之后连续的n个数据 

例如：
top(k,3)代表获取排名序号为k+1,k+2,k+3的内容,例:top(10,3)代表序号为11、12、13的内容,top(0,3)代表序号为1、2、3的内容
需要考虑k值几种情况，k值比较小或者特别大的情况，比如k=1,000,000,000
对应k,n取值范围： 0 <= k < 2^63 和 0 < n < 100


## 2.2 语言限定
限定使用JAVA语言




# 3.  程序目标

你的coding目标是实现一个继承自KNLimit接口的KNLimitImpl类，对其中的processTopKN(long k,int n)方法做实现，解决top(k,n)问题。
在结果正确的基础上，处理时间越短越好。




# 4. 参赛方法说明

用户从阿里云git仓库下载内部赛的接口和示例工程，并且实现指定接口。

阿里云内部赛git仓库地址： https://code.aliyun.com/middlewarerace2017/LimitKNDemo/tree/master

1. 选手定义主类的类名为"TopKN"的类，实现KNLimit接口并且实现自己的processTopKN(long k,int n)方法
2. TopKN类中的main方法调用选手实现的processTopKN方法。方法中的k,n的值通过main方法中的args参数按顺序获取。
2. 选手实现的processTopKN方法从指定的文件目录读取数据文件（需要符合命名规则），进行处理
3. 将自己处理的结果文件，按照命名规则输出到指定目录


PS：
1. 请确保自己的实现主类的类名为"TopKN"，我们的校验排名程序在验证选手代码的时候会去调用TopKN的main方法。为了保证您参赛成绩的有效性，
请确保自己实现类的类名命名准确。
2. 请确保k,n参数的值从main方法的args中按顺序获取

# 5. 数据文件说明

1. 文件个数：10
2. 每个文件大小：1G
3. 文件内容：由纯数字组成，每一行的数字代表一条数据记录
4. 每一行数字的大小取值范围 0 <= k < 2^63 （数字在Long值范围内均匀分布）
5. 数据文件的命名严格按照规则命名。命名规则："KNLIMIT_X.data" ，其中X的范围是[0,9]
6. 数据文件存放目录为/home/admin/topkn-datafiles/


PS: 服务端的数据文件均放在指定目录下，按照指定的命名规则命名。请确保提交的代码从指定路径下以正确的文件名读取文件。否则，将导致算法的校验逻辑失败。


# 6. 结果文件说明
1. 结果文件命名规则：RESULT.rs  ，比赛进行五轮测试，每轮测试都会生成新的RESULT.rs
2. 结果文件输出目录：/home/admin/topkn-resultfiles/teamCode


PS: 
1. 结果文件的命名和输出必须严格符合赛题要求，否则会影响程序的校验和排名。以上teamCode目录是选手参赛时的teamCode，每个人不同，请留意。
2. **结果文件中的每一行记录后面都必须跟一个换行符(\n)，因为最后比对结果用的是MD5，你少个换行符(\n)可能比对也会失败，切记！**

# 7. 测试环境描述
测试环境为相同的24核物理机，内存为98GB，磁盘使用不做限制(一般不建议选手产生超过10G的中间结果文件)。选手可以使用的JVM堆大小为2.5G。

PS:
1. 选手的代码执行时，JAVA_OPTS=" -XX:InitialHeapSize=2621440000 -XX:MaxHeapSize=2621440000 -XX:MaxNewSize=873816064 -XX:MaxTenuringThreshold=6 -XX:NewSize=873816064 -XX:OldPLABSize=16 -XX:OldSize=1747623936 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC  "
2. 不准使用堆外内存。




# 8. 程序校验逻辑
1. 参赛选手给出git地址
2. 结果校验和排名程序每天会从提交的git地址上拉取代码进行结果验证和排名
3. 校验程序会给出随机的k值和n值，作为processTopKN(long k,int n)的输入。其中k的取值范围是[0,TOTAL_LINE_NUM-100]， n的取值范围是(0,100]。其中TOTAL_LINE_NUM表示所有数据文件的总行数。参赛程序所需的数据文件
在结果校验程序所在服务器会随机生成。
4. 校验程序调用选手提供的TopKN中的主方法，生成一轮测试的结果文件RESULT.rs
5. 用户处理得到的结果文件会和校验服务端的"标准答案"文件一一做MD5校验，如果全部一致，则认为算法正确，计算耗时并且统计排名。
6. 重复执行第3至第5步，完成总共5轮测试（5轮测试使用的10个数据文件是相同的）。如果5轮测试的结果文件全部正确，则累计5轮处理的耗时，统计排名




PS:
1. 结果校验时会按顺序校验5份结果文件。单次时间计算，会从传入top的k和n参数作为时间开始,在得到所有n条结果数据后结束时间计算，两个时间差值为单次耗时。
整个top(k,n)的接口调用会在结果校验通过后,串行执行5次(每次k,n参数会不同,k的值会相对比较分散),把5次的单次耗时累加即为总耗时。
2. 调用选手的程序是通过脚本"java -cp $yourJarPath com.alibaba.middleware.race.TopKN $k $n" 这样的形式来调用的。该脚本会调用5次，当然每次的k,n都是随机的。


# 9. 排名规则

在结果校验100%正确的前提下，按照总耗时进行排名，耗时越少排名越靠前，时间精确到毫秒



# 10. 可以使用的类的约定

* 公平起见，仅允许依赖JavaSE 7 包含的标准库。此外不允许使用nio包下的类，以及其他使用堆外内存的类。



# 11. 示例工程说明

在[内部赛接口示例工程](https://code.aliyun.com/middlewarerace2017/LimitKNDemo/tree/master)中有个Demo类。该
类结合sortAllFiles.sh这个shell脚本来完成了topKN问题的解决。实现上未考虑性能优化，仅仅做演示使用。

示例工程实现思路：
1. 通过sortAllFiles.sh将10个文件内有序的小文件处理成为10个全局有序的小文件
2. 通过demo目录下中Demo类里面的方法，按序读取10个全局有序的小文件，得到top(k,n)的结果


# 12. 关于选手中间结果的补充

1. 比赛过程中，选手可以自由利用磁盘的空间。中间结果在进行选手的5轮测试的时候，均可以使用，不会被清空。选手的5轮测试结束后，选手的中间结果会被清空，下次评测使用中间结果需要重新生成。程序校验结束后，所有中间结果全部清空。中间结果请输出到指定目录：/home/admin/middle/teamCode下。

2. 请不要投机取巧将中间结果写入到非指定目录，然后在下一次再校验的时候去读取，从而来改进自己的比赛成绩。校验程序不会特地去清理写在其他目录选手产生的临时、中间结果文件，但是工作人员会不定时去服务器上抽查。

3. 请大家诚信为本，靠自己真正的实力赢得比赛。比赛后续还有答辩，所以投机取巧是不可行哦。


# 13. 代码提交相关的补充

代码提交评测的步骤：
1. 选手将自己的代码提交到[阿里云code](https://code.aliyun.com)上，并且在自己的私人项目添加项目成员"middlewarerace2017"，并将其角色设置为：develeoper
2. 选手在天池系统上通过设置自己的代码git仓库地址（此时会生成teamCode，该teamCode会在很多地方用到，例如写指定的中间文件目录、结果文件目录等）
3. 选手点击提交按钮，评测机器会自己拉取选手的代码，build选手的代码，然后运行相应的jar。
4. 如果选手代码的结果是正确的，则消耗掉一次评测机会，每天有2次评测机会。评测系统下载选手代码失败、编译构建失败，不消耗评测次数；如果代码运行超时或者得到的答案错误，将消耗一次评测机会，请大家提交代码前做好本地的验证工作！

PS：
1.  评测系统使用maven assemble插件构建选手程序。建议选手本地先运行"mvn clean assembly:assembly -Dmaven.test.skip=true" 命令，确保能正确构建再提交
2.  不要忘记添加指定成员为develeoper
3.  teamCode在正式开放代码提交入口之前，是无法获得的。等天池开放代码提交入口后，选手设定自己的git地址后，会获得唯一的teamCode

# 14. 代码评测相关的补充
1. 超时时间： 一次提交，会接收5轮输入。第一轮处理的超时时间为10分钟，后面四轮的超时时间为1分钟。意味着每位选手最多14分钟的处理时间。
2. 日志处理：
    - 选手执行的代码请自行引入日志框架
    - 请将日志写入指定的日志目录：/home/admin/logs/${teamCode}/，这里的teamCode请替换成自己的唯一teamCode，此外请不要透露自己的teamCode给别人哦。
    - 日志的命名按照如下命名：teser-${teamCode}-WARN.log和teser-${teamCode}-ERROR.log。
3. 如何获取自己运行的日志：
    - 选手每次提交的程序运行的gc日志以及符合上面两种命名的日志，评测程序才会将其反馈给选手。
    - 选手可以通过地址：http://middle2017.oss-cn-shanghai.aliyuncs.com/${teamCode}/${logName} 这样的形式获取自己的日志
    - ${teamCode}是选手的唯一识别码之一，${logName}的名称可以为gc.log.${round}、teser-${teamCode}-INFO.log、teser-${teamCode}-WARN.log和teser-${teamCode}-ERROR.log三者之一.${round}的值为0,1,2,3,4，不同轮数对应不同的gc日志。
    - 选手每次提交的处理日志会覆盖前一次的，请知悉
4. 如何获取性能相关的日志：
    - 总共5轮，每轮运行选手代码结束后，都会获取结束时间点之前十分钟的tsar信息，包含CPU、内存、IO和load。
    - 选手可以通过地址：http://middle2017.oss-cn-shanghai.aliyuncs.com/${teamCode}/${tsar_info}
    - ${tsar_info}可选值为：perform_cpu.info，perform_mem.info，perform_io.info，perform_load.info



> 此外，在正式开放代码提交入口之前，已经完成代码编写的同学，可以钉钉联系我参与评测系统的内测，完善评测系统。测试的结果不计入正式比赛排名。


# 15. FAQ补充

#### 1. 请大家在向天池系统提交代码前，仔细检查自己的代码，避免由于程序问题影响天池的校验程序。一些恶意行为的话，可能会被追究责任哟。例如在非指定目录写入大量文件把天池评测系统磁盘撑爆、编写恶意代码破坏天池系统正常评测等等。

#### 2. 是否可以使用堆外内存？答：不可以，仅在堆内内存处理数据。例如MappedByteBuffer、DirectByteBuffer这些涉及堆外内存的类不可以使用。

#### 3. 是否可以使用JAVA以外的语言？ 答：不可以，JNI调用、shell脚本调用都不行

#### 4. 最大文件打开数是多少？

```
$ulimit -a
core file size          (blocks, -c) 0
data seg size           (kbytes, -d) unlimited
scheduling priority             (-e) 0
file size               (blocks, -f) unlimited
pending signals                 (-i) 386774
max locked memory       (kbytes, -l) unlimited
max memory size         (kbytes, -m) unlimited
open files                      (-n) 655350
pipe size            (512 bytes, -p) 8
POSIX message queues     (bytes, -q) 819200
real-time priority              (-r) 0
stack size              (kbytes, -s) 10240
cpu time               (seconds, -t) unlimited
max user processes              (-u) 386774
virtual memory          (kbytes, -v) unlimited
file locks                      (-x) unlimited
```

#### 5. CPU信息怎样的？答：24核，其中1个核的信息如下：

```
processor	: 23
vendor_id	: GenuineIntel
cpu family	: 6
model		: 45
model name	: Intel(R) Xeon(R) CPU E5-2430 0 @ 2.20GHz
stepping	: 7
cpu MHz		: 2199.853
cache size	: 15360 KB
physical id	: 1
siblings	: 12
core id		: 5
cpu cores	: 6
apicid		: 43
initial apicid	: 43
fpu		: yes
fpu_exception	: yes
cpuid level	: 13
wp		: yes
flags		: fpu vme de pse tsc msr pae mce cx8 apic mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx pdpe1gb rdtscp lm constant_tsc arch_perfmon pebs bts rep_good xtopology nonstop_tsc aperfmperf pni pclmulqdq dtes64 ds_cpl vmx smx est tm2 ssse3 cx16 xtpr pdcm dca sse4_1 sse4_2 x2apic popcnt aes xsave avx lahf_lm arat epb xsaveopt pln pts dts tpr_shadow vnmi flexpriority ept vpid
bogomips	: 4399.41
clflush size	: 64
cache_alignment	: 64
address sizes	: 46 bits physical, 48 bits virtual
power management:
```

#### 6. 磁盘信息怎样的？

```
$sudo fdisk -l /dev/sda5

Disk /dev/sda5: 424.0 GB, 423999045632 bytes
255 heads, 63 sectors/track, 51548 cylinders
Units = cylinders of 16065 * 512 = 8225280 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disk identifier: 0x00000000




```

#### 7. k值超过总行数的情况要考虑吗？答: 不需要考虑，k+n的值肯定是小于等于总行数的

#### 8. 生成的数据是否会重复，是否需要去重？答：不需要去重，生成数据的方法可以参考样例工程中的数据生成方法

#### 9. JDK版本详情：

```
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)
```

#### 10. 可以使用多线程吗？答：可以

#### 11. 可以用java的Runtime.execute和ProcessBuilder.start等方法来启动LINUX的守护进程吗？答：不可以


#### 12. 可以使用nio吗？答：不可以。之前已经提到过不准使用堆外内存，由于nio的实现大量依赖堆外内存，我们这里也禁止使用。禁止使用意味着java.nio包下面的所有类不可以用，以及依赖java.nio的类的所有实现都不可以用。

#### 13. 生成的数据会重复吗？答：有可能会重复，但是不需要做去重。

#### 14. 是否开启了超线程？答：已经开启

#### 15. 磁盘读写速度怎样？

```
# 写性能
$sudo  time dd if=/dev/zero of=/data/test  bs=8k count=1000000
1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 18.6796 s, 439 MB/s
0.19user 15.03system 0:18.80elapsed 80%CPU (0avgtext+0avgdata 3760maxresident)k
1152inputs+16000000outputs (1major+360minor)pagefaults 0swaps
# 读性能
$sudo time dd if=/dev/sda5  of=/dev/null bs=8k count=1000000
1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 25.6534 s, 319 MB/s
0.13user 7.93system 0:25.65elapsed 31%CPU (0avgtext+0avgdata 3792maxresident)k
16000320inputs+0outputs (1major+362minor)pagefaults 0swaps

```

#### 16. 评测会清空系统缓存吗？答：每次运行选手的程序前都会用命令"sudo sysctl -w vm.drop_caches=3"清空系统缓存

#### 17. 评测的时候出现assembly错误怎么办？

```
1. 参考样例工程，确保使用如下的构件名：
    <groupId>com.alibaba.middleware.race</groupId>
    <artifactId>limitkndemo</artifactId>
    <version>1.0</version>
    <name>limitkndemo</name>
2. 确保本地使用mvn clean assembly:assembly -Dmaven.test.skip=true命令测试正确

3. 确保你的阿里云code项目已经添加项目成员"middlewarerace2017"，并将其角色设置为：develeoper

4. 在阿里云代码提交的时候，确保git地址的正确性

推荐做法： 直接下载样例工程，把自己的TopKN主类搞进去
```

#### 18. 评测的时候出现找不到结果文件怎么办？答：参考第六条，确保自己的结果文件命名正确，并且在指定目录下。好多漏掉teamCode的。另外保证构件名字正确，参考上面。

#### 19. 那些行为消耗评测次数？答： 结果超时、结果正确、生成结果并且比对答案的时候错误。PS：编译出错、由于目录原因找不到指定结果文件都不消耗评测次数

#### 20. 比赛使用的数据文件每次都不一样吗？答： 由于生成数据文件比较耗时，为了加速评测效率，整个比赛期间都是使用一份数据文件的。

#### 21. 哪里获取teamCode? 答:在天池提交代码处设置自己的git地址，即可在下方好看到自己的teamcode

#### 22. 排行榜啥时候更新？ 答：已经调整为每小时更新一次


#### 23. sudo blockdev --getra /dev/sda的值为多少？ 256


#### 24. 哪些情况消耗评测次数？1. 结果不正确 2. 结果正确 3. 结果超时 4. 找不到结果文件 （ 因此请选手提交代码前一定要注意自己的代码准确性）

#### 25. 磁盘的并发IO性能如何？答： 采用8个进程并发读写性能如下

```

# 测试用脚本
for i in `seq 8`; do sudo time dd if=/dev/zero  of=/data/test$i  bs=8k count=1000000  & done; wait;


# 测试写性能（8个进程写8个文件，每个文件写8.2G）
vm.drop_caches = 3
^@^@1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 180.778 s, 45.3 MB/s
0.29user 65.77system 3:00.77elapsed 36%CPU (0avgtext+0avgdata 3792maxresident)k
640inputs+16000000outputs (0major+364minor)pagefaults 0swaps
1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 181.109 s, 45.2 MB/s
0.27user 66.48system 3:01.11elapsed 36%CPU (0avgtext+0avgdata 3760maxresident)k
544inputs+16000000outputs (1major+360minor)pagefaults 0swaps
1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 181.114 s, 45.2 MB/s
0.31user 66.78system 3:01.11elapsed 37%CPU (0avgtext+0avgdata 3776maxresident)k
312inputs+16000000outputs (0major+362minor)pagefaults 0swaps
1000000+0 records in
1000000+0 records out
8192000000 bytes (8.2 GB) copied, 181.116 s, 45.2 MB/s
 0.29user 66.31system 3:01.11elapsed 36%CPU (0avgtext+0avgdata 3760maxresident)k
 856inputs+16000000outputs (1major+360minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 181.247 s, 45.2 MB/s
 0.30user 66.18system 3:01.24elapsed 36%CPU (0avgtext+0avgdata 3776maxresident)k
 448inputs+16000000outputs (0major+362minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 181.322 s, 45.2 MB/s
 0.30user 66.06system 3:01.32elapsed 36%CPU (0avgtext+0avgdata 3792maxresident)k
 656inputs+16000000outputs (0major+364minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 181.323 s, 45.2 MB/s
 0.28user 66.55system 3:01.32elapsed 36%CPU (0avgtext+0avgdata 3760maxresident)k
 376inputs+16000000outputs (0major+361minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 181.338 s, 45.2 MB/s
 0.29user 66.78system 3:01.33elapsed 36%CPU (0avgtext+0avgdata 3760maxresident)k
 448inputs+16000000outputs (1major+360minor)pagefaults 0swaps
 
 
 
 # 测试读性能（8个进程读8个8.2G的文件）
 vm.drop_caches = 3
 ^@^@1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 133.928 s, 61.2 MB/s
 0.12user 4.06system 2:13.92elapsed 3%CPU (0avgtext+0avgdata 3792maxresident)k
 16000000inputs+0outputs (0major+363minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 133.948 s, 61.2 MB/s
 0.12user 4.20system 2:13.94elapsed 3%CPU (0avgtext+0avgdata 3760maxresident)k
 16000000inputs+0outputs (0major+361minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 133.962 s, 61.2 MB/s
 0.12user 4.16system 2:13.96elapsed 3%CPU (0avgtext+0avgdata 3760maxresident)k
 16000000inputs+0outputs (0major+361minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 133.988 s, 61.1 MB/s
 0.13user 4.19system 2:13.98elapsed 3%CPU (0avgtext+0avgdata 3760maxresident)k
 16000000inputs+0outputs (0major+362minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 133.989 s, 61.1 MB/s
 0.12user 4.08system 2:13.99elapsed 3%CPU (0avgtext+0avgdata 3792maxresident)k
 16000104inputs+0outputs (1major+362minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 134.006 s, 61.1 MB/s
 0.11user 4.15system 2:14.00elapsed 3%CPU (0avgtext+0avgdata 3792maxresident)k
 16000000inputs+0outputs (0major+364minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 134.012 s, 61.1 MB/s
 0.10user 4.11system 2:14.01elapsed 3%CPU (0avgtext+0avgdata 3776maxresident)k
 16000000inputs+0outputs (0major+363minor)pagefaults 0swaps
 1000000+0 records in
 1000000+0 records out
 8192000000 bytes (8.2 GB) copied, 134.014 s, 61.1 MB/s
 0.10user 4.11system 2:14.01elapsed 3%CPU (0avgtext+0avgdata 3792maxresident)k
 16000000inputs+0outputs (0major+363minor)pagefaults 0swaps
```