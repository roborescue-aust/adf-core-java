package adf.impl.tactics;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandPicker;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.module.complex.TargetAllocator;
import adf.core.component.tactics.TacticsAmbulanceCentre;
import adf.core.debug.WorldViewLauncher;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

/**
 * 救护中心的默认策略
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsAmbulanceCentre extends TacticsAmbulanceCentre {

    private TargetAllocator allocator; // 目标分配器
    private CommandPicker picker; // 命令选择器
    private Boolean isVisualDebug; // 是否启用可视化调试模式

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
        messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
                "MessageManager.CenterChannelSubscriber",
                "adf.impl.module.comm.DefaultChannelSubscriber"));
        messageManager.setMessageCoordinator(moduleManager.getMessageCoordinator(
                "MessageManager.CenterMessageCoordinator",
                "adf.impl.module.comm.DefaultMessageCoordinator"));
        // 根据场景模式选择目标分配器和命令选择器
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.allocator = moduleManager.getModule(
                        "DefaultTacticsAmbulanceCentre.TargetAllocator",
                        "adf.impl.module.complex.DefaultAmbulanceTargetAllocator");
                this.picker = moduleManager.getCommandPicker(
                        "DefaultTacticsAmbulanceCentre.CommandPicker",
                        "adf.impl.centralized.DefaultCommandPickerAmbulance");
                break;
        }
        // 注册模块
        registerModule(this.allocator);
        registerModule(this.picker);

        // 判断是否可视化调试
        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));
    }

    /**
     * 策略思考方法
     * 在此方法中进行模块信息更新、可视化调试和发送消息等操作
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
    public void think(AgentInfo agentInfo, WorldInfo worldInfo,
                      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                      MessageManager messageManager, DevelopData developData) {
        // 更新模块信息
        modulesUpdateInfo(messageManager);

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }

        // 进行目标分配
        Map<EntityID, EntityID> allocatorResult = this.allocator.calc().getResult();
        // 根据目标分配结果进行指令生成
        for (CommunicationMessage message : this.picker
                .setAllocatorResult(allocatorResult).calc().getResult()) {
            messageManager.addMessage(message);
        }
    }

    /**
     * 策略恢复方法
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
        // 模块恢复操作
        modulesResume(precomputeData);

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    /**
     * 策略准备方法
     * 在precompute准备阶段进行模块的准备操作
     *
     * @param agentInfo     代理的信息
     * @param worldInfo     世界的信息
     * @param scenarioInfo  场景的信息
     * @param moduleManager 模块管理器
     * @param developData   开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData developData) {
        // 模块准备操作
        modulesPreparate();

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }
}
