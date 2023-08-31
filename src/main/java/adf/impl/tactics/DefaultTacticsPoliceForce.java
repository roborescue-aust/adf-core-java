package adf.impl.tactics;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.action.police.ActionClear;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandPolice;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
import adf.core.agent.communication.standard.bundle.information.MessagePoliceForce;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.complex.RoadDetector;
import adf.core.component.module.complex.Search;
import adf.core.component.tactics.TacticsPoliceForce;
import adf.core.debug.WorldViewLauncher;
import adf.impl.tactics.utils.MessageTool;
import rescuecore2.standard.entities.PoliceForce;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.Objects;

/**
 * 警察部队的默认策略
 * <p>
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsPoliceForce extends TacticsPoliceForce {

    private int clearDistance; // 清理障碍物的距离

    private RoadDetector roadDetector; // 道路检测模块
    private Search search; // 搜索模块

    private ExtAction actionExtClear; // 清理障碍物的扩展动作
    private ExtAction actionExtMove; // 移动的扩展动作

    private CommandExecutor<CommandPolice> commandExecutorPolice; // 警察命令执行器
    private CommandExecutor<CommandScout> commandExecutorScout; // 侦察命令执行器

    private MessageTool messageTool; // 消息工具

    private CommunicationMessage recentCommand; // 最近的命令

    private Boolean isVisualDebug; // 是否启用可视化调试

    /**
     * 初始化方法
     * 在此方法中进行模块的初始化和注册
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           MessageManager messageManager, DevelopData developData) {
        // 设置消息管理器的订阅者和协调者
        messageManager.setChannelSubscriber(
                moduleManager.getChannelSubscriber("MessageManager.PlatoonChannelSubscriber",
                        "adf.impl.module.comm.DefaultChannelSubscriber"));
        messageManager.setMessageCoordinator(
                moduleManager.getMessageCoordinator("MessageManager.PlatoonMessageCoordinator",
                        "adf.impl.module.comm.DefaultMessageCoordinator"));

        // Index entity types
        worldInfo.indexClass(StandardEntityURN.ROAD, StandardEntityURN.HYDRANT,
                StandardEntityURN.BUILDING, StandardEntityURN.REFUGE,
                StandardEntityURN.BLOCKADE);
        // 创建消息工具
        this.messageTool =
                new MessageTool(scenarioInfo, developData);
        // 从配置中获取可视化调试标志
        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));

        // 从场景信息中获取clearDistance
        this.clearDistance = scenarioInfo.getClearRepairDistance();
        // 将最近收到的命令初始化为空
        this.recentCommand = null;

        // 根据仿真模式初始化算法模块和扩展动作
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.search = moduleManager.getModule("DefaultTacticsPoliceForce.Search",
                        "adf.impl.module.complex.DefaultSearch");
                this.roadDetector = moduleManager.getModule(
                        "DefaultTacticsPoliceForce.RoadDetector",
                        "adf.impl.module.complex.DefaultRoadDetector");
                this.actionExtClear = moduleManager.getExtAction(
                        "DefaultTacticsPoliceForce.ExtActionClear",
                        "adf.impl.extaction.DefaultExtActionClear");
                this.actionExtMove = moduleManager.getExtAction(
                        "DefaultTacticsPoliceForce.ExtActionMove",
                        "adf.impl.extaction.DefaultExtActionMove");
                this.commandExecutorPolice = moduleManager.getCommandExecutor(
                        "DefaultTacticsPoliceForce.CommandExecutorPolice",
                        "adf.impl.centralized.DefaultCommandExecutorPolice");
                this.commandExecutorScout = moduleManager.getCommandExecutor(
                        "DefaultTacticsPoliceForce.CommandExecutorScout",
                        "adf.impl.centralized.DefaultCommandExecutorScoutPolice");
                break;
        }
        // 注册模块
        registerModule(this.search);
        registerModule(this.roadDetector);
        registerModule(this.actionExtClear);
        registerModule(this.actionExtMove);
        registerModule(this.commandExecutorPolice);
        registerModule(this.commandExecutorScout);
    }


    /**
     * 预计算
     * 在precompute恢复阶段进行模块的恢复操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void precompute(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           PrecomputeData precomputeData, DevelopData developData) {
        modulesPrecompute(precomputeData);
    }

    /**
     * 恢复方法
     * 在precompute恢复阶段进行模块的恢复操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData developData) {
        // 恢复模块
        modulesResume(precomputeData);

        // 如果进行可视化调试，则显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }
    /**
     * 策略准备方法
     * 在precompute准备阶段进行模块的准备操作
     *
     * @param agentInfo     智能体信息
     * @param worldInfo     世界信息
     * @param scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData     调试数据
     */
    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData developData) {
        // 模块准备
        modulesPreparate();

        // 如果进行可视化调试，则显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }
    /**
     * 策略思考方法
     * 在此方法中进行模块信息更新、可视化调试和发送消息等操作
     *
     * @param agentInfo      智能体信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   场景信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData      调试数据
     */
    @Override
    public Action think(AgentInfo agentInfo, WorldInfo worldInfo,
                        ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                        MessageManager messageManager, DevelopData developData) {
        // 反映收到的消息
        this.messageTool.reflectMessage(agentInfo, worldInfo, scenarioInfo,
                messageManager);
        // 发送请求消息
        this.messageTool.sendRequestMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);
        //发送信息消息
        this.messageTool.sendInformationMessages(agentInfo, worldInfo, scenarioInfo,
                messageManager);
        // 根据收到的消息更新模块信息
        modulesUpdateInfo(messageManager);
        // 显示可视化调试（如果已启用）
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
        // 获取当前代理及其 ID
        PoliceForce agent = (PoliceForce) agentInfo.me();
        EntityID agentID = agent.getID();

        // 处理收到的命令
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandScout.class)) {
            CommandScout command = (CommandScout) message;
            // 如果命令目标 ID 与代理 ID 相同，设置recentCommand和commandExecutorScout
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorScout.setCommand(command);
            }
        }
        for (CommunicationMessage message : messageManager
                .getReceivedMessageList(CommandPolice.class)) {
            CommandPolice command = (CommandPolice) message;
            // 如果命令目标 ID 与代理 ID 相同，设置recentCommand和commandExecutorScout
            if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
                    .getValue() == agentID.getValue()) {
                this.recentCommand = command;
                this.commandExecutorPolice.setCommand(command);
            }
        }
        // 如果找到有效的最近命令，执行相应的操作，然后返回action
        if (this.recentCommand != null) {
            Action action = null;
            if (this.recentCommand.getClass() == CommandPolice.class) {
                action = this.commandExecutorPolice.calc().getAction();
            } else if (this.recentCommand.getClass() == CommandScout.class) {
                action = this.commandExecutorScout.calc().getAction();
            }
            if (action != null) {
                this.sendActionMessage(worldInfo, messageManager, agent, action);
                return action;
            }
        }

        // 如果没有找到有效的最近命令，则计算自主操作的目标

        // 首先从道路探测器获取目标
        EntityID target = this.roadDetector.calc().getTarget();
        // 清理动作根据探测器给出的目标计算需要执行的动作
        Action action = this.actionExtClear.setTarget(target).calc().getAction();
        // 如果找到下一步将要执行的动作则执行并返回
        if (action != null) {
            this.sendActionMessage(worldInfo, messageManager, agent, action);
            return action;
        }
        // 如果没有找到，则根据搜索算法获取目标
        target = this.search.calc().getTarget();
        // 清理动作根据搜索算法给出的目标计算需要执行的动作
        action = this.actionExtClear.setTarget(target).calc().getAction();
        if (action != null) {
            this.sendActionMessage(worldInfo, messageManager, agent, action);
            return action;
        }
        // 如果未找到有效的动作，发送 REST 操作消息，执行休息动作并返回
        messageManager.addMessage(new MessagePoliceForce(true, agent,
                MessagePoliceForce.ACTION_REST, agent.getPosition()));
        return new ActionRest();
    }
    /**
     * 向消息管理器发送操作消息。
     *
     * @param messageManager The message manager
     * @param policeForce      The policeForce agent
     * @param action         The action to be sent
     * @author <a href="https://roozen.top">Roozen</a>
     */
    private void sendActionMessage(WorldInfo worldInfo,
                                   MessageManager messageManager,
                                   PoliceForce policeForce, Action action) {
        Class<? extends Action> actionClass = action.getClass();
        int actionIndex = -1;
        EntityID target = null;

        // 根据动作类型确定动作索引和目标
        if (actionClass == ActionMove.class) {
            List<EntityID> path = ((ActionMove) action).getPath();
            actionIndex = MessagePoliceForce.ACTION_MOVE;
            if (path.size() > 0) {
                target = path.get(path.size() - 1);
            }
        } else if (actionClass == ActionClear.class) {
            actionIndex = MessagePoliceForce.ACTION_CLEAR;
            ActionClear ac = (ActionClear) action;
            target = ac.getTarget();
            if (target == null) {
                for (StandardEntity entity : worldInfo.getObjectsInRange(ac.getPosX(),
                        ac.getPosY(), this.clearDistance)) {
                    if (entity.getStandardURN() == StandardEntityURN.BLOCKADE) {
                        target = entity.getID();
                        break;
                    }
                }
            }
        } else if (actionClass == ActionRest.class) {
            actionIndex = MessagePoliceForce.ACTION_REST;
            target = policeForce.getPosition();
        }

        // 如果找到有效的操作索引，发送操作消息
        if (actionIndex != -1) {
            messageManager.addMessage(
                    new MessagePoliceForce(true, policeForce, actionIndex, target));
        }
    }
}
