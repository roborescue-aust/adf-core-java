package adf.core.agent.action.common;

import adf.core.agent.action.Action;
import rescuecore2.messages.Message;
import rescuecore2.standard.messages.AKMove;
import rescuecore2.worldmodel.EntityID;

import javax.annotation.Nonnull;
import java.util.List;
/**
 *动作移动指令
 */


public class ActionMove extends Action {

    private List<EntityID> path;

    private boolean usePosition;
    private int posX;//位置坐标X
    private int posY;//位置坐标Y

    public ActionMove(@Nonnull List<EntityID> movePath) {
    super();
    this.usePosition = false;
    this.path = movePath;
  }
/**
 *
 *
 */

  public ActionMove(@Nonnull List<
      EntityID> movePath, int destinationX, int destinationY) {
    super();
    this.usePosition = true;
    this.path = movePath;
    this.posX = destinationX;
    this.posY = destinationY;
  }


  @Override
  @Nonnull
  public String toString() {
    return "ActionMove [usePosition=" + usePosition + ", posX=" + posX
        + ", posY=" + posY + ", path=" + path + "]";
  }


  @Nonnull
  public List<EntityID> getPath() {
    return this.path;
  }


  public boolean getUsePosition() {
    return this.usePosition;
  }


  public int getPosX() {
    return this.posX;
  }


  public int getPosY() {
    return this.posY;
  }


  @Override
  @Nonnull
  public Message getCommand(@Nonnull EntityID agentID, int time) {
    return this.usePosition
        ? new AKMove(agentID, time, this.path, this.posX, this.posY)
        : new AKMove(agentID, time, this.path);
  }
}