package adf.core.component.tactics;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandPicker;
import adf.core.component.module.AbstractModule;

import java.util.ArrayList;
import java.util.List;

public abstract class TacticsCenter {

  private TacticsCenter parentControl;
  private List<AbstractModule> modules = new ArrayList<>();
  private List<CommandPicker> modulesCommandPicker = new ArrayList<>();

  public TacticsCenter(TacticsCenter parent) {
    this.parentControl = parent;
  }


  public TacticsCenter() {
    this(null);
  }


  abstract public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData);

  abstract public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      PrecomputeData precomputeInfo, DevelopData developData);

  abstract public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      DevelopData developData);

  abstract public void think(AgentInfo agentInfo, WorldInfo worldInfo,
      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
      MessageManager messageManager, DevelopData developData);


  public TacticsCenter getParentControl() {
    return parentControl;
  }


  protected void registerModule(AbstractModule module) {
    modules.add(module);
  }


  protected boolean unregisterModule(AbstractModule module) {
    return modules.remove(module);
  }


  protected void registerModule(CommandPicker module) {
    modulesCommandPicker.add(module);
  }


  protected boolean unregisterModule(CommandPicker module) {
    return modulesCommandPicker.remove(module);
  }


  protected void modulesPrecompute(PrecomputeData precomputeData) {
    for (AbstractModule module : modules) {
      module.precompute(precomputeData);
    }
    for (CommandPicker module : modulesCommandPicker) {
      module.precompute(precomputeData);
    }
  }


  protected void modulesResume(PrecomputeData precomputeData) {
    for (AbstractModule module : modules) {
      module.resume(precomputeData);
    }
    for (CommandPicker module : modulesCommandPicker) {
      module.resume(precomputeData);
    }
  }


  protected void modulesPreparate() {
    for (AbstractModule module : modules) {
      module.preparate();
    }
    for (CommandPicker module : modulesCommandPicker) {
      module.preparate();
    }
  }


  protected void modulesUpdateInfo(MessageManager messageManager) {
    for (AbstractModule module : modules) {
      module.updateInfo(messageManager);
    }
    for (CommandPicker module : modulesCommandPicker) {
      module.updateInfo(messageManager);
    }
  }
}