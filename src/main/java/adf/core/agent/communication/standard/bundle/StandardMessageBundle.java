package adf.core.agent.communication.standard.bundle;

import adf.core.agent.communication.standard.bundle.centralized.*;
import adf.core.agent.communication.standard.bundle.information.*;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.communication.MessageBundle;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class StandardMessageBundle extends MessageBundle {

  @Override
  @Nonnull
  public List<Class<? extends CommunicationMessage>> getMessageClassList() {
    List<Class<
        ? extends CommunicationMessage>> messageClassList = new ArrayList<>();

    // information
    messageClassList.add(MessageAmbulanceTeam.class);
    messageClassList.add(MessageBuilding.class);
    messageClassList.add(MessageCivilian.class);
    messageClassList.add(MessageFireBrigade.class);
    messageClassList.add(MessagePoliceForce.class);
    messageClassList.add(MessageRoad.class);
    // centralized
    messageClassList.add(CommandAmbulance.class);
    messageClassList.add(CommandFire.class);
    messageClassList.add(CommandPolice.class);
    messageClassList.add(CommandScout.class);
    messageClassList.add(MessageReport.class);

    return messageClassList;
  }
}