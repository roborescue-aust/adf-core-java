package adf.agent.platoon;

import java.util.EnumSet;

import rescuecore2.standard.entities.FireBrigade;
import rescuecore2.standard.entities.StandardEntityURN;

import adf.agent.config.ModuleConfig;
import adf.agent.develop.DevelopData;
import adf.component.tactics.TacticsFireBrigade;

public class PlatoonFire extends Platoon<FireBrigade> {

  public PlatoonFire(TacticsFireBrigade tactics, boolean isPrecompute, boolean isDebugMode, ModuleConfig moduleConfig,
      DevelopData developData) {
    super(tactics, isPrecompute, DATASTORAGE_FILE_NAME_FIRE, isDebugMode, moduleConfig, developData);
  }

  @Override
  protected EnumSet<StandardEntityURN> getRequestedEntityURNsEnum() {
    return EnumSet.of(StandardEntityURN.FIRE_BRIGADE);
  }

  @Override
  protected void postConnect() {
    super.postConnect();
  }
}
