# **选题：基于因果推断的微服务故障根因定位算法**

## **一、 研究目标与创新点**

1. **核心目标：**
   - 设计并实现一个算法，该算法能够**自动化地、准确地**从微服务系统的调用链和性能指标数据中，定位出导致系统性能劣化或故障的**根本原因服务**。
2. **具体目标：**
   - 实现对SkyWalking等工具采集的**调用链数据的自动解析与特征提取**。
   - 构建一个融合了服务依赖拓扑和性能指标的**因果图模型**。
   - 实现一种**因果推断算法**，用于在故障发生时量化每个服务作为根因的可能性。
   - 通过**对比实验**，验证本算法在根因定位准确率、召回率等指标上优于基线方法。
3. **核心创新点：**
   - **创新点1：多模态数据融合的因果图构建。** 不仅使用调用链确定依赖关系，还将服务的响应时间、错误率等时序指标作为节点属性，构建一个 richer 的因果图，为后续推断提供更丰富的上下文。
   - **创新点2：基于PC算法的因果发现与根因排序。** 将PC算法等经典因果发现方法引入微服务根因定位场景，从观测数据中自动推断因果方向，并生成候选根因排序列表，减少对预先定义故障传播模型的依赖。

------

## **二、 技术路线与系统架构**

**总体思路：** 数据采集 → 图构建 → 因果推断 → 结果输出与验证。

1. **数据采集与预处理模块：**
   - **输入：** SkyWalking GraphQL API 提供的Trace数据。
   - **处理：**
     - 解析Trace，提取服务依赖关系（谁调用了谁）。
     - 聚合计算每个服务的性能指标（平均响应时间、错误率、吞吐量）。
     - 将数据按固定时间窗口（如1分钟）进行切片，形成时序面板数据。
2. **因果图构建模块：**
   - **节点：** 每个微服务。
   - **边：** 初始边由调用链确定的直接依赖关系决定。
   - **节点属性：** 每个节点（服务）在每个时间窗口内的性能指标序列。
3. **因果推断与根因定位模块（核心算法）：**
   - **步骤1：条件独立性测试。** 使用统计检验方法，判断在给定某个服务集Z的条件下，两个服务（节点）的性能指标是否独立。
   - **步骤2：因果结构学习。** 使用PC算法，基于上述测试结果，逐步剔除图中的冗余边，并确定部分因果方向，形成一个部分有向无环图。
   - **步骤3：根因评分。** 当系统发生故障时（如全局响应时间飙升）：
     - 将故障时间窗口的性能数据输入构建好的因果图。
     - 计算每个服务的指标与全局故障指标的**因果效应强度**。
     - 根据效应强度对所有服务进行排序，排名最高的即为最可能的根因。
4. **结果输出与可视化模块：**
   - 以REST API形式返回根因定位结果。
   - 提供一个简单的前端界面，展示服务依赖图，并高亮显示被判定为根因的服务。

------

## **三、 七周实施计划**

| 周数      | 核心任务                         | 详细说明                                                     | 输出物                                                       |
| :-------- | :------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| **第1周** | **环境搭建与数据准备**           | 1. 使用Docker Compose搭建包含SkyWalking的微服务Demo。 2. 使用JMeter对Demo系统进行压测，并人为注入故障（如让某个服务变慢或报错）。 3. 确保能通过SkyWalking API获取到Trace数据。 | 1. 可运行的微服务Demo环境。 2. 包含正常和故障场景的Trace数据。 |
| **第2周** | **数据解析与特征工程**           | 1. 编写Java程序调用SkyWalking API，解析返回的JSON数据。 2. 设计数据结构，存储服务依赖图和各服务的性能指标时间序列。 3. 实现数据聚合与时间窗口切分逻辑。 | 1. 数据解析代码。 2. 结构化的服务性能指标数据集。            |
| **第3周** | **因果图构建与PC算法实现（上）** | 1. 基于调用链构建初始的服务依赖图（邻接矩阵或邻接表）。 2. 学习PC算法原理，实现其核心步骤：**条件独立性测试**（例如，使用偏相关系数进行检验）。 | 1. 图构建模块代码。 2. 条件独立性测试函数。                  |
| **第4周** | **因果图构建与PC算法实现（下）** | 1. 完成PC算法，实现**因果方向的推断**。 2. 将算法应用于历史正常数据，学习并输出一个“稳态”下的系统因果图模型。 3. **难点：** 处理算法复杂度，可对大规模图进行剪枝。 | 1. 完整的PC算法实现。 2. 系统稳态因果图模型。                |
| **第5周** | **根因评分与定位模块实现**       | 1. 设计根因评分算法：在故障时间窗口，计算每个节点与故障根节点的因果效应（如回归系数、互信息等）。 2. 实现排序逻辑，输出候选根因列表。 | 1. 根因评分与排序模块代码。 2. 给定故障数据，能输出初步的根因列表。 |
| **第6周** | **系统集成与实验验证**           | 1. 将各模块集成，开发一个简单的Spring Boot RESTful服务，提供根因定位接口。 2. 设计对比实验：与基线方法（如随机游走Personalized PageRank、基于规则的方法）进行对比。 3. 量化评估：计算准确率、召回率、F1值、平均排名等指标。 | 1. 可运行的系统原型。 2. 详细的实验报告与结果分析。          |
| **第7周** | **论文撰写与答辩准备**           | 1. 撰写论文，重点突出**引言、相关工作、算法设计、实验分析**等章节。 2. 准备答辩PPT和演示视频。 | 1. 论文初稿。 2. 答辩材料。                                  |

------

## **四、 核心代码示例（Java伪代码风格）**

java

```
// 1. 数据模型定义
@Data
public class ServiceNode {
    private String serviceName;
    private Map<Long, Double> responseTimeSeries; // 时间戳 -> 响应时间
    private Map<Long, Double> errorRateSeries;    // 时间戳 -> 错误率
}

@Data
public class CausalGraph {
    private Map<String, ServiceNode> nodes;
    private boolean[][] adjacencyMatrix; // 邻接矩阵表示依赖关系
    private List<CausalEdge> causalEdges; // 学习到的因果边
}

// 2. PC算法核心步骤（简化版）
public class PCAlgorithm {
    public CausalGraph learnCausalStructure(Map<String, ServiceNode> data, double significance) {
        // 步骤1：初始化一个全连接图
        CausalGraph graph = initializeFullyConnectedGraph(data.keySet());
        
        // 步骤2：逐步剔除边（基于条件独立性测试）
        int n = graph.getNodeSize();
        for (int depth = 0; depth < n; depth++) {
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (graph.isConnected(i, j)) {
                        // 寻找大小为depth的分离集
                        Set<Integer> possibleSeparators = ...;
                        for (Set<Integer> S : possibleSeparators) {
                            // 进行条件独立性测试
                            double pValue = conditionalIndependenceTest(
                                data.get(i).getTimeSeries(), 
                                data.get(j).getTimeSeries(), 
                                S.stream().map(k -> data.get(k).getTimeSeries()).collect(Collectors.toList())
                            );
                            if (pValue > significance) {
                                // 独立，移除边 i-j
                                graph.removeEdge(i, j);
                                break;
                            }
                        }
                    }
                }
            }
        }
        // 步骤3：确定边的方向（基于定向规则）...
        graph.orientEdges();
        return graph;
    }
    
    private double conditionalIndependenceTest(List<Double> X, List<Double> Y, List<List<Double>> conditioningSet) {
        // 使用偏相关系数等进行检验，返回p-value
        // 简化实现：可使用现成的统计库，如Apache Commons Math
        return new TTest().pairedTTest(X, Y); // 示例，实际应用更复杂
    }
}

// 3. 根因定位器
@Service
public class RootCauseLocator {
    
    @Autowired
    private CausalGraph systemCausalGraph;
    
    public List<RankedService> locate(Map<String, ServiceNode> currentWindowData, String globalFaultIndicator) {
        List<RankedService> candidates = new ArrayList<>();
        
        for (ServiceNode node : systemCausalGraph.getNodes()) {
            // 计算该节点与全局故障指标之间的因果效应强度
            double causalStrength = calculateCausalEffect(
                node.getTimeSeries(), 
                currentWindowData.get(globalFaultIndicator).getTimeSeries(),
                systemCausalGraph.getParents(node) // 获取该节点的父节点（原因）
            );
            candidates.add(new RankedService(node.getServiceName(), causalStrength));
        }
        
        // 按效应强度降序排序
        candidates.sort(Comparator.comparing(RankedService::getScore).reversed());
        return candidates;
    }
    
    private double calculateCausalEffect(List<Double> cause, List<Double> effect, List<ServiceNode> parents) {
        // 简化实现：使用线性回归系数作为因果效应的近似
        // Y = effect, X = [cause, parent1, parent2, ...]
        // 返回cause变量的回归系数
        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < cause.size(); i++) {
            double[] x = new double[1 + parents.size()];
            x[0] = cause.get(i);
            for (int j = 0; j < parents.size(); j++) {
                x[j+1] = parents.get(j).getTimeSeries().get(i);
            }
            regression.addObservation(x, effect.get(i));
        }
        return regression.getSlope(); // 返回原因变量（cause）的系数
    }
}
```



------

## **五、 预期成果与评估**

- **成果：**
  1. 一个可运行的、基于因果推断的微服务根因定位系统原型。
  2. 一份算法对比实验报告，证明本算法在预设故障场景下的有效性（例如，在3种故障注入场景下，Top-1准确率达到85%以上，优于基线方法10%）。

这个计划为你提供了一个清晰的路线图。它**聚焦于算法核心**，**创新点明确**，工作量适合本科论文，并且技术栈与你原有的Java背景高度契合。

## 六、具体分工

### **核心分工原则**

- **同学A（算法与核心逻辑）：** 负责所有需要**深度思考和逻辑设计**的部分。是项目的**架构师和大脑**。
- **同学B（环境与数据管道）：** 负责所有**有明确指令和可视化结果**的部分。是项目的**建造者和感官**。
- **协作模式：** A同学需要为B同学提供**非常具体的、可执行的指令**（比如具体的命令、代码文件、配置内容）。B同学负责执行，并将结果（如日志、截图）反馈给A。

------

### **分周详细分工表

| 周数                     | 同学A（主导逻辑设计）                                        | 同学B（主导环境与执行）                                      | 协作与A对B的指令                                             |                                                              |                                                              |
| :----------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **第1周** 巩固基础       | 1. **设计微服务Demo：** 确定要创建哪几个服务（如网关、用户、订单），以及它们之间的调用关系。 2. **编写服务代码：** 创建Spring Boot项目，编写Controller，使用OpenFeign实现服务间调用。 3. **制造性能瓶颈：** 在订单服务中加入`Thread.sleep(1000)`。 | 1. **提供SkyWalking环境：** 将已搭建好的Docker Compose文件分享给A。 2. **集成Agent：** 在A写好的Demo服务中，按照SkyWalking官方文档，配置`skywalking-agent.jar`。 3. **启动与验证：** 启动所有服务，在SkyWalking UI上确认能看到调用链。 | A对B说：“这是我的项目代码，请把`agent`文件夹里的配置，按照这个文档[附链接]加到你的启动命令里，然后启动服务，给我一张SkyWalking看到链路的截图。” |                                                              |                                                              |
| **第2周** 数据获取       | 1. **研究SkyWalking API：** 使用Postman或浏览器，手动调用GraphQL API，理解如何查询Trace数据。 2. **设计数据模型：** 定义Java类，如`Trace`, `Span`, `ServiceNode`，用来存放解析后的数据。 3. **编写核心解析器：** 实现将API返回的复杂JSON映射到自己定义的简单Java对象的功能。 | 1. **数据采集：** 使用JMeter或Postman，疯狂访问A写的Demo服务，生成大量Trace数据。 2. **提供数据样本：** 从SkyWalking API中直接复制几条原始的JSON格式的Trace数据，交给A用于开发解析器。 3. **验证解析：** 运行A写好的解析程序，看是否能成功打印出服务名、耗时等信息。 | A对B说：“这是我写的一个解析程序，你把你从API里拿到的原始JSON数据贴到这个`input.json`文件里，然后运行这个程序，把控制台打印的结果截图给我。” |                                                              |                                                              |
| **第3周** 因果图构建(上) | 1. **搭建算法骨架：** 创建`PCAlgorithm`空类，里面写好方法名（如`findCausalRelationships`）。 2. **实现简单统计：** 先实现一个简单的“相关性计算”作为热身，比如计算两个服务响应时间的皮尔逊系数。 3. **寻找因果发现库：** 研究我上面推荐的`causal-learn`库，并写一个最简单的Java调用Python的Demo。 | 1. **生成测试数据集：** 按照A的要求，从SkyWalking中导出不同时间段的指标数据，整理成CSV文件（第一行是服务名，下面是数字）。 2. **环境准备：** 在自己的电脑上安装Python和`causal-learn`库 (`pip install causal-learn`)。 3. **运行验证：** 运行A提供的Python Demo脚本，确保环境没问题。 | A对B说：“请安装Python，然后执行`pip install causal-learn`。之后运行我这个`test.py`脚本，把生成的图片发给我看。” |                                                              |                                                              |
| **第4周** 因果图构建(下) | 1. **集成因果库：** 编写Java程序，将第2周解析好的数据输出为CSV文件，然后自动调用B同学环境里的Python脚本执行PC算法。 2. **解析结果：** 编写代码读取Python脚本输出的结果（如图片、文本），并转换为Java内部的图结构。 3. **【创新点】** 思考如何将调用链信息作为先验知识，辅助PC算法。 | 1. **管道连接：** 协助A调试Java调用Python的流程。 2. **结果可视化：** 将Python生成的因果图图片保存下来，用于论文和前端展示。 3. **前端准备：** 使用HTML+ECharts，画一个静态的、假数据的服务依赖图。 | A对B说：“我写了一个程序，它会生成`data.csv`并调用你的`pc_algorithm.py`。你帮我运行一下，看最后是不是生成了一张`causal_graph.png`的图片。” |                                                              |                                                              |
| **第5周** 根因定位       | 1. **设计评分算法：** 这是A同学的**核心创新点**。基于第4周得到的因果图，设计一个简单的公式，例如：`分数 = 与故障节点的相关性 + 在因果图中的重要性`。 2. **提供根因API：** 编写一个Spring Boot Controller，实现`/api/rootcause`接口，内部调用你的评分算法。 3. **制造故障：** 修改Demo，让用户服务随机变慢，用于测试。 | 1. **开发前端界面：** - 做一个按钮，点击后调用A提供的`/api/rootcause`接口。 - 将返回的JSON结果（根因服务列表）用alert弹窗或一个简单的列表显示出来。 2. **测试验证：** 制造故障，在界面上点击按钮，看返回的根因是不是“用户服务”。 | A对B说：“这是我的API地址。你在你的网页上加一个按钮，点击后就访问这个地址，然后把返回的结果显示出来。我们现在让用户服务变慢，看看结果显示的对不对。” |                                                              |                                                              |
| **第6周** 实验验证       | 1. **设计实验：** 设计2-3种故障场景（如服务A慢、服务B高错误率）。 2. **实现基线方法：** 实现一个非常简单的基线方法（如“谁最慢谁就是根因”）。 3. **运行对比：** 在相同故障下，运行自己的算法和基线方法，记录结果。 | 1. **充当“测试员”：** 按照A的指令，反复制造不同的故障，并从前端界面和日志中记录两种方法的结果。 2. **整理数据：** 将测试结果整理成Excel表格（故障场景 | 你的算法结果                                                 | 基线方法结果）。 3. **制作图表：** 使用Excel将A同学分析好的数据，绘制成准确的柱状图或折线图。 | A对B说：“我们现在测试三种情况：1. 用户服务慢。2. 订单服务报错。3. 两个都慢。你分别用我们的系统和简单方法记录下找出的根因，然后做成一个表格给我。” |
| **第7周** 论文与答辩     | 1. **撰写核心章节：** 绪论、相关工作、**算法设计（重点）**、核心实现。 2. **分析实验数据：** 根据B整理的表格，分析为什么你的算法更好。 | 1. **撰写非核心章节：** 环境搭建与配置、前端界面展示、实验数据记录。 2. **制作PPT和演示视频：** 负责所有视觉化材料的制作。 3. **准备Demo演示：** 熟练操作整个系统，负责答辩时的现场演示。 | A对B说：“这是论文的算法部分初稿。你根据你的经验，把‘环境搭建’和‘前端实现’两节写完。另外，把我们上周的测试结果做成PPT里的对比图表。” |                                                              |                                                              |

------

### **关键接口定义示例（第2周末尾必须确定）**

这个接口是两人协作的基石。

**接口1：获取系统服务依赖图**

- **URL:** `GET /api/topology`
- **响应 (JSON):**

json

```
{
  "nodes": [
    {"id": "gateway-service", "name": "网关服务"},
    {"id": "user-service", "name": "用户服务"},
    {"id": "order-service", "name": "订单服务"}
  ],
  "links": [
    {"source": "gateway-service", "target": "order-service"},
    {"source": "order-service", "target": "user-service"}
  ]
}
```



**接口2：进行根因定位分析**

- **URL:** `GET /api/rootcause?startTime=1234567890&endTime=1234567990`
- **响应 (JSON):**

json

```
{
  "globalFaultIndicator": "avg_response_time",
  "rootCauses": [
    {"serviceName": "user-service", "score": 0.95, "description": "响应时间异常升高"},
    {"serviceName": "order-service", "score": 0.72, "description": "错误率飙升"}
  ]
}
```



### **Git协作流程建议**

1. **同学A**创建主仓库。
2. **同学B** Fork 或 Clone 后，在自己的分支（如 `feat-frontend`）上开发。
3. 每周合并一次代码到主分支（`main`）。合并前必须进行沟通，解决冲突。
4. 提交信息要规范，例如：`feat: 实现PC算法核心逻辑`，`fix: 修复前端图表数据绑定错误`。

通过这样的分工，两位同学职责清晰，目标明确，既发挥了各自的专长，又通过紧密的协作确保了项目的整体性和进度。



## 七、参考文献

[1]佟业新,曲新奎,杨皓然,等. 基于微服务分布式链路的服务质量优化策略[J].计算机系统应用,2024,33(09):140-152.DOI:10.15888/j.cnki.csa.009628.
[2]张齐勋,吴一凡,杨勇,等. 微服务系统服务依赖发现技术综述[J].软件学报,2024,35(01):118-135.DOI:10.13328/j.cnki.jos.006827.